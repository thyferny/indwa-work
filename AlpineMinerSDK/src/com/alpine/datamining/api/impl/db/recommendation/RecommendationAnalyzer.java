/**
 * ClassName  RecommendationAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: March 12, 2011
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.recommendation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.RandomSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.RecommendationConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.DataOperationAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

public class RecommendationAnalyzer extends DataOperationAnalyzer{
	private static Logger logger= Logger.getLogger(RecommendationAnalyzer.class);
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		DataTable dataTable = new DataTable();
		fillDataTableMetaInfo(source, dataTable);
		Statement st = null;
		ResultSet rs = null;

		try {
			RecommendationConfig config = (RecommendationConfig) source
					.getAnalyticConfig();

			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) source, source.getAnalyticConfig());
			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(databaseConnection.getProperties().getName()); 
			setOutputTable(config.getOutputTable());
			setOutputSchema(config.getOutputSchema());
			setDropIfExist(config.getDropIfExist());
			setOutputType(config.getOutputType());

			String outputTableName = getQuotaedTableName(getOutputSchema(),
					getOutputTable());
			String cohortsString = config.getCohorts();
			StringBuffer cohortsSql = getCohortsSql(cohortsString);
			String customerProductCountColumn = config.getCustomerProductCountColumn();
			if(StringUtil.isEmpty(customerProductCountColumn)){
				customerProductCountColumn = "null";
			}else{
				customerProductCountColumn = "'"+StringHandler.doubleQ(customerProductCountColumn)+"'";
			}
			String targetCohort = config.getTargetCohort();
			if(StringUtil.isEmpty(targetCohort)){
				targetCohort = "null";
			}
			ISqlGeneratorMultiDB sqlGenerator;
			AnalysisStorageParameterModel analysisStorageParameterModel = null;
			String appendOnlyString = "";
			String endingString = ""; 
			sqlGenerator = SqlGeneratorMultiDBFactory
			.createConnectionInfo(source.getDataSourceType());
			analysisStorageParameterModel=config.getStorageParameters();
			if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
				appendOnlyString = " ";
			}else{
				appendOnlyString = sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel());
			}

			endingString = sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()); 

			double epsilon = AlpineMinerConfig.RECOMMENDATION_EPSILON;
			String sql = "select alpine_miner_pr('"+
					config.getCustomerTable()+"','"+
					outputTableName+"','"+
			        config.getSelectionTable()+"','"+
			        StringHandler.doubleQ(config.getCustomerValueColumn())+"','"+
			        StringHandler.doubleQ(config.getCustomerIDColumn())+"','"+
			        StringHandler.doubleQ(config.getCustomerProductColumn())+"',"+
			        customerProductCountColumn+",'"+
			        StringHandler.doubleQ(config.getSelectionIDColumn())+"',"+
			        config.getCohortsAbove()+","+
			        config.getCohortsBelow()+","+
			        epsilon+","+
			        config.getMaxRecords()+","+
			        config.getMinProductCount()+","+
			        config.getScoreThreshold()+",'"+
			        config.getSimThreshold()+"',"+
			        cohortsSql+","+
			        targetCohort+",'"+
			        appendOnlyString+"','"+
			        endingString + 
			        "') ";
			if(dataSourceInfo.getDBType().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				sql+=" from dual";
			}

			st = databaseConnection.createStatement(false);
			dropIfExist(dataSet);
			logger.debug(
					"RecommendationAnalyzer.doAnalysis():sql=" + sql);
			st.execute(sql);
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
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
	private StringBuffer getCohortsSql(String cohortsString) {
		StringBuffer cohortsSql = new StringBuffer("'(case ");
		String[] cohortsArray = cohortsString.split(";");
		for (int i = 0; i < cohortsArray.length; i++){
			String[] temp = cohortsArray[i].split(":");
			if(temp.length == 3){
				cohortsSql.append(" when ");
				if (i == 0){
					cohortsSql.append(" value <= ").append(temp[2]);
				}else if (i == cohortsArray.length - 1){
					cohortsSql.append(" value > ").append(temp[1]);
				}else{
					cohortsSql.append(" value > ").append(temp[1]);
					cohortsSql.append(" and value <= ").append(temp[2]);
				}
				cohortsSql.append(" then ").append(temp[0]);
			}

		}
		cohortsSql.append(" else null end)'");
		return cohortsSql;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.RECOMMENDATION_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.RECOMMENDATION_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}
