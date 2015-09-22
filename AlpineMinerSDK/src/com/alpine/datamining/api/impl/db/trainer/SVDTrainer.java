/**
 * ClassName SVDTrainer
 *
 * Version information: 1.00
 *
 * Data: 2011-6-15
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.SVDConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.SVDAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.svd.SVD;
import com.alpine.datamining.operator.svd.SVDParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;
/** 
 * Eason
 */

public class SVDTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(SVDTrainer.class);
	
	private DataSet dataSet = null;
	public AnalyticOutPut doAnalysis(AnalyticSource source)
	throws AnalysisException {
		SVDAnalyzerOutPutTrainModel analyzerOutPutModel = null;
		SVDConfig config = (SVDConfig) source
		.getAnalyticConfig();
		if (config.getTrainedModel() == null||config.getForceRetrain().equals("Yes")) {
			Model model = train(source);
			analyzerOutPutModel = new SVDAnalyzerOutPutTrainModel(
					model);
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
			analyzerOutPutModel = new SVDAnalyzerOutPutTrainModel(config.getTrainedModel().getModel());
			analyzerOutPutModel.getEngineModel().setName(getName());
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setComeFromRetrain(false);

		}
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
		.getDataBaseInfo();
		analyzerOutPutModel.setUmatrixTable(getResultTableSampleRow(databaseConnection,
		dbInfo, config.getUmatrixSchema(), config.getUmatrixTable()));
		analyzerOutPutModel.setVmatrixTable(getResultTableSampleRow(databaseConnection,
				dbInfo, config.getVmatrixSchema(), config.getVmatrixTable()));
		return analyzerOutPutModel;
	}

	protected   Model train( AnalyticSource source )
	throws AnalysisException  {

		try {
			SVDConfig config=(SVDConfig)source.getAnalyticConfig(); 
			dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			Operator learner = OperatorUtil.createOperator(SVD.class);
			
			SVDParameter parameter = new SVDParameter();

			if (!StringUtil.isEmpty(config.getColName()))
			{
				parameter.setColName(config.getColName());
			}
			if (!StringUtil.isEmpty(config.getRowName()))
			{
				parameter.setRowName(config.getRowName());
			}
//			if (!StringUtil.isEmpty(config.getValue()))
//			{
//				parameter.setValue(config.getValue());
//			}
			if (!StringUtil.isEmpty(config.getNumFeatures()))
			{
				parameter.setNumFeatures(Integer.parseInt(config.getNumFeatures()));
			}
			if (!StringUtil.isEmpty(config.getOriginalStep()))
			{
				parameter.setOriginalStep(Double.parseDouble(config.getOriginalStep()));
			}
			if (!StringUtil.isEmpty(config.getSpeedupConst()))
			{
				parameter.setSpeedupConst(Double.parseDouble(config.getSpeedupConst()));
			}
			if (!StringUtil.isEmpty(config.getFastSpeedupConst()))
			{
				parameter.setFastSpeedupConst(Double.parseDouble(config.getFastSpeedupConst()));
			}
			if (!StringUtil.isEmpty(config.getSlowdownConst()))
			{
				parameter.setSlowdownConst(Double.parseDouble(config.getSlowdownConst()));
			}
			if (!StringUtil.isEmpty(config.getNumIterations()))
			{
				parameter.setNumIterations(Integer.parseInt(config.getNumIterations()));
			}
			if (!StringUtil.isEmpty(config.getMinNumIterations()))
			{
				parameter.setMinNumIterations(Integer.parseInt(config.getMinNumIterations()));
			}
			if (!StringUtil.isEmpty(config.getMinImprovement()))
			{
				parameter.setMinImprovement(Double.parseDouble(config.getMinImprovement()));
			}
			if (!StringUtil.isEmpty(config.getImprovementReached()))
			{
				if(Boolean.parseBoolean(config.getImprovementReached())){
					parameter.setImprovementReached(1);
				}else{
					parameter.setImprovementReached(0);
				}
			}
			if (!StringUtil.isEmpty(config.getInitValue()))
			{
				parameter.setInitValue(Double.parseDouble(config.getInitValue()));
			}
			if (!StringUtil.isEmpty(config.getEarlyTeminate()))
			{
				if (Boolean.parseBoolean(config.getEarlyTeminate())){
					parameter.setEarlyTeminate(1);
				}else{
					parameter.setEarlyTeminate(0);
				}
			}
			if (!StringUtil.isEmpty(config.getUmatrixSchema()) && !StringUtil.isEmpty(config.getUmatrixTable()))
			{
				parameter.setUmatrix(StringHandler.doubleQ(config.getUmatrixSchema())+"."+StringHandler.doubleQ(config.getUmatrixTable()));
			}
			if (!StringUtil.isEmpty(config.getVmatrixSchema()) && !StringUtil.isEmpty(config.getVmatrixTable()))
			{
				parameter.setVmatrix(StringHandler.doubleQ(config.getVmatrixSchema())+"."+StringHandler.doubleQ(config.getVmatrixTable()));
			}
			if (!StringUtil.isEmpty(config.getUmatrixDropIfExist()))
			{
				if (config.getUmatrixDropIfExist().trim().equalsIgnoreCase("Yes")){
					parameter.setUdrop(1);
				}else{
					parameter.setUdrop(0);
				}
			}
			if (!StringUtil.isEmpty(config.getVmatrixDropIfExist()))
			{
				if (config.getVmatrixDropIfExist().trim().equalsIgnoreCase("Yes")){
					parameter.setVdrop(1);
				}else{
					parameter.setVdrop(0);
				}
			}

			learner.setParameter(parameter);
			Model model = ((Training) learner).train(dataSet); 
			return model;
		} 
		catch (Exception e) {
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
		
	}
	protected   void fillSpecialDataSource(  Operator dataSource,AnalyticConfiguration config) {
	 
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((SVDConfig)config).getDependentColumn());
	}
	 

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SVD_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SVD_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
