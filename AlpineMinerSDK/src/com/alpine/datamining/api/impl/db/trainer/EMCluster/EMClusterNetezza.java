/**
 * 

 * ClassName EMClusterNetezza.java
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
import java.util.List;

import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 * 
 */
public class EMClusterNetezza extends EMClusterImpl {
    private static final Logger itsLogger = Logger.getLogger(EMClusterNetezza.class);

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

		ResultSet rs = null;

		ArrayList<Double> tempResult = new ArrayList<Double>(clusterNumber + 2
				* clusterNumber * columnNames.size());

		long timeStamp = System.currentTimeMillis();
		String temp = null;
		String[] outputResult = new String[clusterNumber + 2 * clusterNumber
				* columnNames.size()];
		
		
		String columntablename = AlpineMinerEMColumnTable+timeStamp;
		String sigmatablename = AlpineMinerEMSigmaTable+timeStamp;
		
		String dropifexists = "call droptable_if_existsdoubleq('"
				+ StringHandler.doubleQ(columntablename) + "')";
		st.execute(dropifexists);

		String createSQL = "create table "
				+ StringHandler.doubleQ(columntablename)
				+ "	(  valueinfo varchar(128),id integer)";
		itsLogger.debug("EMClusterNetezza.EMTrain():sql=" + createSQL);
		st.execute(createSQL);
		int i = 0;
		for (String str : columnNames) {
			i++;
			String insertSQL = "insert into "
					+ StringHandler.doubleQ(columntablename) + " values ('" + StringHandler.doubleQ(str)
					+ "'," + i + ")";
			itsLogger.debug("EMClusterNetezza.EMTrain():sql=" + insertSQL);
			st.execute(insertSQL);
		}
		
		createSQL = "create table "
				+ StringHandler.doubleQ(sigmatablename)
				+ "	(  sigma float,id integer)";
		itsLogger.debug("EMClusterNetezza.EMTrain():sql=" + createSQL);
		st.execute(createSQL);
		i = 0;
		for (String str : columnNames) {
			i++;
			String insertSQL = "insert into "
					+ StringHandler.doubleQ(sigmatablename) + " (select stddev(" + StringHandler.doubleQ(str)
					+ "),"+i+" from " + StringHandler.doubleQ(tableName) + ")";//128bit fixed
			itsLogger.debug("EMClusterNetezza.EMTrain():sql=" + insertSQL);
			st.execute(insertSQL);
		}

		StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
		StringBuffer sql = new StringBuffer();
		sql.append("call ")
				.append(AlpineStoredProcedure.EMCLUSTER_TRAIN_STRING)
				.append("('")
				.append(StringHandler.doubleQ(tableName)).append("','")
				.append(StringHandler.doubleQ(columntablename)).append("',")
				.append(clusterNumber).append(",")
				.append(initClusterSize).append(",")
				.append(maxIterationNumber).append(",")
				.append(epsilon).append(",'")
				.append(StringHandler.doubleQ(temptablename.toString()))
				.append("','")
				.append(StringHandler.doubleQ(sigmatablename))
				.append("')");
		itsLogger.debug("EMClusterNetezza.EMTrain():sql=" + sql);
		
		rs = st.executeQuery(sql.toString());
		while (rs.next()) {
			temp = rs.getString(1);
			outputResult=temp.split(",");
		}
		for (i = 0; i < outputResult.length; i++) {
			tempResult.add(Double.parseDouble(outputResult[i]));
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
