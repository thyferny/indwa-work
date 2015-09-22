/**
 * ClassName LoREvaluatorAdapter.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LogisticRegressionHadoopModel;
import com.alpine.datamining.api.impl.hadoop.predictor.HadoopLogisticRegressionPredictor;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopGoodnessOfFitGeneratorRunner;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopLiftDataGeneratorRunner;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopRocDataGeneratorRunner;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.operator.evaluator.DoubleListAndDoubleData;
import com.alpine.datamining.operator.evaluator.DoubleListData;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.utility.common.VariableModelUtility;
import com.alpine.utility.db.Resources;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

public class LoREvaluatorAdapter extends AbstractEvaluatorAdapter implements EvaluatorAdapter {

	private AnalyticContext context =null; 

	private String operatorName;
	
	public LoREvaluatorAdapter(AnalyticContext context,String operatorName){
		this.context=context;
		this.operatorName=operatorName;
	}

	HadoopLiftDataGeneratorRunner liftDataGenerator = new HadoopLiftDataGeneratorRunner(context,operatorName);
	HadoopRocDataGeneratorRunner rocDataGenerator = new HadoopRocDataGeneratorRunner(context,operatorName);
	HadoopGoodnessOfFitGeneratorRunner gofDataGenerator = new HadoopGoodnessOfFitGeneratorRunner(context,operatorName);
	HadoopLogisticRegressionPredictor predictor = new HadoopLogisticRegressionPredictor();
	
	@Override
	public DoubleListAndDoubleData generateROCData(EngineModel eModel,
			AnalyticSource source) throws Exception {
		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();
		String goodValue=config.getColumnValue();
		String tmpPath = VariableModelUtility.replaceHadoopTmpPath(((HadoopAnalyticSource)source).getVariableMap());
		String tmpPredictFile = tmpPath +"logistROC" + System.currentTimeMillis();
		String tmpFileName = "predictor";
		predictor.setContext(context);
		HadoopPredictorConfig predictConf = new HadoopPredictorConfig();
		predictConf.setTrainedModel(eModel);
		predictConf.setOverride(Resources.YesOpt);
		predictConf.setResultsLocation(tmpPredictFile);
		predictConf.setResultsName(tmpFileName);
		try {
			HadoopAnalyticSource predictSource = lightlySourceCloneWithNoConfig(source);
			HadoopMultiAnalyticFileOutPut out=predictor.doPredict(predictSource, predictConf);
			
			HadoopAnalyticSource nextSource = lightlySourceCloneWithNoConfig(source);
			nextSource.setFileName(tmpPredictFile + HadoopFile.SEPARATOR + tmpFileName);
			nextSource.setHadoopFileStructureModel(out.getHadoopFileStructureModel());
			
			String modelGoodValue=((LogisticRegressionHadoopModel)eModel.getModel()).getGood();
			String modelGoodbad=((LogisticRegressionHadoopModel)eModel.getModel()).getBad();
			if(goodValue.equals(modelGoodValue)==false){
				if(goodValue.equals(modelGoodbad)||"Other".equalsIgnoreCase(goodValue)){
					goodValue="Other";
				}
				else{
					throw new Exception("No \""+goodValue+"\" in Dependent Column");
				}
			}
			int piIndex = nextSource
					.getHadoopFileStructureModel().getColumnNameList()
					.indexOf(getCValueColumnName(goodValue));

			nextSource.setAnalyticConfiguration(config);
			rocDataGenerator.setAnalyticContext(context);
			DoubleListAndDoubleData result =  rocDataGenerator.createROCData(nextSource, eModel, piIndex + "",config.getColumnValue(),out.getHadoopFileStructureModel());
			setLocalMode(rocDataGenerator.isLocalMode()) ;
			return result ;
		} catch (Exception e) {
			throw e;
		}
		finally{
			dropIfExists(tmpPredictFile,((HadoopAnalyticSource) source).getHadoopInfo());
		}
	}

	// if a value contains more than 1 ","
	private String getCValueColumnName(String value) {
		return  AbstractHadoopAnalyzer.PREDICTION_NAME_C+AbstractHadoopAnalyzer.PREDICT_SEP_CHAR
				+ value.replaceAll(",", "_") ;
	}
	
	public boolean dropIfExists(String fullFilePath,HadoopConnection hadoopConnection) {
		HadoopHDFSFileManager hdfsManager = HadoopHDFSFileManager.INSTANCE;
		if (hdfsManager.exists(fullFilePath, hadoopConnection)) {
			hdfsManager.deleteHadoopFile(fullFilePath, hadoopConnection);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public DoubleListData generateLiftData(EngineModel eModel,
			AnalyticSource source) throws Exception {
		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();

		String goodValue=config.getColumnValue();
		String tmpPath = VariableModelUtility.replaceHadoopTmpPath(((HadoopAnalyticSource)source).getVariableMap());
		String tmpPredictFile = tmpPath+"logistROC" + System.currentTimeMillis();
		String tmpFileName = "predictor";
		predictor.setContext(context);
		HadoopPredictorConfig predictConf = new HadoopPredictorConfig();
		predictConf.setTrainedModel(eModel);
		predictConf.setOverride(Resources.YesOpt);
		predictConf.setResultsLocation(tmpPredictFile);
		predictConf.setResultsName(tmpFileName);
		try {
			HadoopAnalyticSource predictSource = lightlySourceCloneWithNoConfig(source);
			HadoopMultiAnalyticFileOutPut out=predictor.doPredict(predictSource, predictConf);
			
			HadoopAnalyticSource nextSource = lightlySourceCloneWithNoConfig(source);
			nextSource.setFileName(tmpPredictFile + HadoopFile.SEPARATOR + tmpFileName);
			nextSource.setHadoopFileStructureModel(out.getHadoopFileStructureModel());

			String modelGoodValue=((LogisticRegressionHadoopModel)eModel.getModel()).getGood();
			String modelGoodbad=((LogisticRegressionHadoopModel)eModel.getModel()).getBad();
			if(goodValue.equals(modelGoodValue)==false){
				if(goodValue.equals(modelGoodbad)||"Other".equalsIgnoreCase(goodValue)){
					goodValue="Other";
				}
				else{
					throw new Exception("No \""+goodValue+"\" in Dependent Column");
				}
			}
			
			int piIndex = nextSource
					.getHadoopFileStructureModel()
					.getColumnNameList()
					.indexOf(
							getCValueColumnName(goodValue));

			nextSource.setAnalyticConfiguration(config);
			liftDataGenerator.setAnalyticContext(context);
			DoubleListData result = liftDataGenerator.createLiftData(nextSource, eModel, piIndex + "",config.getColumnValue(),out.getHadoopFileStructureModel());
			setLocalMode(liftDataGenerator.isLocalMode()) ;
			return result;
		} catch (Exception e) {
			throw e;
		}
		finally{
			dropIfExists(tmpPredictFile,((HadoopAnalyticSource) source).getHadoopInfo());
		}
		
	}

	@Override
	public GoodnessOfFit generateGoFData(EngineModel eModel,
			AnalyticSource source) throws Exception {

		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();
		String tmpPath = VariableModelUtility.replaceHadoopTmpPath(((HadoopAnalyticSource)source).getVariableMap());

		String tmpPredictFile = tmpPath+"logistGof" + System.currentTimeMillis();
		String tmpFileName = "predictor";
		predictor.setContext(context);
		HadoopPredictorConfig predictConf = new HadoopPredictorConfig();
		predictConf.setTrainedModel(eModel);
		predictConf.setOverride(Resources.YesOpt);
		predictConf.setResultsLocation(tmpPredictFile);
		predictConf.setResultsName("predictor");
		try {
			HadoopAnalyticSource predictSource = lightlySourceCloneWithNoConfig(source);
			HadoopMultiAnalyticFileOutPut out=predictor.doPredict(predictSource, predictConf);
			
			HadoopAnalyticSource nextSource = lightlySourceCloneWithNoConfig(source);
			nextSource.setFileName(tmpPredictFile + HadoopFile.SEPARATOR + tmpFileName);
			nextSource.setHadoopFileStructureModel(out.getHadoopFileStructureModel());
			
			String goodValue=((LogisticRegressionHadoopModel) eModel.getModel())
					.getGood();
			String badValue=((LogisticRegressionHadoopModel) eModel.getModel())
					.getBad();
			
			gofDataGenerator.dependValues.clear();
			// ------------------------for
			gofDataGenerator.dependValues
					.add(goodValue);
			gofDataGenerator.dependValues
					.add(badValue);
			// ----------------------------
			
			int cIndex1 = nextSource.getHadoopFileStructureModel()
					.getColumnNameList()
					.indexOf(getCValueColumnName(goodValue));
			int cIndex2 = nextSource.getHadoopFileStructureModel()
					.getColumnNameList()
					.indexOf(AbstractHadoopAnalyzer.BAD_VALUE_DEFAULT);
			String cIndexes = cIndex1 + "," + cIndex2;
			gofDataGenerator.setcIndexes(cIndexes);
 
			GoodnessOfFit gof = new GoodnessOfFit();
			nextSource.setAnalyticConfiguration(config);
			gof.setSourceName(eModel.getName());
			gofDataGenerator.setAnalyticContext(context);
			
			gofDataGenerator.createGoodnessOfFit(nextSource, gof,out.getHadoopFileStructureModel());
			setLocalMode(gofDataGenerator.isLocalMode()) ;
			return gof;
		} catch (Exception e) {
			throw e;
		}
		finally{
			dropIfExists(tmpPredictFile,((HadoopAnalyticSource) source).getHadoopInfo());
		}
	}

	@Override
	public void stop() {
		predictor.stop();
		liftDataGenerator.stop();
		rocDataGenerator.stop();
		gofDataGenerator.stop();
	}

}
