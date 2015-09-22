/**
 * 

* ClassName RandomForestDB2.java
*
* Version information: 1.00
*
* Date: 2012-10-23
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.randomforest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 *  
 */

public class RandomForestDB2 extends RandomForestIMP{

	Connection connection = null;

	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public void randomForestTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			DataSet dataSet) throws SQLException {this.dataSet = dataSet;
			pnewTable = "pnew" + timeStamp;
			deleteNullTable = "dn" + timeStamp;
			outputTable = "tp" + timeStamp;
			sampleTable = "s" + timeStamp;
			predictTable = "p" + timeStamp;
			randomTable = "r" + timeStamp;
			sumPeosoTable="sp"+ timeStamp;
			
			
			 
//			String[] columnsArray=inforArray.toString().split(","); 
//			Array sqlArray = connection.createArrayOf(
//					"VARCHAR", columnsArray);
			
			StringBuffer sql = new StringBuffer();
			sql.append("call alpine_miner_randomforest_inittra('");
			sql.append(StringHandler.doubleQ(inputSchema));
			sql.append("','");
			sql.append(StringHandler.doubleQ(tableName));
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("' ");
			
			sql.append(") ");
			itsLogger.debug("RandomForestDB2.randomForestTrainInit():sql="+sql.toString());
//			CallableStatement stpCall = connection
//			.prepareCall(sql.toString());

//			stpCall.setArray(1, sqlArray);
			st.execute(sql.toString());
//			stpCall.close();

	}

	@Override
	public void randomForestSampleOnce(String inputSchema, String timeStamp,
			Statement st, String pnewTable) throws SQLException {

		StringBuffer sql = new StringBuffer();
		sql.append("call alpine_miner_randomforest_sample('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(pnewTable));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("',");
		sql.append(AlpineMinerConfig.ADABOOST_SAMPLE_NUMBER);
		sql.append(")");
		itsLogger.debug("RandomForestDB2.randomForestSampleOnce():sql="+sql.toString());
		st.execute(sql.toString());
	}

	@Override
	public void generateOOBTable(String inputSchema, String OOBTable,
			String pnewTable, String sampleTable, Statement st, ResultSet rs)
			throws SQLException {
		StringBuffer dropColumnSql=new StringBuffer();
		dropColumnSql.append("alter table ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(sampleTable)) 
		.append(" drop column \"alpine_miner_randomforest_r\"");
		itsLogger.debug(dropColumnSql.toString());
		st.execute(dropColumnSql.toString());
		
		StringBuffer createOOBTable=new StringBuffer();
		createOOBTable.append("create table ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(OOBTable))
		.append(" as (select * from  ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(pnewTable))
		.append(" except ")
		.append(" select * from ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(sampleTable)).append(") definition only ") ;
		itsLogger.debug("RandomForestDB2.generateOOBTable():sql="+createOOBTable.toString());
		st.execute(createOOBTable.toString());
		StringBuffer insertOOBTable=new StringBuffer();
		insertOOBTable.append("insert into ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(OOBTable))
		.append("   (select * from  ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(pnewTable))
		.append(" except ")
		.append(" select * from ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(sampleTable)).append(")  ") ;
		itsLogger.debug("RandomForestDB2.generateOOBTable():sql="+insertOOBTable.toString());
		st.execute(insertOOBTable.toString());
	}

	@Override
	public double getOOBError(DataBaseAnalyticSource tempPredictSource,
			String dependColumn, String predictedLabel)
			throws OperatorException {
		String schema=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputSchema();
		String tableName=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputTable();
		String sql = "select sum(case when"+StringHandler.doubleQ(predictedLabel)+"="+StringHandler.doubleQ(dependColumn)+" then 0 else 1 end),sum(1) from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);
		//TODO  I have no DB2 server here  test if this work with boolean dependcolumn 
	 
				//if the up sql does not work   try  text(StringHandler.doubleQ(dependColumn))
				// if this neither works , try following:
				
//				   String getTypeSql= "select "+StringHandler.doubleQ(dependColumn)+" from  "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName)+" where 0=1";
//						ResultSet rs = st.executeQuery(getTypeSql.toString());
//						ResultSetMetaData metaData = rs.getMetaData();
//			 			String type=metaData.getColumnTypeName(1) ;
//						if(type== "bool")
//						{
//							sql= "select sum(case when "+StringHandler.doubleQ(predictedLabel)+"= ( case when "+StringHandler.doubleQ(dependColumn)+"= true then 't' else 'f' end)  then 0 else 1 end),sum(1) from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);
		//
//						}else{
//							sql= "select sum(case when "+StringHandler.doubleQ(predictedLabel)+"="+StringHandler.doubleQ(dependColumn)+"  then 0 else 1 end),sum(1) from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);
//						}
				
		DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		  Statement st = null;
		  try {
		         double ResultSum=0;
		         double rowIndex=0;
		   st = databaseConnection.createStatement(false);
		   itsLogger.debug("RandomForestDB2.performOOBLoss():sql="+sql);
		   ResultSet rs=st.executeQuery(sql);
		   while (rs.next()) {
			ResultSum=rs.getDouble(1);
		    rowIndex=rs.getDouble(2);
		   }
		   st.close();
		   return ResultSum/rowIndex;
		  } catch (SQLException e) {
		   e.printStackTrace();
		   throw new OperatorException(e.getLocalizedMessage());
		  }
	}

	@Override
	public double getMSE(DataBaseAnalyticSource tempPredictSource,
			String predictedLabel) throws OperatorException {
		String schema=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputSchema();
		String tableName=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputTable();
		
		String sql = "select sum(1),sum("+StringHandler.doubleQ(predictedLabel)+"),sum("+StringHandler.doubleQ(predictedLabel)+"*"+StringHandler.doubleQ(predictedLabel)+") from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);
		  DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		  Statement st = null;
		  try {
		         double ResultSquaredSum=0;
		         double ResultSum=0;
		         double rowIndex=0;
		   st = databaseConnection.createStatement(false);
		   itsLogger.debug("RandomForestDB2.performOOBLoss():sql="+sql);
		   ResultSet rs=st.executeQuery(sql);
		   while (rs.next()) {
		    ResultSquaredSum = rs.getDouble(3);
		    ResultSum=rs.getDouble(2);
		    rowIndex=rs.getDouble(1);
		   }
		   st.close();
		   return ResultSquaredSum/rowIndex-Math.pow(ResultSum/rowIndex, 2);
		  } catch (SQLException e) {
		   e.printStackTrace();
		   throw new OperatorException(e.getLocalizedMessage());
		  }
	}

	@Override
	public double getMAPE(DataBaseAnalyticSource tempPredictSource,
			String dependColumn, String predictedLabel)
			throws OperatorException {
		String schema=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputSchema();
		String tableName=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputTable();
		
		String sql = "select sum(1),sum(abs(("+StringHandler.doubleQ(predictedLabel)+"-"+StringHandler.doubleQ(dependColumn)+")/"+StringHandler.doubleQ(dependColumn)+"))"+" from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName)+" where "+StringHandler.doubleQ(dependColumn)+"<>0";
		  DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		  Statement st = null;
		  try {
		         double absoluteSum=0;
		         double rowIndex=0;
		   st = databaseConnection.createStatement(false);
		   itsLogger.debug("RandomForestDB2.getMAPE():sql="+sql);
		   ResultSet rs=st.executeQuery(sql);
		   while (rs.next()) {
			   absoluteSum = rs.getDouble(2);
			   rowIndex=rs.getDouble(1);
		   }
		   st.close();
		   if(rowIndex!=0){
			   return absoluteSum/rowIndex;
		   }
		   else{
			   return Double.NaN;
		   }
		  } catch (SQLException e) {
		   e.printStackTrace();
		   throw new OperatorException(e.getLocalizedMessage());
		  }
	}

	@Override
	public void clearTrainResult(String inputSchema, String tableName)
			throws SQLException {
		DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		Statement st = databaseConnection.createStatement(false);
		StringBuffer sql = new StringBuffer();
		sql.append("call PROC_DROPSCHTABLEIFEXISTS('");
		sql.append(StringHandler.doubleQ(inputSchema)).append("','");
		sql.append(StringHandler.doubleQ(tableName)).append("')");
		itsLogger.debug("RandomForestDB2.clearTrainResult:sql="+sql);
		st.execute(sql.toString());
	}
 

	@Override
	public void randomForestSampleNoReplace(String inputSchema,
			String timeStamp, String dependentColumn, Statement st,
			ResultSet rs, String pnewTable, String sampleTable, Locale locale,
			long size) throws SQLException {
		st.execute("call PROC_DROPSCHTABLEIFEXISTS('"+StringHandler.doubleQ(inputSchema)+"','"+StringHandler.doubleQ("s"+timeStamp)+"')");
		StringBuffer executesql=new StringBuffer();
		
		long sampleSize=Math.round(size*0.632)+1;
		executesql.append("create table  ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(sampleTable))
		.append(" as (select ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable)+".*, rand() as \"alpine_miner_randomforest_r\" from ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable))
		.append(" order by \"alpine_miner_randomforest_r\" fetch first ")
		.append(sampleSize+" row only) definition only");
		//TODO I have no DB2 server here  test it 
		st.execute(executesql.toString());
		
		executesql=new StringBuffer();
		executesql.append("insert into ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(sampleTable))
		.append(" (select ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable)+".*, rand() as \"alpine_miner_randomforest_r\" from ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable))
		.append(" order by \"alpine_miner_randomforest_r\" fetch first ")
		.append(sampleSize+" row only)");
		st.execute(executesql.toString());
	}

}
