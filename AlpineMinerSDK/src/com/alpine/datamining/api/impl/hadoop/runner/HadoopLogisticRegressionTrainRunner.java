/**
 * 

 * ClassName HadoopLogisticRegressionTrainRunner.java
 *
 * Version information: 1.00
 *
 * Date: 2012-9-6
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopLogisticRegressionConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LogisticRegressionHadoopModel;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.LogisticConfigureKeySet;
import com.alpine.hadoop.logistic.LogisticCombiner;
import com.alpine.hadoop.logistic.LogisticMapper;
import com.alpine.hadoop.logistic.LogisticReducer;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Peter
 * 
 * 
 */

public class HadoopLogisticRegressionTrainRunner extends AbstractHadoopRunner {

	HashMap<String, String[]> interactionColumnColumnMap = new HashMap<String, String[]>();//fake

	int maxIterationNumber;
	double epsilon;
	Configuration iteratorJobConf = new Configuration();
	HadoopLogisticRegressionConfig config = null;
	private static Logger itsLogger = Logger.getLogger(HadoopLogisticRegressionTrainRunner.class);
	private static String tmpFileFormat = null;

	public HadoopLogisticRegressionTrainRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	}
	

	LoRModelIfc resultModel = null;

	@Override
	public int run(String[] args) throws Exception {
		//never use the args because we already have the field
		String beta ="";
		long lastTimeStamp = 0;

		double lastFitness = 0;
	    
		double diff = Double.POSITIVE_INFINITY;
		double deviance = Double.NaN;
		double nullDeviance = Double.NaN;
		double chiSquare = Double.NaN;
		String variance = null;
		int iteration = 0;
		String bad="";

		String lastBeta="";
		
		String selectedColumnNames = config.getColumnNames();
		StringBuffer distinctColumnNames = new StringBuffer();
		distinctColumnNames.append(config.getDependentColumn());
		if (selectedColumnNames != null) {
			for (String columnName : selectedColumnNames.split(",")) {
				int id = fileStructureModel.getColumnNameList().indexOf(columnName);
				if (false == HadoopDataType.isNumberType(fileStructureModel.getColumnTypeList().get(id))) {
					distinctColumnNames.append(",").append(columnName);
				}
			}
		}
			
		Map<String, String[]> distinctColumnMap = initDistinctMap(distinctColumnNames.toString());
		if(distinctColumnMap.get(config.getDependentColumn())==null||distinctColumnMap.get(config.getDependentColumn()).length!=2){
			throw new Exception(logisticDependentErr);
		}
		else{
			for(String distinct:distinctColumnMap.get(config.getDependentColumn())){
				if(config.getGoodValue().equals(distinct.trim())==false){
					bad=distinct;
				}
			}
		}
	 
		for (int i = 0; i <=maxIterationNumber && diff > epsilon; i++) {
			double fitness = 0;
			lastBeta=beta;
			initConf(beta, i ,distinctColumnMap);
			long timeStamp = System.currentTimeMillis();
			lastTimeStamp = timeStamp;
			
			Job iterationJob = createJob(HadoopConstants.JOB_NAME.LogisticRegression_Iterator+"_"+i, iteratorJobConf, 
					LogisticMapper.class, LogisticReducer.class, Text.class, Text.class, inputFileFullName, tmpFileFormat+ timeStamp);

			iterationJob.setJarByClass(LogisticMapper.class);
			iterationJob.setCombinerClass(LogisticCombiner.class);
			super.setInputFormatClass(iterationJob) ;
			
			runMapReduceJob(iterationJob,true);
			if(i==0){
				badCounter=iterationJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
			}
			
			String uri = tmpFileFormat + lastTimeStamp;

			List<String> fileInfos = hdfsManager.readHadoopPathToLineList4All(uri,hadoopConnection);
			if(fileInfos==null||fileInfos.size()==0){
				throw new EmptyFileException(uri+resultEmptyErr);
			}
			Map<String, String> iterationMap = new HashMap<String, String>();
			for (String line : fileInfos) {
				iterationMap.put(line.split("\t")[0], line.split("\t")[1]);
			}
			beta = iterationMap.get(LogisticConfigureKeySet.beta);
			fitness =  Double.parseDouble(iterationMap
					.get(LogisticConfigureKeySet.fitness));
			
			diff = Math.abs(2*fitness - 2*lastFitness)/(0.1 + Math.abs(2*fitness));
			lastFitness=fitness;

			double positive = Double.parseDouble(iterationMap
					.get(LogisticConfigureKeySet.positive));
			double totalnumber = Double.parseDouble(iterationMap
					.get(LogisticConfigureKeySet.totalnumber));
			double pi0 = positive / totalnumber;
			double logLikelihood = fitness;
			double restrictedLogLikelihood = totalnumber
					* (pi0 * Math.log(pi0) + (1 - pi0) * Math.log(1 - pi0));
			deviance = -2 * logLikelihood;
			nullDeviance = -2 * restrictedLogLikelihood;
			chiSquare = nullDeviance - deviance;
			variance = iterationMap.get(LogisticConfigureKeySet.variance);
			iteration = i;
		}

		String[] betaArray;
		String[] varianceArray;
		if(diff <= epsilon){
			beta = beta.substring(1, beta.length() - 1);
			betaArray = beta.split(",");
		}
		else{
			lastBeta= lastBeta.substring(1, lastBeta.length() - 1);
			betaArray = lastBeta.split(",");
		}
		variance = variance.substring(1, variance.length() - 1);
		varianceArray = variance.split(",");
		
		double[] betaResult = new double[betaArray.length];
		double[] varianceResult = new double[varianceArray.length];

		for (int i = 0; i < betaArray.length; i++) {
			betaResult[i] = Double.parseDouble(betaArray[i]);
			varianceResult[i] = Double.parseDouble(varianceArray[i]);
		}

		double reShapeTmp=betaResult[0];
		for(int i=0;i<betaResult.length-1;i++){
			betaResult[i]=betaResult[i+1];
		}
		betaResult[betaResult.length-1]=reShapeTmp;
		
		reShapeTmp=varianceResult[0];
		for(int i=0;i<varianceResult.length-1;i++){
			varianceResult[i]=varianceResult[i+1];
		}
		varianceResult[varianceResult.length-1]=reShapeTmp;
		
		List<String> resultColumnsList = initResultColumn(distinctColumnMap);

		String[] columnsArray = new String[resultColumnsList.size()];
		for (int i = 0; i < columnsArray.length; i++) {
			columnsArray[i] = resultColumnsList.get(i).toString();
		}
		resultModel = new LogisticRegressionHadoopModel(columnsArray,
				config.getDependentColumn(), betaResult, varianceResult,
				config.getGoodValue());
		resultModel.setChiSquare(chiSquare);
		resultModel.setIteration(iteration);
		resultModel.setModelDeviance(deviance);
		resultModel.setNullDeviance(nullDeviance);
		((LogisticRegressionHadoopModel)resultModel).setBad(bad.trim());
		//dependent column's distinct value should be removed from here,only use for 2 distinct check;
		distinctColumnMap.remove(config.getDependentColumn());
		((LogisticRegressionHadoopModel)resultModel).setCharColumnMap(distinctColumnMap);

		if (diff > epsilon) {
			resultModel.setImprovementStop(false);
		} else {
			resultModel.setImprovementStop(true);
		}

		resultModel.setInteractionColumnColumnMap(interactionColumnColumnMap);
		resultModel.setAllTransformMap_valueKey(new HashMap<String, HashMap<String, String>>());
		return 0;
	}

	private List<String> initResultColumn(Map<String, String[]> distinctColumnMap) {
		List<String> resultColumnsList = new ArrayList<String>();
		if(config.getColumnNames()!=null&&"".equals(config.getColumnNames())==false){
			for(String column:config.getColumnNames().split(",")){
				if(distinctColumnMap.get(column)==null){
					resultColumnsList.add(column);
				}
				else{
					String[] distinctArray = distinctColumnMap.get(column);
					for(int i=0;i<distinctArray.length-1;i++){
						resultColumnsList.add(column+"_"+distinctArray[i].trim());
					}
				}
			}
		}
		if(config.getInterActionModel()!=null&&config.getInterActionModel().getInterActionItems()!=null){
			for(AnalysisInterActionItem item:config.getInterActionModel().getInterActionItems()){
				if("*".equals(item.getInteractionType())){
					resultColumnsList.add(item.getFirstColumn()+":"+item.getSecondColumn());
					if(resultColumnsList.contains(item.getFirstColumn())==false){
						resultColumnsList.add(item.getFirstColumn());
					}
					if(resultColumnsList.contains(item.getSecondColumn())==false){
						resultColumnsList.add(item.getSecondColumn());
					}
				}
				else{
					resultColumnsList.add(item.toString());
				}
			}
		}
		return resultColumnsList ;
	}

	private void initConf(String beta, int iteratorCount,Map<String, String[]> distinctColumnMap)
			throws AnalysisException {// beta will be changed
		if (iteratorCount == 0||iteratorCount == -1) {
			super.initHadoopConfig(iteratorJobConf,HadoopConstants.JOB_NAME.LogisticRegression_Iterator);
			iteratorJobConf.set(LogisticConfigureKeySet.dependent,config.getDependentColumn());
			iteratorJobConf.set(LogisticConfigureKeySet.good,config.getGoodValue());
			iteratorJobConf.set(LogisticConfigureKeySet.iteratorCount,String.valueOf(iteratorCount));
			if(config.getInterActionModel()==null){
				iteratorJobConf.set(LogisticConfigureKeySet.interactionItems,"");
			}else{
				iteratorJobConf.set(LogisticConfigureKeySet.interactionItems,listToString(config.getInterActionModel().getInterActionItems(), ","));
			}
			if(config.getColumnNames()!=null){
				iteratorJobConf.set(LogisticConfigureKeySet.columns,config.getColumnNames());

				for(String columnName:config.getColumnNames().split(",")){
					String[] distinctArray = distinctColumnMap.get(columnName);
					if(distinctArray!=null&&distinctArray.length>0){
						String distincts=genereateDistinctValueString(distinctColumnMap.get(columnName));
						iteratorJobConf.set(AlpineHadoopConfKeySet.ALPINE_PREFIX+columnName, distincts);
					}
				}
			}
		} else {
			iteratorJobConf.set(LogisticConfigureKeySet.beta, beta);
			iteratorJobConf.set(LogisticConfigureKeySet.iteratorCount,
					String.valueOf(iteratorCount));
		}
	}



	protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource);
		tmpFileFormat=tmpPath+"logistic";
		config = (HadoopLogisticRegressionConfig) hadoopSource
				.getAnalyticConfig();
		maxIterationNumber = Integer.parseInt(config.getMax_generations());
		epsilon = Double.parseDouble(config.getEpsilon());
	}

	@Override
	public Object runAlgorithm(AnalyticSource source) throws  Exception {
		init((HadoopAnalyticSource) source);
		try {
			ToolRunner.run(this, null);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		finally{
			deleteTemp();
		}
		return resultModel;
	}

	public static String listToString(List<AnalysisInterActionItem> list,String seperator){
		
		StringBuffer sb = new StringBuffer();
		if(list!=null&&seperator!=null){
			 for (int i = 0; i < list.size(); i++) {
				if(i>0){
					sb.append(seperator) ;
				}
				sb.append(list.get(i).toString()) ;
			}
		}
		return sb.toString();
	}
}
