package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.datamining.api.impl.hadoop.predictor.HadoopDecisionTreePredictor;
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

public class DTEvaluatorAdapter extends AbstractEvaluatorAdapter implements EvaluatorAdapter{

	private AnalyticContext context =null; 

	private String operatorName;
	public DTEvaluatorAdapter(AnalyticContext context,String operatorName){
		this.context=context;
		this.operatorName=operatorName;
	}
	HadoopLiftDataGeneratorRunner liftDataGenerator = new HadoopLiftDataGeneratorRunner(context,operatorName);
	HadoopRocDataGeneratorRunner rocDataGenerator = new HadoopRocDataGeneratorRunner(context,operatorName);
	HadoopGoodnessOfFitGeneratorRunner gofDataGenerator = new HadoopGoodnessOfFitGeneratorRunner(context,operatorName);
	HadoopDecisionTreePredictor predictor = new HadoopDecisionTreePredictor();
	@Override
	public DoubleListAndDoubleData generateROCData(EngineModel eModel,
			AnalyticSource source) throws Exception {
		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();
		String goodValue=config.getColumnValue();
		String tmpPath = VariableModelUtility.replaceHadoopTmpPath(((HadoopAnalyticSource)source).getVariableMap());

		String tmpPredictDir = tmpPath + "dtROC" + System.currentTimeMillis();
		String tempFileName = "predictor";
		
		HadoopPredictorConfig predictConf = new HadoopPredictorConfig();
		predictor.setContext(context);
		predictConf.setTrainedModel(eModel);
		predictConf.setOverride(Resources.YesOpt);
		predictConf.setResultsLocation(tmpPredictDir);
		predictConf.setResultsName(tempFileName);
		try {
			HadoopAnalyticSource predictSource = lightlySourceCloneWithNoConfig(source);
			HadoopMultiAnalyticFileOutPut out=predictor.doPredict(predictSource, predictConf);
			
			HadoopAnalyticSource nextSource = lightlySourceCloneWithNoConfig(source);
			nextSource.setFileName(tmpPredictDir + HadoopFile.SEPARATOR + tempFileName);
			nextSource.setHadoopFileStructureModel(out.getHadoopFileStructureModel());

			int piIndex = nextSource
					.getHadoopFileStructureModel().getColumnNameList()
					.indexOf(getValueColumnName(goodValue));
			if(piIndex==-1){
				throw new Exception("No \""+goodValue+"\" in Dependent Column");
			}

			nextSource.setAnalyticConfiguration(config);
			rocDataGenerator.setAnalyticContext(context);
			DoubleListAndDoubleData resutl = rocDataGenerator.createROCData(nextSource, eModel, piIndex + "",goodValue, out.getHadoopFileStructureModel());

			setLocalMode(rocDataGenerator.isLocalMode()) ;
			return resutl ;
		} catch (Exception e) {
			throw e;
		}
		finally{
			dropIfExists(tmpPredictDir,((HadoopAnalyticSource) source).getHadoopInfo());
		}
	}

	@Override
	public DoubleListData generateLiftData(EngineModel eModel,
			AnalyticSource source) throws Exception {
		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();
		String goodValue=config.getColumnValue();
		String tmpPath = VariableModelUtility.replaceHadoopTmpPath(((HadoopAnalyticSource)source).getVariableMap());
		String tmpPredictFile = tmpPath+"dtROC" + System.currentTimeMillis();
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

			int piIndex = nextSource
					.getHadoopFileStructureModel()
					.getColumnNameList()
					.indexOf(
							getValueColumnName(goodValue));

			if(piIndex==-1){
				throw new Exception("No \""+goodValue+"\" in Dependent Column");
			}
			
			nextSource.setAnalyticConfiguration(config);
			liftDataGenerator.setAnalyticContext(context);
			DoubleListData resutl = liftDataGenerator.createLiftData(nextSource, eModel, piIndex + "",goodValue, out.getHadoopFileStructureModel());
			setLocalMode(liftDataGenerator.isLocalMode()) ;
			return resutl ;
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
		String tmpPredictFile = tmpPath+"dtGof" + System.currentTimeMillis();
		String tmpFileName = "predictor";
		HadoopPredictorConfig predictConf = new HadoopPredictorConfig();
		predictConf.setTrainedModel(eModel);
		predictConf.setOverride(Resources.YesOpt);
		predictConf.setResultsLocation(tmpPredictFile);
		predictConf.setResultsName("predictor");
		predictor.setContext(context);
		try {
			HadoopAnalyticSource predictSource = lightlySourceCloneWithNoConfig(source);
			HadoopMultiAnalyticFileOutPut out=predictor.doPredict(predictSource, predictConf);
			
			HadoopAnalyticSource nextSource = lightlySourceCloneWithNoConfig(source);
			nextSource.setFileName(tmpPredictFile + HadoopFile.SEPARATOR + tmpFileName);
			nextSource.setHadoopFileStructureModel(out.getHadoopFileStructureModel());
			
			gofDataGenerator.dependValues.clear();
			StringBuffer cIndexes=new StringBuffer();
			boolean first=true;
			for(String value:((DecisionTreeHadoopModel) eModel.getModel()).getLeafValues())
			{
				gofDataGenerator.dependValues.add(value);
				if(first){
					first=false;
				}
				else{
					cIndexes.append(",");
				}
				cIndexes.append(((HadoopAnalyticSource) nextSource)
						.getHadoopFileStructureModel()
						.getColumnNameList()
						.indexOf(getValueColumnName(value)));
				
			}
			gofDataGenerator.setcIndexes(cIndexes.toString());
 
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
	// if a value contains more than 1 ","
	private String getValueColumnName(String Value) {
		return  AbstractHadoopAnalyzer.PREDICTION_NAME_C+AbstractHadoopAnalyzer.PREDICT_SEP_CHAR
				+ Value.replaceAll(",", "_");
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
	public void stop() {
		predictor.stop();
		liftDataGenerator.stop();
		rocDataGenerator.stop();
		gofDataGenerator.stop();
	}
}
