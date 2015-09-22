/**
* ClassName AdaboostIMP.java
*
* Version information: 1.00
*
* Data: 9 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.trainer.adaboost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public abstract class AdaboostIMP {
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

    private static final Logger logger = Logger.getLogger(AdaboostIMP.class);

	
	
	
	public abstract void adaboostTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			Iterator<String> dependvalueIterator,DataSet dataSet)
			throws SQLException;
	public void adaboostTrainSample(String inputSchema, long timeStamp,
			String dependentColumn, Statement st, ResultSet rs,String pnewTable, Locale locale)
			throws SQLException, AnalysisException {
		try {
			int breakLoop = 0;
			int maxLoop = AlpineMinerConfig.ADABOOST_SAMPLE;
			while (breakLoop != 1 && maxLoop != 0) {
				adaboostTrainSampleOnce(inputSchema, timeStamp, st, pnewTable);
				StringBuffer sql = new StringBuffer();
				sql.append("select count(distinct ");
				sql.append(StringHandler.doubleQ(dependentColumn));
				sql.append(") from ");
				sql.append(StringHandler.doubleQ(inputSchema)).append(".");
				sql.append(StringHandler.doubleQ(sampleTable));
				sql.append(" ");
				logger.debug(sql.toString());
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
			logger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}

	}

	public abstract void adaboostTrainSampleOnce(String inputSchema, long timeStamp,
			Statement st,String pnewTable) throws SQLException;


	
	public abstract double adaboostChangePeoso(String inputSchema, String tableName,
			long timeStamp, String dependentColumn,
			String dependentColumnReplaceQ, Statement st, ResultSet rs,
			Iterator<String> sampleDvalueIterator) throws AnalysisException;
	
	public abstract void clearTrainResult(String inputSchema, long timeStamp,
			Statement st,String pnewTable) throws AnalysisException;
	public abstract void setOutputTable(PredictorConfig tempconfig); 

	
}