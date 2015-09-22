/**
 * 

* ClassName EMClusterImol.java
*
* Version information: 1.00
*
* Data: Apr 26, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.EMCluster;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
/**
 * @author Shawn
 *
 */
public abstract class EMClusterImpl {

	protected static String AlpineMinerEMClusterTable="alpine_miner_em_";
	protected static String AlpineMinerEMColumnTable="alpine_em_column_";
	protected static String AlpineMinerEMSigmaTable="alpine_em_sigma_";
	protected DatabaseConnection databaseConnection = null;
	/**
	 * @param connection
	 * @param st
	 * @param tableName
	 * @param maxIterationNumber
	 * @param epsilon
	 * @param initClusterSize 
	 * @param trainModel 
	 * @throws SQLException 
	 */
	public abstract ArrayList<Double> emTrain(Connection connection, Statement st, String tableName,
			int maxIterationNumber, double epsilon,int clusterNumber,List<String> columnNames, int initClusterSize, EMModel trainModel) throws SQLException ;

}
