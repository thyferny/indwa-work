/**
 * ClassName TreeTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
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
import com.alpine.datamining.api.impl.algoconf.DecisionTreeConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeParameter;
import com.alpine.datamining.operator.tree.threshold.TreeTrainer;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;/**
 * 
 * @author Jeff
 *
 */
public class DecisionTreeTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(DecisionTreeTrainer.class);
	
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		DecisionTreeConfig config = (DecisionTreeConfig)source.getAnalyticConfig();
		try {
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			Operator learner = OperatorUtil.createOperator(TreeTrainer.class);
			DecisionTreeParameter parameter = new DecisionTreeParameter();
			if(!StringUtil.isEmpty(config.getConfidence()))
			{
				logger.debug("set \"confidence\" to "+config.getConfidence());
//				learner.setParameter("confidence", config.getConfidence());
				parameter.setConfidence(Double.parseDouble(config.getConfidence()));
			}
//			if(!StringUtil.isEmpty(config.getColumnNames()))
//			{
//				logger.debug("set \"ColumnName\" to "+config.getColumnNames());
//				learner.setParameter("columnname", config.getColumnNames());		
//			}
			if(!StringUtil.isEmpty(config.getMinimal_leaf_size()))
			{
				logger.debug("set \"minimal_leaf_size\" to "+config.getMinimal_leaf_size());
//				learner.setParameter("minimal_leaf_size", config.getMinimal_leaf_size());
				parameter.setMinLeafSize(Integer.parseInt(config.getMinimal_leaf_size()));
			}

			if(!StringUtil.isEmpty(config.getMaximal_depth()))
			{
				logger.debug("set \"maximal_depth\" to "+config.getMaximal_depth());
//				learner.setParameter("maximal_depth", config.getMaximal_depth());
				parameter.setMaxDepth(Integer.parseInt(config.getMaximal_depth()));
			}
			if(!StringUtil.isEmpty(config.getMinimal_gain()))
			{
				logger.debug("set \"minimal_gain\" to "+config.getMinimal_gain());
//				learner.setParameter("minimal_gain", config.getMinimal_gain());
				parameter.setMinGain(Double.parseDouble(config.getMinimal_gain()));
			}
			if(!StringUtil.isEmpty(config.getMinimal_size_for_split()))
			{
				logger.debug("set \"minimal_size_for_split\" to "+config.getMinimal_size_for_split());
//				learner.setParameter("minimal_size_for_split", config.getMinimal_size_for_split());
				parameter.setSplitMinSize(Integer.parseInt(config.getMinimal_size_for_split()));
			}
			if(!StringUtil.isEmpty(config.getNo_pre_pruning()))
			{
				logger.debug("set \"no_pre_pruning\" to "+config.getNo_pre_pruning());
//				learner.setParameter("no_pre_pruning", config.getNo_pre_pruning());
				parameter.setNoPrePruning(Boolean.parseBoolean(config.getNo_pre_pruning()));
			}
			if(!StringUtil.isEmpty(config.getNo_pruning()))
			{
				logger.debug("set \"no_pruning\" to "+config.getNo_pruning());
//				learner.setParameter("no_pruning", config.getNo_pruning());
				parameter.setNoPruning(Boolean.parseBoolean(config.getNo_pruning()));
			}
			if(!StringUtil.isEmpty(config.getNumber_of_prepruning_alternatives()))
			{
				logger.debug("set \"number_of_prepruning_alternatives\" to "+config.getNumber_of_prepruning_alternatives());
//				learner.setParameter("number_of_prepruning_alternatives", config.getNumber_of_prepruning_alternatives());
				parameter.setPrepruningAlternativesNumber(Integer.parseInt(config.getNumber_of_prepruning_alternatives()));
			}
			if (!StringUtil.isEmpty(config.getSize_threshold_load_data()))
			{
				logger.debug("set \"size_threshold_load_data\" to "+config.getSize_threshold_load_data());
//				learner.setParameter("size_threshold_load_data", config.getSize_threshold_load_data());
				parameter.setThresholdLoadData(Integer.parseInt(config.getSize_threshold_load_data()));
			}
			if(((DecisionTreeConfig)config).getDependentColumn()==null)
			{
				AnalysisError error = new AnalysisError(this,AnalysisErrorName.DependentColumn_Empty,config.getLocale());
				logger.error(error);
				throw error;  
			}
			learner.setParameter(parameter);
			Model model = ((Training) learner).train(dataSet); 
			return model;
		} catch (Exception e) {
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
 
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {

		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((DecisionTreeConfig)config).getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.DECISION_TREE_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.DECISION_TREE_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
