/**
 * 

 * ClassName EMClusterDB2.java
 *
 * Version information: 1.00
 *
 * Data: Apr 26, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.EMCluster;
/**
 * @author Shawn
 * 
 */

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

public class EMClusterDB2 extends EMClusterImpl {
    private static final Logger itsLogger = Logger.getLogger(EMClusterDB2.class);

    CallableStatement stpCall;
	protected static String AlpineTempByTemp="alpine_tempbytemp_";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.trainer.EMCluster.EMClusterImpl#emTrain
	 * (java.sql.Connection, java.sql.Statement, java.lang.String, int, double,
	 * int, java.util.List, int,
	 * com.alpine.datamining.operator.EMCluster.EMModel)
	 */
	@Override
	public ArrayList<Double> emTrain(Connection connection, Statement st,
			String tableName, int maxIterationNumber, double epsilon,
			int clusterNumber, List<String> columnNames, int initClusterSize,
			EMModel trainModel) throws SQLException {

		ArrayList<Double> tempResult = new ArrayList<Double>(clusterNumber + 2
				* clusterNumber * columnNames.size());
		long timeStamp = System.currentTimeMillis();
		String[] columns = new String[columnNames.size()];
		StringBuffer stddevsql=new StringBuffer();
		Double[] sigma =new Double[columnNames.size()*clusterNumber];
		stddevsql.append("select ");
		int i = 0;
		for (String column : columnNames) {
			columns[i] = StringHandler.doubleQ(column);
			if(i==0){
				stddevsql
				.append("(stddev(")
				.append(columns[i])
				.append("))");
			}
			else{
				stddevsql
				.append(",(stddev(")
				.append(columns[i])
				.append("))");
			}
			i++;
		}
		stddevsql
		.append(" from ")
		.append(StringHandler.doubleQ(tableName));
		CallableStatement stpDevCall = connection.prepareCall(stddevsql.toString());
		stpDevCall.execute();
		ResultSet devRs=stpDevCall.getResultSet();
		while (devRs.next()) {
			for(int k=0;k<clusterNumber;k++){
				for(int j=0;j<columnNames.size();j++)
				{
					double tmp=devRs.getDouble(j+1);
					if(tmp>1E+4){
						sigma[k*columnNames.size()+j]=tmp*tmp;
					}
					else{
						sigma[k*columnNames.size()+j]=tmp;
					}
				}
			}
		}
		
		StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
		StringBuffer sql = new StringBuffer();
		StringBuffer tempbytemp=new StringBuffer(AlpineTempByTemp+timeStamp);
		sql.append("call ")
				.append(AlpineStoredProcedure.EMCLUSTER_TRAIN_STRING).append("('")
				.append(StringHandler.doubleQ(tableName)).append("',")
				.append("?").append(",")
				.append(clusterNumber).append(",")
				.append(initClusterSize).append(",")
				.append(maxIterationNumber).append(",")
				.append(epsilon).append(",'")
				.append(StringHandler.doubleQ(temptablename.toString())).append("','")
				.append(StringHandler.doubleQ(tempbytemp.toString())).append("',")
				.append("?").append(",")
				.append("?)");
		itsLogger.debug("EMClusterDB2.emTrain():sql=" + sql);
		

		CallableStatement stpCall = connection.prepareCall(sql.toString()); 
		java.sql.Array columnArray = connection.createArrayOf("VARCHAR",
				columns);
		java.sql.Array sigmaArray = connection.createArrayOf("DOUBLE",
				sigma);
		stpCall.setArray(1, columnArray);
		stpCall.setArray(2, sigmaArray);
		stpCall.registerOutParameter(3, java.sql.Types.ARRAY);
		stpCall.execute();
		Array arrayOut = stpCall.getArray(3);
		ResultSet rs = arrayOut.getResultSet();

		while (rs.next()) {
			tempResult.add(rs.getInt(1) - 1, rs.getDouble(2));
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
