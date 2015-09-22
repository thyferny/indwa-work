/**
* 
* ClassName LinearRegressionHadoopTrainer.java
*
* Version information: 1.00
*
* Date: Aug 9, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package  com.alpine.datamining.api.impl.hadoop.trainer;

import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelTrainer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopLinearRegressionTrainRunner;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;

/**
 * @author Shawn,Peter
 *  
 */

public class HadoopLinearRegressionTrainer extends AbstractHadoopModelTrainer      {

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.LINEAR_REGRESSION_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.LINEAR_REGRESSION_TRAIN_DESCRIPTION, locale));

		return nodeMetaInfo;
	}
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		hadoopRunner= new HadoopLinearRegressionTrainRunner(getContext(),getName());
		Model model = null;
		try {
		  model =(Model)	hadoopRunner.runAlgorithm(source);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		//set real column name for visualization
		if(model instanceof LinearRegressionHadoopModel){
			AnalysisFileStructureModel fileStructureModel = ((HadoopAnalyticSource)source).getHadoopFileStructureModel();
			List<String> columnNameList = fileStructureModel.getColumnNameList();
			((LinearRegressionHadoopModel)model).setRealColumnNames(columnNameList);
		} 
		return model;
	}
	
}
