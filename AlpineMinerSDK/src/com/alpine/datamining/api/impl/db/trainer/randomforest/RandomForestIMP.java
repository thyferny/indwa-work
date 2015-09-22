/**
 * 

* ClassName RandomForestIMP.java
*
* Version information: 1.00
*
* Date: 2012-10-9
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.randomforest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopAggregaterAnalyzer;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 *  
 */

public abstract class RandomForestIMP {

	protected static Logger itsLogger = Logger.getLogger(RandomForestIMP.class);
	
	protected String pnewTable = null;
	protected String deleteNullTable = null;
	protected String outputTable = null;
	protected String sampleTable = null;
	protected String randomTable = null;
	protected String predictTable = null;
	protected String sumPeosoTable=null;
	protected DataSet dataSet = null;
	protected AbstractAnalyzer analyzer = null;
	protected AbstractDBModelPredictor Predictor = null;
	static String dropIfExists = "yes";
	static String alpine_adaboost_id="alpine_adaboost_id";

 
	
	
	public abstract void randomForestTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			 DataSet dataSet)
			throws SQLException;

	
	
	public void randomForestSample(String inputSchema, String string,
			String dependentColumn, Statement st, ResultSet rs,String pnewTable, String resultTable, Locale locale)
			throws SQLException, AnalysisException {
		try {
			int breakLoop = 0;
			int maxLoop = AlpineMinerConfig.ADABOOST_SAMPLE;
			while (breakLoop != 1 && maxLoop != 0) {
				randomForestSampleOnce(inputSchema, string, st, pnewTable);
				StringBuffer sql = new StringBuffer();
				sql.append("select count(distinct ");
				sql.append(StringHandler.doubleQ(dependentColumn));
				sql.append(") from ");
				sql.append(StringHandler.doubleQ(inputSchema)).append(".");
				sql.append(StringHandler.doubleQ(resultTable));
				sql.append(" ");
				itsLogger.debug(sql.toString());
				rs = st.executeQuery(sql.toString());
				while (rs.next())
					if (rs.getInt(1) > 1)
						breakLoop = 1;
				maxLoop--;
			}
			if (breakLoop != 1) {
				String e = SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_SAMPLE_FAIL,locale);

				throw new AnalysisException(e);
			}
		} catch (SQLException e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}

	}

	public abstract void randomForestSampleOnce(String inputSchema, String string,
			Statement st,String pnewTable) throws SQLException;

	public abstract void generateOOBTable(String OOBTable,String inputSchema,String pnewTable
			,String sampleTable, Statement st, ResultSet rs) throws SQLException  ;

	public abstract double getOOBError(DataBaseAnalyticSource tempPredictSource,
			String dependColumn,String predictedLabel) throws OperatorException;

	public abstract double getMSE(DataBaseAnalyticSource tempPredictSource,
			String predictedLabel) throws OperatorException;

	public abstract double getMAPE(DataBaseAnalyticSource tempPredictSource,
			String dependColumn, String predictedLabel)
			throws OperatorException;



	public abstract void clearTrainResult(String inputSchema,
			String tableName) throws SQLException;



	public abstract void randomForestSampleNoReplace(String inputSchema, String timeStamp,
			String dependentColumn, Statement st, ResultSet rs,
			String pnewTable, String sampleTable, Locale locale, long size) throws SQLException;

}
