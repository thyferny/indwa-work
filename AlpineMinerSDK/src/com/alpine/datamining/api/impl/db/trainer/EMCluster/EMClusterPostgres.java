/**
 * 

* ClassName EMClusterPostgres.java
*
* Version information: 1.00
*
* Data: Apr 26, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.EMCluster;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class EMClusterPostgres extends EMClusterImpl{
    private static final Logger itsLogger = Logger.getLogger(EMClusterPostgres.class);



    /* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.trainer.EMCluster.EMClusterImpl#emTrain(java.sql.Connection, java.sql.Statement, java.lang.String, int, double, int, java.util.List, int, com.alpine.datamining.operator.EMCluster.EMModel)
	 */
	@Override
	public ArrayList<Double> emTrain(Connection connection, Statement st,
			String tableName, int maxIterationNumber, double epsilon,
			int clusterNumber, List<String> columnNames, int initClusterSize,
			EMModel trainModel) throws SQLException {

		ResultSet rs = null;
		StringBuffer columnArray = new StringBuffer();
		ArrayList<Double> tempResult=new ArrayList<Double>(clusterNumber+2*clusterNumber*columnNames.size());
		Iterator<String> columnIter = columnNames.iterator();
		long timeStamp = System.currentTimeMillis();
		Object[] outputResult = new Object[clusterNumber+2*clusterNumber*columnNames.size()];
		StringBuffer stddevsql=new StringBuffer();
		Double[] sigma =new Double[columnNames.size()*clusterNumber];
		stddevsql.append("select ");
		int flag=0;
		columnArray.append("array[");
		while (columnIter.hasNext()) {
			String column = columnIter.next();
			columnArray.append("'" + StringHandler.doubleQ(column) + "',");
			if(flag==0){
				stddevsql
				.append("sqrt(stddev(")
				.append(StringHandler.doubleQ(column))
				.append("))");
			}
			else{
				stddevsql
				.append(",sqrt(stddev(")
				.append(StringHandler.doubleQ(column))
				.append("))");
			}
			flag=1;
		}
		columnArray.deleteCharAt(columnArray.length() - 1);
		columnArray.append("]");
		
		stddevsql
		.append(" from ")
		.append(StringHandler.doubleQ(tableName));
		rs = st.executeQuery(stddevsql.toString());
		while (rs.next()) {
			for(int k=0;k<clusterNumber;k++){
				for(int j=0;j<columnNames.size();j++)
				{
					sigma[k*columnNames.size()+j]=rs.getDouble(j+1);
				}
			}
		}
		
		StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select ").append(AlpineStoredProcedure.EMCLUSTER_TRAIN_STRING).append("('")
				.append(StringHandler.doubleQ(tableName)).append("',")
				.append(columnArray).append(",")
				.append(clusterNumber).append(",")
				.append(initClusterSize).append(",")
				.append(maxIterationNumber).append(",")
				.append(epsilon).append(",'")
				.append(StringHandler.doubleQ(temptablename.toString()))
				.append("',array")
				.append(Arrays.toString(sigma))
				.append(")");
		itsLogger.debug("EMClusterPostgres.emTrain():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		while (rs.next())
			{
			outputResult=(Object[])rs.getArray(1).getArray();
			}
		 for (int i=0;i<outputResult.length;i++)
		 {
			 tempResult.add( (Double) outputResult[i]);
		 }
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			itsLogger.debug(e.toString());
			throw new SQLException(e.getLocalizedMessage());
		}
		return tempResult;
	}
 

}
