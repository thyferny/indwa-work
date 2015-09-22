/**
 * 

 * ClassName HadoopDecisionTreeTrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-9-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.trainer;

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopDecisionTrainConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelTrainer;
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopDecisionTreeTrainRunner;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;
import com.alpine.hadoop.tree.model.HadoopTree;

/**
 * @author Peter
 * 
 * 
 */
public class HadoopDecisionTreeTrainer extends AbstractHadoopModelTrainer{

	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		hadoopRunner= new HadoopDecisionTreeTrainRunner(getContext(),getName());
		((HadoopDecisionTreeTrainRunner)hadoopRunner).setContext(getContext());

 	//	HadoopTree hadoopTree = createFakeTree();//(HadoopTree)realTrainer.runAlgorithm(source);
	 	HadoopTree hadoopTree;
		try {
			hadoopTree = (HadoopTree)hadoopRunner.runAlgorithm(source);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}

	 	DecisionTreeHadoopModel decisionTreeHadoopModel=  toDecisionTreeHadoopModel(hadoopTree);
	 	decisionTreeHadoopModel.setDependent(((HadoopDecisionTrainConfig)source.getAnalyticConfig()).getDependentColumn());
	 	return decisionTreeHadoopModel;
	}


	private DecisionTreeHadoopModel toDecisionTreeHadoopModel(
			HadoopTree hadoopTree) {
  
		return new DecisionTreeHadoopModel(hadoopTree);
	}
 

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.DECISION_TREE_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.DECISION_TREE_TRAIN_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

}
