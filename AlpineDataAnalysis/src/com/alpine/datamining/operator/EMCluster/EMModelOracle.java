/**
 * 

* ClassName EMModelOracle.java
*
* Version information: 1.00
*
* Data: Jul 30, 2012
*
* COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.

 */

package com.alpine.datamining.operator.EMCluster;
/**
 * @author Shawn
 * 
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

public class EMModelOracle extends EMModel{
    private static Logger itsLogger= Logger.getLogger(EMModelOracle.class);

    private static final long serialVersionUID = -8464704058532331726L;

	public EMModelOracle(DataSet trainingDataSet) {
		super(trainingDataSet);
	}

	@Override
	public void EMClusterPredict(String predictTable,String appendOnlyString,
			String endingString, Connection conncetion, String dropIfExist,
			String schemaName, String tableName) throws SQLException {
		Statement st=null;
		try {
			
			st = conncetion.createStatement();
			StringBuffer dropifexists=new StringBuffer();
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				dropifexists.append("call PROC_DROPSCHTABLEIFEXISTS('")
				.append(StringHandler.doubleQ(schemaName))
				.append(".")
				.append(StringHandler.doubleQ(tableName))
				.append("')");
				itsLogger.debug("EMModelOracle.EMClusterPredict():sql=" + dropifexists);
				
				st.execute(dropifexists.toString());
			}
		
			long timeStamp = System.currentTimeMillis();
			StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
			int columnsize=0;
			int clusterNumber=this.getClusterNumber();
			StringBuffer alphaBuffer=new StringBuffer();
			StringBuffer muBuffer=new StringBuffer();
			StringBuffer sigmaBuffer=new StringBuffer();
			StringBuffer sql=new StringBuffer();
			StringBuffer dataInfo=new StringBuffer();
			Iterator<Column> localIterator=getTrainingHeader().getColumns().iterator();
			List<String> columnList= new ArrayList<String>(); 
			StringBuffer inforArray = new StringBuffer();
			inforArray.append("varchar2array(");
			while (localIterator.hasNext()) {
				Column tmp=localIterator.next();
				String distinctValue = this.getAllTransformMap_valueKey().get(tmp.getName());
				String columnValues =  null;
				if(distinctValue!=null){
					String originColumnName=tmp.getName().substring(0,tmp.getName().length()-distinctValue.length()-1);
					columnValues="(case when "+originColumnName+"=''"+distinctValue+"'' then 1 else 0 end)";
				}
				else{
					columnValues =   tmp.getName();
					columnValues = StringHandler.doubleQ(columnValues);
					columnValues = columnValues.substring(1, columnValues.length() - 1);
				}
				columnList.add(tmp.getName());
				inforArray.append("'");
				inforArray.append(columnValues);
				inforArray.append("',");
				columnsize++;
			}
			inforArray.deleteCharAt(inforArray.length() - 1);
			inforArray.append(")");
			
			for(int i:this.getClusteInfo().keySet())
			{
				alphaBuffer.append(this.getClusteInfo().get(i).getAlpha()).append(',');
				for(int j=0;j<columnsize;j++)
				{
					muBuffer.append(this.getClusteInfo().get(i).getMuValue().get(columnList.get(j))).append(',');
					sigmaBuffer.append(this.getClusteInfo().get(i).getSigmaValue().get(columnList.get(j))).append(',');
				}
			}
			dataInfo.append("floatarray(").append(alphaBuffer).append(muBuffer).append(sigmaBuffer);
			  
			 
			 
			sql.append("select ")
			.append(AlpineStoredProcedure.EMCLUSTER_PREDICT_STRING)
			.append("('")
			.append(StringHandler.doubleQ(schemaName))
			.append(".")
			.append(StringHandler.doubleQ(tableName))
			.append("','").append(StringHandler.doubleQ(predictTable))
			.append("', ").append(inforArray)
			.append(",").append(dataInfo.substring(0, dataInfo.length() - 1)).append(")")
			.append(",").append(clusterNumber)
			.append(",'").append(StringHandler.doubleQ(temptablename.toString()))
			.append("')").append(" from dual");
			itsLogger.debug("EMModelOracle.EMPredict():sql=" + sql.toString());

			st.executeQuery(sql.toString());

			
		} catch (SQLException e) {
			itsLogger.debug("EMModelOracle.EMPredict()" + e);
			throw new SQLException(e);
			
		}finally{
			try {
				if(st != null)
					{
						st.close();
					} 
				}catch (SQLException e) {
					itsLogger.debug(e.toString());
					throw  e;
					}

		}
	}

}
