/**
 * 

 * ClassName EMClusterOracle.java
 *
 * Version information: 1.00
 *
 * Data: Apr 26, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.EMCluster;

import java.sql.Array;
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
public class EMClusterOracle extends EMClusterImpl {
    private static final Logger itsLogger = Logger.getLogger(EMClusterOracle.class);

    /*
      * (non-Javadoc)
      *
      * @see
      * com.alpine.datamining.api.impl.db.trainer.EMCluster.EMClusterImpl#emTrain
      * (java.sql.Connection, java.sql.Statement, java.lang.String, int, double,
      * int, java.util.List, int,
      * com.alpine.datamining.operator.EMCluster.EMModel)
      */
	
	protected static String AlpineTempByTemp="alpine_emtemp_";
	@Override
	public ArrayList<Double> emTrain(Connection connection, Statement st,
			String tableName, int maxIterationNumber, double epsilon,
			int clusterNumber, List<String> columnNames, int initClusterSize,
			EMModel trainModel) throws SQLException {

		ResultSet rs = null;
		StringBuffer columnArray = new StringBuffer();
		ArrayList<Double> tempResult = new ArrayList<Double>(clusterNumber + 2
				* clusterNumber * columnNames.size());
		Iterator<String> columnIter = columnNames.iterator();
		long timeStamp = System.currentTimeMillis();
		StringBuffer stddevsql=new StringBuffer();
		Double[] sigma =new Double[columnNames.size()*clusterNumber];
		stddevsql.append("select ");
		int flag=0;
		columnArray.append("varchar2array(");
		while (columnIter.hasNext()) {
			String column = columnIter.next();
			columnArray.append("'" + StringHandler.doubleQ(column) + "',");
			if(flag==0){
				stddevsql
				.append("stddev(")
				.append(StringHandler.doubleQ(column))
				.append(")");
			}
			else{
				stddevsql
				.append(",stddev(")
				.append(StringHandler.doubleQ(column))
				.append(")");
			}
			flag=1;
		}
		columnArray.deleteCharAt(columnArray.length() - 1);
		columnArray.append(")");
		
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
		String sigmaString=Arrays.toString(sigma);
		sigmaString=sigmaString.substring(1, sigmaString.length()-1);
		
		StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
		StringBuffer tempbytemp=new StringBuffer(AlpineTempByTemp+timeStamp);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select ").append(AlpineStoredProcedure.EMCLUSTER_TRAIN_STRING).append("('")
				.append(StringHandler.doubleQ(tableName)).append("',")
				.append(columnArray).append(",")
				.append(clusterNumber).append(",")
				.append(initClusterSize).append(",")
				.append(maxIterationNumber).append(",")
				.append(epsilon).append(",'")
				.append(StringHandler.doubleQ(temptablename.toString())).append("','")
				.append(StringHandler.doubleQ(tempbytemp.toString()))
				.append("',floatarray(")
				.append(sigmaString)
				.append("))").append(" from dual");
		itsLogger.debug("EMClusterOracle.emTrain():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		while (rs.next()) {
			ResultSet array = ((Array) rs.getArray(1)).getResultSet();
			while (array.next()) {
				tempResult.add(array.getInt(1) - 1, array.getDouble(2));
			}
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
