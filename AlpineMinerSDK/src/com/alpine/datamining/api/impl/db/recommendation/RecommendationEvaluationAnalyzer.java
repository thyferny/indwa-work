/**
 * ClassName  RecommendationEvaluationAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: March 12, 2011
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.recommendation;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.RecommendationEvaluationConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.DataOperationAnalyzer;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class RecommendationEvaluationAnalyzer extends DataOperationAnalyzer{
	private static Logger logger= Logger.getLogger(RecommendationEvaluationAnalyzer.class);
	
	public AnalyticOutPut doAnalysis(AnalyticSource source)
	throws AnalysisException {

		DataTable dataTable = new DataTable();
		fillDataTableMetaInfo(source, dataTable);
		Statement st = null;
		ResultSet rs = null;

		try {
			RecommendationEvaluationConfig config = (RecommendationEvaluationConfig) source
			.getAnalyticConfig();

			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) source, source.getAnalyticConfig());
			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(databaseConnection.getProperties().getName()); 
			String sql = "";
			sql = "select alpine_miner_pr_eval('"
				+ config.getRecommendationTable()+"','"
				+ StringHandler.doubleQ(config.getRecommendationIdColumn())+"','"
				+ StringHandler.doubleQ(config.getRecommendationProductColumn())+"','"
				+ config.getPreTable()+"','"
				+ StringHandler.doubleQ(config.getPreIdColumn())+"','"
				+ StringHandler.doubleQ(config.getPreValueColumn())+"','"
				+ config.getPostTable()+"','"
				+ StringHandler.doubleQ(config.getPostIdColumn())+"','"
				+ StringHandler.doubleQ(config.getPostProductColumn())+"','"
				+ StringHandler.doubleQ(config.getPostValueColumn())
				+"')";
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				sql+=" from dual";
			}
			st = databaseConnection.createStatement(false);
			logger.debug(
					"RecommendationEvaluationAnalyzer.doAnalysis():sql=" + sql);
			rs = st.executeQuery(sql);
//			StringBuffer resultString = new StringBuffer();
			Double[] result = null;
			if(rs.next()){
				if (dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
					ArrayList<Double> b = new ArrayList<Double>(); 
					ResultSet resultSet = rs.getArray(1).getResultSet();
					while(resultSet.next()){
						b.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
					}
					if (b != null){
						result = new Double[b.size()];
						for(int i = 0; i < b.size(); i++){
							if(b.get(i) != null){
								result[i] = b.get(i).doubleValue();
							}else{
								result[i] = 0.0;
							}
						}
					}
				}else{
					Array array = rs.getArray(1);
					if (array != null){
						result = (Double[])array.getArray();
					}
				}
			}
			RecommendationEvaluationOutPut outPut = new RecommendationEvaluationOutPut(result);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return outPut;
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof AnalysisException) {
				throw (AnalysisException) e;
			} else {
				throw new AnalysisException(e);
			}
		} finally {
			try {
				if (st != null){
					st.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}
		}
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.RECOMMENDATION_EVALUATION_NAME,locale));
		nodeMetaInfo
		.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.RECOMMENDATION_EVALUATION_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}
