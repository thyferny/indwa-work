package com.alpine.datamining.api.impl.hadoop.trainer;

/**
 * 
 * ClassName HadoopLogisticRegressionTrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-9-6
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelTrainer;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopLogisticRegressionTrainRunner;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;

/**
 * @author Peter
 * 
 */

public class HadoopLogisticRegressionTrainer extends AbstractHadoopModelTrainer {

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		hadoopRunner = new HadoopLogisticRegressionTrainRunner(getContext(),getName());
		try {

			Model model = (Model) hadoopRunner.runAlgorithm(source);
			return model;
		} catch (Exception e) {
			throw new AnalysisException(e);
		}

	}

}
