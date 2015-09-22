/**
 * ClassName NaiveBayesTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.bayes.NaiveBayes;
import com.alpine.datamining.operator.bayes.NaiveBayesParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
/** 
 * Eason
 */

public class NaiveBayesTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(NaiveBayesTrainer.class);
	
	protected   Model train( AnalyticSource source )
			throws AnalysisException  {

		try {
			NaiveBayesConfig config=(NaiveBayesConfig)source.getAnalyticConfig(); 
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			Operator learner = OperatorUtil.createOperator(NaiveBayes.class);
			
			NaiveBayesParameter parameter = new NaiveBayesParameter();


			if (!StringUtil.isEmpty(config.getCalculateDeviance()))
			{
				parameter.setCaculateDeviance(Boolean.valueOf(config.getCalculateDeviance()));
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
	protected void setNumericalLabelCategory(Column label){
		if(label.isNumerical())
		{
			((NumericColumn)label).setCategory(true);
		}
	}
	protected   void fillSpecialDataSource(  Operator dataSource,AnalyticConfiguration config) {
	 
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((NaiveBayesConfig)config).getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NAIVE_BAYES_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NAIVE_BAYES_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
