/**
 * ClassName WOETableGenerator.java
 *
 * Version information: 1.00
 *
 * Data: 30 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.predictor;

import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQL;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSqlFactory;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.datamining.operator.woe.WOEModel;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class WOETableGenerator extends AbstractDBModelPredictor {
	private static Logger logger= Logger.getLogger(WOETableGenerator.class);
	
	DataSet dataSet;

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		PredictorConfig config = (PredictorConfig) source.getAnalyticConfig();

		AnalyticOutPut result = doPredict((DataBaseAnalyticSource) source,
				config);

		return result;
	}

	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(
			DataBaseAnalyticSource source, PredictorConfig config)
			throws AnalysisException {

		WOEGenerateTable(source, config.getTrainedModel().getModel());
		AnalyzerOutPutDataBaseUpdate result = new AnalyzerOutPutDataBaseUpdate();
		result.setDataset(dataSet);
		result.setSchemaName(config.getOutputSchema());
		result.setTableName(config.getOutputTable());

		DataBaseInfo dbInfo =   source .getDataBaseInfo();
	
		result.setDbInfo(dbInfo);
		
		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			
		return result;
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		
	 AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
	 nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.WOE_PREDICT_NAME,locale));
	 nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.WOE_PREDICT_DESCRIPTION,locale));
		 
	 return nodeMetaInfo;
	 }
	
	protected DBSource  getDataSource(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
			 {
		String schema = para.getTableInfo().getSchema();
		String tableName = para.getTableInfo().getTableName();
 
		if (schema != null && schema.trim().length() > 0) {
			schema=StringHandler.doubleQ(schema);
			tableName=StringHandler.doubleQ(tableName);
			tableName = schema + "." + tableName;
		}else
		{
			tableName=StringHandler.doubleQ(tableName);
		}
 
		DBSource  dataSource = OperatorUtil
				.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();
		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
						.getDataBaseInfo().getSystem());
		parameter.setUrl(para.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		parameter.setTableName(tableName);
		dataSource.setParameter(parameter);
		fillSpecialDataSource(dataSource, config);
		return   dataSource;
	}
	

	private void WOEGenerateTable(DataBaseAnalyticSource source, Model model)
			throws AnalysisException {
		try {
			dataSet = getDataSet(source, source.getAnalyticConfig());
			String dbType = ((DataBaseAnalyticSource) source)
					.getDataSourceType();
			WOEDataSQL dataWOE = WOEDataSqlFactory.generalWOEDataSQL(dbType);

			AnalysisWOETable tableWOEInfor = ((WOEModel) model)
					.getWOEInfoTable();
			PredictorConfig woeConfig = (PredictorConfig) source
					.getAnalyticConfig();

			String inputSchema = ((DataBaseAnalyticSource) source)
					.getTableInfo().getSchema();
			String tableName = ((DataBaseAnalyticSource) source).getTableInfo()
					.getTableName();
			String outputSchema = woeConfig.getOutputSchema();
			String outputTableName = woeConfig.getOutputTable();

			
			String dropIfExist = woeConfig.getDropIfExist();
			Connection conncetion = ((DataBaseAnalyticSource) source)
					.getConnection();
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory
			.createConnectionInfo(source.getDataSourceType());

			AnalysisStorageParameterModel analysisStorageParameterModel = null;
			String appendOnlyString = "";
			String endingString = ""; 
			analysisStorageParameterModel=woeConfig.getStorageParameters();
			if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
				appendOnlyString = " ";
			}else{
				appendOnlyString = sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel());
			}

			endingString = sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()); 

			dataWOE.createTable(source, inputSchema,
					tableName, outputSchema, outputTableName,
					dropIfExist, tableWOEInfor, conncetion, dataSet, appendOnlyString, endingString);
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}

	}

}
