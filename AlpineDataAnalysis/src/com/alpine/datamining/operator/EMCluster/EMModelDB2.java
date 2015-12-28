 

package com.alpine.datamining.operator.EMCluster;



import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

 public class EMModelDB2 extends EMModel{
     private static Logger itsLogger= Logger.getLogger(EMModelDB2.class);


     private static final long serialVersionUID = 7703744937919610269L;

	public EMModelDB2(DataSet trainingDataSet) {
		super(trainingDataSet);
	}
	
	@Override
	public void EMClusterPredict(String predictTable,String appendOnlyString,
			String endingString, Connection connection, String dropIfExist,
			String schemaName, String tableName) throws SQLException {
		Statement st=null;
		try {
			
			st = connection.createStatement();
			StringBuffer dropifexists=new StringBuffer();
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				dropifexists.append("call PROC_DROPSCHTABLEIFEXISTS('")
				.append(StringHandler.doubleQ(schemaName))
				.append("','")
				.append(StringHandler.doubleQ(tableName))
				.append("')");
				itsLogger.debug("EMModelDB2.EMClusterPredict():sql=" + dropifexists);
				
				st.execute(dropifexists.toString());
			}
		
			long timeStamp = System.currentTimeMillis();
			StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
			int columnsize=getTrainingHeader().getColumns().size();
			int clusterNumber=this.getClusterNumber();
			StringBuffer sql=new StringBuffer();
			Iterator<Column> ColumnIterator=getTrainingHeader().getColumns().iterator();
			String[] ColumnArray = new String[columnsize];
			int i=0;

			List<String> columnList= new ArrayList<String>(); 
			while (ColumnIterator.hasNext()) {
				Column tmp = ColumnIterator.next();
				
				String distinctValue = this.getAllTransformMap_valueKey().get(tmp.getName());
				String columnValues =  null;
				if(distinctValue!=null){
					String originColumnName=tmp.getName().substring(0,tmp.getName().length()-distinctValue.length()-1);
					columnValues="case when "+originColumnName+"='"+distinctValue+"' then 1 else 0 end";
				}
				else{
					columnValues =   tmp.getName();
					columnValues = StringHandler.doubleQ(columnValues);
					columnValues = columnValues.substring(1, columnValues.length() - 1);
				}
				
				ColumnArray[i] =columnValues;
				columnList.add(tmp.getName());
				i++;
			}
			
			ArrayList<Double> alpha=new ArrayList<Double>();
			ArrayList<Double> mu=new ArrayList<Double>();
			ArrayList<Double> sigma=new ArrayList<Double>();
			
			for(i=0;i<clusterNumber;i++)
			{
				alpha.add(i, this.getClusteInfo().get(i+1).getAlpha());
				for(int j=0;j<columnsize;j++)
				{
					mu.add(i*columnsize+j, this.getClusteInfo().get(i+1).getMuValue().get(columnList.get(j)));
					sigma.add(i*columnsize+j, this.getClusteInfo().get(i+1).getSigmaValue().get(columnList.get(j)));
				}
			}
			
			  
			sql.append("call ")
			.append(AlpineStoredProcedure.EMCLUSTER_PREDICT_STRING).append("('")
			.append(StringHandler.doubleQ(schemaName)).append(".").append(StringHandler.doubleQ(tableName)).append("','")
			.append(predictTable).append("', ")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append(clusterNumber).append(",'")
			.append(StringHandler.doubleQ(temptablename.toString())).append("')");
			itsLogger.debug("EMModelDB2.EMPredict():sql=" + sql.toString());
			
			CallableStatement stpCall = connection.prepareCall(sql.toString()); 
			java.sql.Array columnArray = connection.createArrayOf("VARCHAR",
					ColumnArray);
			java.sql.Array alphaArray = connection.createArrayOf("DOUBLE",
					alpha.toArray(new Double[0]));
			java.sql.Array muArray = connection.createArrayOf("DOUBLE",
					mu.toArray(new Double[0]));
			java.sql.Array sigmaArray = connection.createArrayOf("DOUBLE",
					sigma.toArray(new Double[0]));
			stpCall.setArray(1, columnArray);
			stpCall.setArray(2, alphaArray);
			stpCall.setArray(3, muArray);
			stpCall.setArray(4, sigmaArray);

			stpCall.execute();
			
			//st.executeQuery(sql.toString());

			
		} catch (SQLException e) {
			itsLogger.debug("EMModelDB2.EMPredict()" + e);
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
