/**
 * 

 * ClassName HadoopRunnerLinearRegressionTrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-8-20
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.alpine.datamining.api.impl.algoconf.HadoopLinearTrainConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.lir.LinearCombiner;
import com.alpine.hadoop.lir.LinearMapper;
import com.alpine.hadoop.lir.LinearQQMapper;
import com.alpine.hadoop.lir.LinearReducer;
import com.alpine.hadoop.lir.StatisticCombiner;
import com.alpine.hadoop.lir.StatisticMapper;
import com.alpine.hadoop.lir.StatisticReducer;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Shawn
 * 
 * 
 */

public class HadoopLinearRegressionTrainRunner extends AbstractHadoopRunner {

	// String
	// mrJarFilePath="/home/thyferny/plugins/AlpineMinerUI_1.0.0/lib/AlpineHadoopAnalytics.jar";
	private static Logger itsLogger = Logger.getLogger(HadoopLinearRegressionTrainRunner.class);
	private long objectTimeStamp=System.currentTimeMillis();
	private String betaTemp=null;
	private String statTemp=null;
	private String qqTemp=null;

	protected String resultsName;

	protected HadoopLinearTrainConfig config;
	protected String betaModelPath;
	Configuration baseConf = new Configuration();
	LinearRegressionHadoopModel resultModel = null;
	
	Map<String, String> resultMap = new HashMap<String, String>();

	private List<String> resultColumnsList=new ArrayList<String>();


	public HadoopLinearRegressionTrainRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	
	}

	@Override
	public int run(String[] args) throws Exception {

		String selectedColumnNames = config.getColumnNames();
		StringBuffer distinctColumnNames = new StringBuffer();
		StringBuffer sb=new StringBuffer();
		if (selectedColumnNames != null) {
			int flag=0;
			for (String columnName : selectedColumnNames.split(",")) {
				int id = fileStructureModel.getColumnNameList().indexOf(
						columnName);
				if (false == HadoopDataType.isNumberType(fileStructureModel
						.getColumnTypeList().get(id))) {
					if(flag==0){
						distinctColumnNames.append(columnName);
						flag=1;
					}
					else{
						distinctColumnNames.append(",").append(columnName);
					}
				}
				else{
					sb.append(columnName).append(",");
				}
			}
		}

		Map<String, String[]> distinctColumnMap = initDistinctMap(distinctColumnNames.toString());// if
														// distinctColumnNames
														// not null
		List<String> shouldReMoveList=new ArrayList<String>(); 
		for (String key : distinctColumnMap.keySet()) {
			if(distinctColumnMap.get(key).length<=1){
				shouldReMoveList.add(key);
				itsLogger.warn("column "+key+" is constant value");
			}
			else{
				if(key.equals(config.getDependentColumn())==false){
					sb.append(key).append(",");
				}
			}
		}
		distinctColumnMap.keySet().removeAll(shouldReMoveList);
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		else if(config.getInterActionModel()==null
				||config.getInterActionModel().getInterActionItems()==null
				||config.getInterActionModel().getInterActionItems().size()==0){
            throw new Exception(oneDistinctErr);
		}
		config.setColumnNames(sb.toString());
		initResultColumn(distinctColumnMap);

		callBetaJob(distinctColumnMap);// beta job

		tmpFileToDelete.add(betaTemp);
		readFileToKeyMap(betaTemp);

		String beta = trimArrayString(resultMap.get(LinearConfigureKeySet.beta));
		String coefficients = trimArrayString(resultMap.get(LinearConfigureKeySet.coefficients));
		String covariance = trimArrayString(resultMap.get(LinearConfigureKeySet.covariance));
		String dependent_avg = trimArrayString(resultMap.get(LinearConfigureKeySet.dependent_avg));

		Map<String, Double> coefficientmap = new LinkedHashMap<String, Double>();
		String[] coeffecients = coefficients.split(",");
		Double[] coeffecientData = new Double[coeffecients.length];
		int i;
		String[] columns = resultColumnsList.toArray(new String[resultColumnsList.size()]);
		for (i = 0; i < coeffecients.length; i++) {
			coeffecientData[i] = Double.parseDouble(coeffecients[i]);
		}
		for (i = 0; i < columns.length; i++) {
			coefficientmap.put(columns[i], coeffecientData[i]);
		}
		coefficientmap.put("Intercept",
				coeffecientData[coeffecientData.length - 1]);

		resultModel = new LinearRegressionHadoopModel(columns,
				config.getDependentColumn(), coeffecientData, coefficientmap);

		callStatJob(beta, coefficients, covariance, dependent_avg);// stat job
		readFileToKeyMap(statTemp);

		resultModel.setR2(Double.parseDouble(resultMap.get(LinearConfigureKeySet.r2)));
		resultModel.setS(Double.parseDouble(resultMap.get(LinearConfigureKeySet.s)));
		double[] se = stringToDoubleArray(resultMap.get(LinearConfigureKeySet.se));
		resultModel.setSe(se);
		double[] t = stringToDoubleArray(resultMap.get(LinearConfigureKeySet.t));
		resultModel.setT(t);
		double[] p = stringToDoubleArray(resultMap.get(LinearConfigureKeySet.p));
		resultModel.setP(p);
		boolean needQQJob=true;
		if(Double.isInfinite(resultModel.getR2())||Double.isNaN(resultModel.getR2())){
			needQQJob=false;
		}
		if(Integer.parseInt(resultMap.get(LinearConfigureKeySet.dof))<=0){
			needQQJob=false;
			resultModel.setS(Double.NaN);
		}
		if(needQQJob){
			callQQJob(1, 200);
			generateResual();
		}
		

		resultModel.setCharColumnMap(distinctColumnMap);
		return 0;
	}

	private void readFileToKeyMap(String dirPath) throws Exception {
		List<String> fileInfos = hdfsManager.readHadoopPathToLineList4All(dirPath,hadoopConnection);
		if(fileInfos==null||fileInfos.size()==0) {
			throw new EmptyFileException(dirPath+resultEmptyErr);
		}
		for (String line : fileInfos) {
			resultMap.put(line.split("\t")[0], line.split("\t")[1]);
		}
	}

	private void callQQJob(int totalNumber, int linenumber)
			throws Exception {
		Configuration qqConf=new Configuration(baseConf);
		super.initHadoopConfig(qqConf,HadoopConstants.JOB_NAME.LinearRegression_QQ);
		qqConf.set(LinearConfigureKeySet.columns, genereateDistinctValueString(resultColumnsList.toArray(new String[resultColumnsList.size()])));
		qqConf.set(LinearConfigureKeySet.totalnumber,Integer.toString(totalNumber));
		qqConf.set(LinearConfigureKeySet.linenumber,Integer.toString(linenumber));
		Job computerQQJob = createJob(HadoopConstants.JOB_NAME.LinearRegression_QQ, qqConf, 
				LinearQQMapper.class, null, Text.class, Text.class, inputFileFullName, qqTemp);
		super.setInputFormatClass(computerQQJob) ;
		runMapReduceJob(computerQQJob,true);
	}

	private void callStatJob(String beta,String coefficients,String covariance,String dependent_avg) throws Exception {
		Configuration statConf=new Configuration(baseConf);
		super.initHadoopConfig(statConf,HadoopConstants.JOB_NAME.LinearRegression_Staticstics);
		statConf.set(LinearConfigureKeySet.beta, beta);
		baseConf.set(LinearConfigureKeySet.beta, beta);
		statConf.set(LinearConfigureKeySet.coefficients, coefficients);
		statConf.set(LinearConfigureKeySet.covariance, covariance);
		statConf.set(LinearConfigureKeySet.dependent_avg, dependent_avg);
		Job computerStatisticsJob = createJob(HadoopConstants.JOB_NAME.LinearRegression_Staticstics, statConf, 
				StatisticMapper.class, StatisticReducer.class, Text.class, Text.class, inputFileFullName, statTemp);

		computerStatisticsJob.setCombinerClass(StatisticCombiner.class);
		super.setInputFormatClass(computerStatisticsJob) ;
		runMapReduceJob(computerStatisticsJob,true);
	}

	private void callBetaJob(Map<String, String[]> distinctColumnMap) throws Exception {
		initConf("", true,distinctColumnMap);
		Configuration betaConf=new Configuration(baseConf);
		super.initHadoopConfig(betaConf,HadoopConstants.JOB_NAME.LinearRegression_Beta);
		Job computerBetaJob = createJob(HadoopConstants.JOB_NAME.LinearRegression_Beta, betaConf, 
				LinearMapper.class, LinearReducer.class, Text.class, DoubleArrayWritable.class, inputFileFullName, betaTemp);

		computerBetaJob.setCombinerClass(LinearCombiner.class);
		super.setInputFormatClass(computerBetaJob) ;
		runMapReduceJob(computerBetaJob,true);
		badCounter=computerBetaJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
	}
	
	@Override
	public Model runAlgorithm(AnalyticSource source) throws  Exception {
		init((HadoopAnalyticSource) source);
		try {
			ToolRunner.run(this, null);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		finally{
			deleteTemp();
		}
		return resultModel;
	}

	private void initResultColumn(Map<String, String[]> distinctColumnMap) {
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
	}
	
	private void initConf(String beta, boolean firstJob,Map<String,String[]> distinctColumnMap) throws AnalysisException {
		if(firstJob){
			baseConf.set(LinearConfigureKeySet.dependent,config.getDependentColumn());
			if(config.getInterActionModel()==null){
				baseConf.set(LinearConfigureKeySet.interactionItems,"");
			}else{
				baseConf.set(LinearConfigureKeySet.interactionItems,listToString(config.getInterActionModel().getInterActionItems(), ","));
			}
			if(config.getColumnNames()!=null){
				baseConf.set(LinearConfigureKeySet.columns, config.getColumnNames());
				
				for(String columnName:config.getColumnNames().split(",")){
					String[] distinctArray = distinctColumnMap.get(columnName);
					if(distinctArray!=null&&distinctArray.length>0){
						String distincts=genereateDistinctValueString(distinctColumnMap.get(columnName));
						baseConf.set(AlpineHadoopConfKeySet.ALPINE_PREFIX+columnName, distincts);
					}
				}
			}
		}
		else{
			baseConf.set(LinearConfigureKeySet.beta, beta);
		}
	}

	protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource) ;
		betaTemp=tmpPath+"LinearBetaTemp"+objectTimeStamp;
		statTemp=tmpPath+"LinearStasticTemp"+objectTimeStamp;
		qqTemp=tmpPath+"LinearQQTemp"+objectTimeStamp;
		config = (HadoopLinearTrainConfig) hadoopSource.getAnalyticConfig();
	}
	
	public   String listToString(List<AnalysisInterActionItem> list,String seperator){
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
	
	public   double[] stringToDoubleArray(String value){
		value=trimArrayString(value);
		String[] valueArray=value.split(",");
		double[] result = new double[valueArray.length];
		for (int i = 0; i < valueArray.length; i++)
		{
			result[i] = Double.parseDouble(valueArray[i]);
		}
		return result;
	}
	
	public   String trimArrayString(String arrayString){
		if(arrayString.startsWith("[")){
			arrayString=arrayString.substring(1);
		}
		if(arrayString.endsWith("]")){
			arrayString=arrayString.substring(0,arrayString.length()-1);
		}
		return arrayString;
	}

	public boolean DoubleParseAble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private void generateResual()
			throws Exception {
		List<String> qqResult = null;
		try {
			qqResult=hdfsManager.readHadoopPathToLineList(qqTemp,hadoopConnection,200);//Integer.parseInt(ProfileReader.getInstance().getParameter(
							//ProfileUtility.UI_TABLE_LIMIT)));
			for (String tempString : qqResult) {
				if (!tempString.equals("")) {
					String[] residualValue = tempString.split(",");
					if (residualValue[0] != "" && residualValue[1] != "") {
						double[] residualData = new double[2];
						if (DoubleParseAble(residualValue[0])
								&& DoubleParseAble(residualValue[1])) {
							residualData[0] = Double
									.parseDouble(residualValue[1]);
							residualData[1] = Double
							.parseDouble(residualValue[0]) 
									- Double.parseDouble(residualValue[1]);
						} else
							continue;
						((LinearRegressionHadoopModel) resultModel)
								.addResidual(residualData);
					}
				}
			}
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		}
		finally{
			deleteTemp();
		}
	}
}
