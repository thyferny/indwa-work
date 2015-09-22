/**
* ClassName PLDATrainer.java
*
* Version information: 1.00
*
* Data: 2012-2-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PLDAConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.trainer.PLDA.PLDAFactory;
import com.alpine.datamining.api.impl.db.trainer.PLDA.PLDAImpl;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPLDATrainModel;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;


/**
 * @author Shawn
 *
 */
public class PLDATrainer extends AbstractDBModelTrainer{
	private static Logger logger= Logger.getLogger(PLDATrainer.class);
	
	private String dbtype = null;
	private DataSet dataSet = null;
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#createNodeMetaInfo(java.util.Locale)
	 */
	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.PLDA_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.PLDA_TRAIN_DESCRIPTION,locale));
		return nodeMetaInfo;
		
	}

	
	
	public AnalyticOutPut doAnalysis(AnalyticSource source)
	throws AnalysisException {
		AnalyzerOutPutPLDATrainModel analyzerOutPutModel=null;
		PLDAConfig config = (PLDAConfig) source
		.getAnalyticConfig();
		if (config.getTrainedModel() == null||config.getForceRetrain().equals("Yes")) {
	
			Model model = train(source);
			analyzerOutPutModel = new AnalyzerOutPutPLDATrainModel(model);
			String modelName= getName();
			analyzerOutPutModel.getEngineModel().setName(modelName);
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setComeFromRetrain(true);
 
			
		} else {// need not train the model agian, UI have the reused model
			try {
				dataSet = getDataSet(((DataBaseAnalyticSource)source),source.getAnalyticConfig());
			}catch (Exception e) {
					logger.error(e) ;
					if(e instanceof WrongUsedException){
						throw new AnalysisError(this,(WrongUsedException)e);
					} 
					else if(e instanceof AnalysisError){
						throw (AnalysisError)e;
					} 
					else{
						throw new AnalysisException(e );
					}
			}
			analyzerOutPutModel = new AnalyzerOutPutPLDATrainModel(config.getTrainedModel().getModel());
			analyzerOutPutModel.getEngineModel().setName(getName());
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setComeFromRetrain(false);
		
			
		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
		.getDataBaseInfo();
		
		analyzerOutPutModel.setPLDAWordTopicOutTable(getResultTableSampleRow(databaseConnection,
		dbInfo, config.getTopicOutSchema(), config.getTopicOutTable()));
		analyzerOutPutModel.setPLDADocTopicOutTable(getResultTableSampleRow(databaseConnection,
				dbInfo, config.getDocTopicOutSchema(), config.getDocTopicOutTable()));
		return analyzerOutPutModel;
		
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#train(com.alpine.datamining.api.AnalyticSource)
	 */
	@Override
	protected Model train(AnalyticSource analyticSource) throws AnalysisException {
		ResultSet rs = null;
		Statement st = null;
		PLDAModel trainModel = null;
		try {
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
					.createConnectionInfo(analyticSource.getDataSourceType());
			dbtype = dataSourceInfo.getDBType();
			PLDAConfig pldaConfig=(PLDAConfig) analyticSource
			.getAnalyticConfig();
			PLDAImpl pldaImpl = PLDAFactory.createPLDAAnalyzer(dbtype);


			pldaImpl.setDocTopicOutTableStorageParameters(pldaConfig.getDocTopicOutTableStorageParameters());
			pldaImpl.setPLDAModelOutputTableStorageParameters(pldaConfig.getPLDAModelOutputTableStorageParameters());
			pldaImpl.setTopicOutTableStorageParameters(pldaConfig.getTopicOutTableStorageParameters());
			dataSet = getDataSet((DataBaseAnalyticSource) analyticSource,
						analyticSource.getAnalyticConfig());
			setSpecifyColumn(dataSet, analyticSource.getAnalyticConfig());
			dataSet.computeAllColumnStatistics();
			long timeStamp = System.currentTimeMillis();
			String dicSchema=pldaConfig.getDictionarySchema();
			String dicTable=pldaConfig.getDictionaryTable();
			String dicIndexColumn=pldaConfig.getDicIndexColumn();
			String dicContentColumn=pldaConfig.getDicContentColumn();
			String contentSchema = ((DataBaseAnalyticSource) analyticSource)
			.getTableInfo().getSchema();
			String contentTable = ((DataBaseAnalyticSource) analyticSource)
			.getTableInfo().getTableName();
//			String contentSchema=pldaConfig.getContentSchema();
//			String contentTable=pldaConfig.getContentTable();
			String contentColumn = pldaConfig.getContentWordColumn();
			String contentIDColumn=pldaConfig.getContentDocIndexColumn();
			double alpha=Double.parseDouble(pldaConfig.getAlpha());
			double beta=Double.parseDouble(pldaConfig.getBeta());
			long topicnumber=Long.parseLong(pldaConfig.getTopicNumber());
			long iterationNumber=Long.parseLong(pldaConfig.getIterationNumber());
			String dropIfExists=pldaConfig.getPLDADropIfExist();
			String modelOutSchema=pldaConfig.getPLDAModelOutputSchema();
			String modelOutTable=pldaConfig.getPLDAModelOutputTable();
			
			String topicTableDropIfExists=pldaConfig.getTopicDropIfExist();
			String topicOutSchema=pldaConfig.getTopicOutSchema();
			String topicOutTable=pldaConfig.getTopicOutTable();
			
			String docTopicTableDropIfExists=pldaConfig.getDocTopicDropIfExist();
			String docTopicOutSchema=pldaConfig.getDocTopicOutSchema();
			String docTopicOutTable= pldaConfig.getDocTopicOutTable();
			Connection conncetion = null;
			trainModel=PLDAFactory.createPLDAModel(dbtype, dataSet);


			conncetion = ((DataBaseAnalyticSource) analyticSource).getConnection();

			st = conncetion.createStatement();
			pldaImpl.pldaTrain(conncetion,st,dicSchema, dicTable, dicIndexColumn, dicContentColumn, contentSchema,
					contentTable,contentColumn,contentIDColumn,timeStamp,dropIfExists,alpha,beta
					,modelOutSchema,modelOutTable, topicnumber
					, iterationNumber,topicOutSchema, topicOutTable,topicTableDropIfExists
					,docTopicTableDropIfExists,docTopicOutSchema,docTopicOutTable);
			
			trainModel.setDocIdColumn(contentIDColumn);
			trainModel.setDocContentColumn(contentColumn);
			trainModel.setDicContentColumn(dicContentColumn);
			trainModel.setDictSchema(dicSchema);
			trainModel.setDictTable(dicTable);
			trainModel.setDicIdColumn(dicIndexColumn);
			trainModel.setTopicNumber(topicnumber);
			trainModel.setAlpha(alpha);
			trainModel.setBeta(beta);
			trainModel.setModelSchema(modelOutSchema);
			trainModel.setModelTable(modelOutTable);
			trainModel.setUrl(((DataBaseAnalyticSource)analyticSource).getDataBaseInfo().getUrl());
			trainModel.setDbsystem(((DataBaseAnalyticSource)analyticSource).getDataBaseInfo().getSystem());
			trainModel.setUserName(((DataBaseAnalyticSource)analyticSource).getDataBaseInfo().getUserName());
			trainModel.setPassword(((DataBaseAnalyticSource)analyticSource).getDataBaseInfo().getPassword());
		}catch (Exception e) {
				logger.error(e);
				if (e instanceof WrongUsedException) {
					throw new AnalysisError(this, (WrongUsedException) e);
				} else if (e instanceof AnalysisError) {
					throw (AnalysisError) e;
				} else {
					throw new AnalysisException(e);
				}
			}finally{
				try {
					if(st != null)
					{
						st.close();
					}
					if(rs!=null)
					{
						rs.close();
					}
				} catch (SQLException e) {
					logger.debug(e.toString());
				throw new AnalysisException(e.getLocalizedMessage());
				}
			}

		return trainModel;
	}

}
