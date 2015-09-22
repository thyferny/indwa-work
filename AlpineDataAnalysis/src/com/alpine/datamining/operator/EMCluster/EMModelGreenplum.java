 /**
 * 

* ClassName EMModelGreenplum.java
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

 public class EMModelGreenplum extends EMModel{
     private static Logger itsLogger= Logger.getLogger(EMModelGreenplum.class);

     private static final long serialVersionUID = 3717107045157862606L;

	public EMModelGreenplum(DataSet trainingDataSet) {
		super(trainingDataSet);
	}

	@Override
	public void EMClusterPredict(String predictTable,String appendOnlyString,
			String endingString, Connection conncetion, String dropIfExist, String schemaName, String tableName) throws SQLException {
		Statement st=null;
		
		try {
			
			st = conncetion.createStatement();
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("EMModelGreenplum.EMClusterPredict():sql=" +
                        "Drop table IF EXISTS " + StringHandler.doubleQ(schemaName) + "." + StringHandler.doubleQ(tableName));
				
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName));
			}
			
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
			inforArray.append("array[");
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
			inforArray.append("]");
			
			for(int i:this.getClusteInfo().keySet())
			{
				alphaBuffer.append(this.getClusteInfo().get(i).getAlpha()).append(',');
				for(int j=0;j<columnsize;j++)
				{
					muBuffer.append(this.getClusteInfo().get(i).getMuValue().get(columnList.get(j))).append(',');
					sigmaBuffer.append(this.getClusteInfo().get(i).getSigmaValue().get(columnList.get(j))).append(',');
				}
			}
			dataInfo.append("array[").append(alphaBuffer).append(muBuffer).append(sigmaBuffer);
			  
			 
			 
			sql.append("select ")
			.append(AlpineStoredProcedure.EMCLUSTER_PREDICT_STRING)
			.append("('")
			.append(StringHandler.doubleQ(schemaName))
			.append(".")
			.append(StringHandler.doubleQ(tableName))
			.append("','").append(predictTable)
			.append("', ").append(inforArray)
			.append(",").append(dataInfo.substring(0, dataInfo.length() - 1)).append("]")
			.append(",'").append(appendOnlyString)
			.append("','").append(endingString)
			.append("',").append(clusterNumber)
			.append(")");
			itsLogger.debug("EMModelGreenplum.EMClusterPredict():sql=" + sql.toString());

			st.executeQuery(sql.toString());

			
		} catch (SQLException e) {
			itsLogger.debug("EMModelGreenplum.EMClusterPredict()" + e);
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
