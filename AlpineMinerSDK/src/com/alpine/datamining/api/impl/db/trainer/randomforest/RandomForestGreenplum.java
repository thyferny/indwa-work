/**
 * 

* ClassName RandomForestGreenplum.java
*
* Version information: 1.00
*
* Date: 2012-10-9
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.randomforest;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 *  
 */

public class RandomForestGreenplum extends RandomForestIMP{

	
	
	public void randomForestTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			  DataSet dataSet)
			throws SQLException {
		this.dataSet = dataSet;
		pnewTable = "pnew" + timeStamp;
		deleteNullTable = "dn" + timeStamp;
		sampleTable = "s" + timeStamp;
		randomTable = "r" + timeStamp;
		sumPeosoTable="sp"+ timeStamp;
		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_randomforest_inittra('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(tableName));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("','");
		sql.append(StringHandler.doubleQ(dependentColumn));
//		sql.append("',");
//		sql.append(inforArray);
		sql.append("')");
		itsLogger.debug("RandomForestGreenplum.adaboostTrainInit():sql="+sql.toString());

		st.executeQuery(sql.toString());
	}

	
	
	
	
	@Override
	public void randomForestSampleOnce(String inputSchema, String timeStamp,
			Statement st, String pnewTable) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_randomforest_sample('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(pnewTable));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("',");
		sql.append(AlpineMinerConfig.ADABOOST_SAMPLE_NUMBER);
		sql.append(")");
		itsLogger.debug("RandomForestGreenplum.adaboostTrainSampleOnce():sql="+sql.toString());
		st.executeQuery(sql.toString());

	}





	@Override
	public double getOOBError(DataBaseAnalyticSource tempPredictSource,String dependColumn,String predictedLabel) throws OperatorException{
		String schema=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputSchema();
		String tableName=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputTable();
		
		
		String sql =null;
		  DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		  Statement st = null;
		  try {
		         double ResultSum=0;
		         double rowIndex=0;
		   st = databaseConnection.createStatement(false);
		   String getTypeSql= "select "+StringHandler.doubleQ(dependColumn)+" from  "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName)+" where 0=1";
			ResultSet rs = st.executeQuery(getTypeSql.toString());
			ResultSetMetaData metaData = rs.getMetaData();
 			String type=metaData.getColumnTypeName(1) ;
			if(type== "bool")
			{
				sql= "select sum(case when "+StringHandler.doubleQ(predictedLabel)+"= ( case when "+StringHandler.doubleQ(dependColumn)+"= true then 't' else 'f' end)  then 0 else 1 end),sum(1) from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);

			}else{
				sql= "select sum(case when "+StringHandler.doubleQ(predictedLabel)+"="+StringHandler.doubleQ(dependColumn)+"  then 0 else 1 end),sum(1) from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName);
			}

		   itsLogger.debug("RandomForestGreenplum.getOOBError():sql="+sql);
 
		   rs=st.executeQuery(sql);
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
	public void generateOOBTable(String inputSchema,String OOBTable,String pnewTable
			,String sampleTable, Statement st, ResultSet rs) throws SQLException {
	 
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
		.append(" as select * from  ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(pnewTable))
		.append(" except ")
		.append(" select * from ")
		.append(StringHandler.doubleQ(inputSchema))
		.append(".")
		.append(StringHandler.doubleQ(sampleTable));
		itsLogger.debug(createOOBTable.toString());
		st.execute(createOOBTable.toString());
	}





	@Override
	public double getMSE(DataBaseAnalyticSource tempPredictSource,String predictedLabel) throws OperatorException {
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
		   itsLogger.debug("RandomForestGreenplum.getMSE():sql="+sql);
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
	public double getMAPE(DataBaseAnalyticSource tempPredictSource,String dependColumn,String predictedLabel) throws OperatorException {
		String schema=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputSchema();
		String tableName=((PredictorConfig)tempPredictSource.getAnalyticConfig()).getOutputTable();
		
		String sql = "select sum(1),sum(abs(("+StringHandler.doubleQ(predictedLabel)+"-"+StringHandler.doubleQ(dependColumn)+")/"+StringHandler.doubleQ(dependColumn)+"))"+" from "+StringHandler.doubleQ(schema)+"."+StringHandler.doubleQ(tableName)+" where "+StringHandler.doubleQ(dependColumn)+"<>0";
		  DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		  Statement st = null;
		  try {
		         double absoluteSum=0;
		         double rowIndex=0;
		   st = databaseConnection.createStatement(false);
		   itsLogger.debug("RandomForestGreenplum.getMAPE():sql="+sql);
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
	public void clearTrainResult(String inputSchema,String tableName) throws SQLException {
		DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		Statement st = databaseConnection.createStatement(false);
		StringBuffer sql = new StringBuffer();
		sql.append("drop table if exists ");
		sql.append(StringHandler.doubleQ(inputSchema)).append(".");
		sql.append(StringHandler.doubleQ(tableName));
		itsLogger.debug("RandomForestGreenplum.clearTrainResult:sql="+sql);
		st.execute(sql.toString());
	}





	@Override
	public void randomForestSampleNoReplace(String inputSchema, String timeStamp,
			String dependentColumn, Statement st, ResultSet rs,
			String pnewTable, String sampleTable, Locale locale,long size) throws SQLException {
		st.execute("drop table if exists "+StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ("s"+timeStamp));
		StringBuffer executesql=new StringBuffer();
		
		long sampleSize=Math.round(size*0.632)+1;
		executesql.append("create table  ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(sampleTable))
		.append(" as select *, random() as \"alpine_miner_randomforest_r\" from  ")
		.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable))
		.append("  order by \"alpine_miner_randomforest_r\" limit ")
		.append(sampleSize);
		st.execute(executesql.toString());
		
	}

}
