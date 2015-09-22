/**
 * ClassName SVMClassificationTrainer
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.SVMClassificationConfig;
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
import com.alpine.datamining.operator.svm.AbstractSVM;
import com.alpine.datamining.operator.svm.SVMClassification;
import com.alpine.datamining.operator.svm.SVMParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
/** 
 * Eason
 */

public class SVMClassificationTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(SVMClassificationTrainer.class);
	
	protected   Model train( AnalyticSource source )
			throws AnalysisException  {

		try {
			SVMClassificationConfig config=(SVMClassificationConfig)source.getAnalyticConfig(); 
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			Operator learner = OperatorUtil.createOperator(SVMClassification.class);
			
			SVMParameter parameter = new SVMParameter();

			if (!StringUtil.isEmpty(config.getKernelType())){
				if(config.getKernelType().equals(AbstractSVM.kernelTypeArray[0])){
					parameter.setKernelType(1);
				}else if(config.getKernelType().equals(AbstractSVM.kernelTypeArray[1])){
					parameter.setKernelType(2);
				}else if(config.getKernelType().equals(AbstractSVM.kernelTypeArray[2])){
					parameter.setKernelType(3);
				}
			}
			if(!StringUtil.isEmpty(config.getDegree())){
				parameter.setDegree(Integer.parseInt(config.getDegree()));
			}
			if(!StringUtil.isEmpty(config.getGamma())){
				parameter.setGamma(Double.parseDouble(config.getGamma()));
			}
			if(!StringUtil.isEmpty(config.getEta())){
				parameter.setEta(Double.parseDouble(config.getEta()));
			}
			if(!StringUtil.isEmpty(config.getNu())){
				parameter.setNu(Double.parseDouble(config.getNu()));
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
	 
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((SVMClassificationConfig)config).getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SVM_CL_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SVM_CL_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
