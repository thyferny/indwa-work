/**
 * ClassName NeuralNetworkTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayer;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.neuralnet.sequential.NNParameter;
import com.alpine.datamining.operator.neuralnet.sequential.NNTrain;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;/**
 * 
 * @author Eason
 *
 */
public class NeuralNetworkTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(NeuralNetworkTrainer.class);
	
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		NeuralNetworkConfig config = (NeuralNetworkConfig)source.getAnalyticConfig();
		try {
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource)source),config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
//			dataSet.calculateAllStatistics();
			Operator learner = OperatorUtil.createOperator(NNTrain.class);
			NNParameter parameter = new NNParameter();
			if(!StringUtil.isEmpty(config.getDecay()))
			{
				logger.debug("set \"decay\"");
//				learner.setParameter("decay", config.getDecay());
				parameter.setDecay(Boolean.parseBoolean(config.getDecay()));
				
			}
			if(!StringUtil.isEmpty(config.getError_epsilon()) )
			{
				logger.debug("set \"error_epsilon\"");
				parameter.setErrorEpsilon(Double.parseDouble(config.getError_epsilon()));
//				learner.setParameter("error_epsilon", config.getError_epsilon());
			}
			if(!StringUtil.isEmpty(config.getFetchSize()))
			{
				logger.debug("set \"fetch_size\"");
//				learner.setParameter("fetch_size", config.getFetchSize());
				parameter.setFetchSize(Integer.parseInt(config.getFetchSize()));
			}
			if(config.getHiddenLayersModel()!=null&&config.getHiddenLayersModel().getHiddenLayers()!=null
					&&!config.getHiddenLayersModel().getHiddenLayers().isEmpty())
			{	List<String[]> list= new ArrayList<String[]>();
				for(AnalysisHiddenLayer hiddenLayer:config.getHiddenLayersModel().getHiddenLayers()){
					list.add(new String[]{hiddenLayer.getLayerName(),String.valueOf(hiddenLayer.getLayerSize())});
				}
				logger.debug("set \"hidden_layers\"");
				parameter.setHiddenLayers(list);
			}
			if(!StringUtil.isEmpty(config.getLearning_rate()))
			{
				logger.debug("*set \"learning_rate\"");
//				learner.setParameter("learning_rate", config.getLearning_rate());
				parameter.setLearningRate(Double.parseDouble(config.getLearning_rate()));
			}
			if(!StringUtil.isEmpty(config.getLocal_random_seed()))
			{
				logger.debug("*set \"local_random_seed\"");
//				learner.setParameter("local_random_seed", config.getLocal_random_seed());
				parameter.setRandomSeed(Integer.parseInt(config.getLocal_random_seed()));
			}
			if(!StringUtil.isEmpty(config.getMomentum()))
			{
				logger.debug("set \"momentum\"");
//				learner.setParameter("momentum", config.getMomentum());
				parameter.setMomentum(Double.parseDouble(config.getMomentum()));
			}
			if(!StringUtil.isEmpty(config.getNormalize()))
			{
				logger.debug("set \"normalize\"");
//				learner.setParameter("normalize", config.getNormalize());
				parameter.setNormalize(Boolean.parseBoolean(config.getNormalize()));
			}
			if(!StringUtil.isEmpty(config.getTraining_cycles()))
			{
				logger.debug("set \"training_cycles\"");
//				learner.setParameter("training_cycles", config.getTraining_cycles());
				parameter.setTrainingIteration(Integer.parseInt(config.getTraining_cycles()));
			}
//			if(!StringUtil.isEmpty(config.getColumnNames()))
//			{
//				logger.debug("set \"column_names\"");
////				learner.setParameter("column_names", config.getColumnNames());
//				parameter.setColumnNames(config.getColumnNames());
//			}
			if(!StringUtil.isEmpty(config.getAdjust_per()))
			{
				logger.debug("set \"adjust_per_row\"");
				if(config.getAdjust_per().equalsIgnoreCase("row"))
				{
//					learner.setParameter("adjust_per_row", "true");
					parameter.setAdjustPerRow(true);
				}
				else
				{
//					learner.setParameter("adjust_per_row", "false");
					parameter.setAdjustPerRow(false);
				}
			}
			warnTooManyValue(dataSet,Integer.parseInt(AlpineMinerConfig.C2N_WARNING),config.getLocale());
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

 
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((NeuralNetworkConfig)config).getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NEURALNETWORK_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NEURALNETWORK_TRAIN_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}


}
