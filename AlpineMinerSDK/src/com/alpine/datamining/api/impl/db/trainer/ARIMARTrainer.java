/**
 * ClassName ARIMATrainer.java
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
import com.alpine.datamining.api.impl.algoconf.ARIMAConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.timeseries.ARIMAR;
import com.alpine.datamining.operator.timeseries.ARIMARParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
/** 
 * Eason
 */

public class ARIMARTrainer extends AbstractDBModelTrainer {
	private static final Logger itsLogger = Logger.getLogger(ARIMARTrainer.class);
	protected   Model train( AnalyticSource source )
			throws AnalysisException  {
		
		try {
			ARIMAConfig config=(ARIMAConfig)source.getAnalyticConfig(); 
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
//			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			Operator learner = OperatorUtil.createOperator(ARIMAR.class);
			ARIMARParameter parameter = new ARIMARParameter();
//			if(!StringUtil.isEmpty(config.getColumnNames()))
//			{
//				learner.setParameter("column_names", ((ARIMAConfig)config).getColumnNames());
//			}
			if(!StringUtil.isEmpty(config.getP()))
			{
//				learner.setParameter("p", ((ARIMAConfig)config).getP());
				parameter.setP(Integer.parseInt(config.getP()));
			}
			if(!StringUtil.isEmpty(config.getQ()))
			{
//				learner.setParameter("q", ((ARIMAConfig)config).getQ());
				parameter.setQ(Integer.parseInt(config.getQ()));
			}
			if(!StringUtil.isEmpty(config.getD()))
			{
//				learner.setParameter("d", ((ARIMAConfig)config).getD());
				parameter.setD(Integer.parseInt(config.getD()));
			}
			if(!StringUtil.isEmpty(config.getIdColumn()))
			{
//				learner.setParameter("id_column", ((ARIMAConfig)config).getIdColumn());
				parameter.setIdColumn(config.getIdColumn());
			}
			if(!StringUtil.isEmpty(config.getValueColumn()))
			{
//				learner.setParameter("value_column", ((ARIMAConfig)config).getValueColumn());
				parameter.setValueColumn(config.getValueColumn());
			}
			if(!StringUtil.isEmpty(config.getGroupColumn()))
			{
//				learner.setParameter("value_column", ((ARIMAConfig)config).getValueColumn());
				parameter.setGroupColumn(config.getGroupColumn());
			}

//			if(!StringUtil.isEmpty(config.getCycle()))
//			{
//				learner.setParameter("cycle", ((ARIMAConfig)config).getCycle());
//			}
			if(!StringUtil.isEmpty(config.getThreshold()))
			{
//				learner.setParameter("threshold", ((ARIMAConfig)config).getThreshold());
				parameter.setThreshold(Integer.parseInt(config.getThreshold()));
			}
//			warnTooManyValue(dataSet,Integer.parseInt(AlpineMinerConfig.C2N_WARNING));
			learner.setParameter(parameter);
			Model model = ((Training) learner).train(dataSet); 
			return model;
		} 
		catch (Exception e) {
			itsLogger.error(e) ;
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
	 
			((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((ARIMAConfig)config).getDependentColumn());
	}
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_TRAIN_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

}
