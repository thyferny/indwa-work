/**
 * 

* ClassName HadoopLogisticRegressionPredictor.java
*
* Version information: 1.00
*
* Date: 2012-9-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.predictor;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelPredictor;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LogisticRegressionHadoopModel;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopLogisticRegressionPredictRunner;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
/**
 * @author Peter
 *
 *  
 */

public class HadoopLogisticRegressionPredictor extends AbstractHadoopModelPredictor{
	private static Logger itsLogger = Logger
			.getLogger(HadoopLogisticRegressionPredictor.class);
	@Override
	public HadoopMultiAnalyticFileOutPut doPredict(
			HadoopAnalyticSource source, HadoopPredictorConfig config)
			throws Exception {
		HadoopMultiAnalyticFileOutPut outPut=new HadoopMultiAnalyticFileOutPut();
		source.setAnalyticConfiguration(config);
		hadoopRunner= new HadoopLogisticRegressionPredictRunner(getContext(),getName());
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Hadoop Logistic Predictor Start");
		}
		hadoopRunner.runAlgorithm(source);
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Hadoop Logistic Predictor End");
		}
		String outputFolder;
		outputFolder=config.getOutputTable();
		//set real column name for visualization
		outPut.setOutputFolder(outputFolder);
		
		String resultsName = config.getResultsName();
		String resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		String fullPathFileName=resultLocaltion+resultsName;
		
		String[] outputFileNames = new String[]{resultsName};

		outPut.setOutputFileNames(outputFileNames);
		
		config.setVisualizationTypeClass(HadoopDataOperationConfig.HD_MULTIOUTPUT_VISUALIZATIONCLASS);
		
		List<String[]> outputFileSampleContents=new ArrayList<String[]>();
		
		try {
			List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(fullPathFileName, source.getHadoopInfo(),
					Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT)));
			outputFileSampleContents.add(lineList.toArray(new String[lineList.size()]));
		} catch (NumberFormatException e) {
			itsLogger.error(e);
			throw e;
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		
		outPut.setHadoopConnection(source.getHadoopInfo());
		AnalysisFileStructureModel fileStructureModel = source.getHadoopFileStructureModel();
		if(fileStructureModel!=null&&fileStructureModel.getColumnNameList()!=null&&fileStructureModel.getColumnTypeList()!=null){
			List<String> columnNameList = fileStructureModel.getColumnNameList();
			List<String> columnTypeList = fileStructureModel.getColumnTypeList();
			LogisticRegressionHadoopModel model=(LogisticRegressionHadoopModel)config.getTrainedModel().getModel();
			String predictColumn = PREDICTION_NAME_P + PREDICT_SEP_CHAR + model.getSpecifyColumn();
			
			String good=model.getGood();
			good=good.replaceAll(",", "_");
			String predictColumnGood = PREDICTION_NAME_C + PREDICT_SEP_CHAR  + good ;
			String predictColumnElse=  BAD_VALUE_DEFAULT;
			columnNameList.add(predictColumn);
			columnTypeList.add(HadoopDataType.CHARARRAY);
			columnNameList.add(predictColumnGood);
			columnTypeList.add(HadoopDataType.DOUBLE);
			columnNameList.add(predictColumnElse);
			columnTypeList.add(HadoopDataType.DOUBLE);
			AnalysisFileStructureModel newModel = createOutFileStructureModel();
			newModel.setColumnNameList(columnNameList);
			newModel.setColumnTypeList(columnTypeList);
			outPut.setHadoopFileStructureModel(newModel);
		} 
		
		outPut.setOutputFileSampleContents(outputFileSampleContents);
		return outPut;
	}



	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}



	@Override
	protected AbstractAnalyzerOutPut doPredict(HadoopPredictorConfig config)
			throws AnalysisException, Exception {
		// nothing for no write hadoop result
		return null;
	}

}