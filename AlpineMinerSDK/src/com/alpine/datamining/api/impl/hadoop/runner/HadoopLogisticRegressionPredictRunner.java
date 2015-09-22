/**
 * 

* ClassName HadoopLinearRegressionPredictRunner.java
*
* Version information: 1.00
*
* Date: 2012-8-21
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop.runner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LogisticRegressionHadoopModel;
import com.alpine.datamining.operator.Model;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.LogisticConfigureKeySet;
import com.alpine.hadoop.logistic.LogisticPredictorMapper;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
/**
 * @author Peter
 *
 *  
 */

public class HadoopLogisticRegressionPredictRunner  extends AbstractHadoopRunner{
	Configuration predictConf=new Configuration();
  
	protected String resultLocaltion;
 
	protected HadoopPredictorConfig config;
	protected String outputFileFullName;

	private static Logger itsLogger = Logger
			.getLogger(HadoopLinearRegressionPredictRunner.class);
	public HadoopLogisticRegressionPredictRunner(AnalyticContext context,String operatorName){
		super(context,operatorName);
	}
	public int run(String[] args) throws Exception {
		 
		Job predictJob=new Job(predictConf);
		FileInputFormat.addInputPath(predictJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(predictJob, new Path(outputFileFullName));
		predictJob.setJobName(HadoopConstants.JOB_NAME.LogisticRegression_Predictor);
		getContext().registerMapReduceJob(predictJob) ;
		predictJob.setJarByClass(LogisticPredictorMapper.class);
		predictJob.setMapperClass(LogisticPredictorMapper.class);
		predictJob.setNumReduceTasks(0);
		predictJob.setOutputKeyClass(Text.class);
		predictJob.setOutputValueClass(Text.class);
		super.setInputFormatClass(predictJob) ;
		runMapReduceJob(predictJob,true);
		badCounter=predictJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		return 0;
	}


	private void setPredictJobValue(Model model) throws AnalysisException {
		super.initHadoopConfig(predictConf,HadoopConstants.JOB_NAME.LogisticRegression_Predictor);
		double[] beta = ((LogisticRegressionHadoopModel)model).getBeta();
		String[] columns=((LogisticRegressionHadoopModel)model).getColumnNames();
		StringBuffer columnTypes=new StringBuffer();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
		for(int i=0;i<columnTypeList.size();i++){
			if(i==0){
				columnTypes.append(columnTypeList.get(i));
			}
			else{
				columnTypes.append(",").append(columnTypeList.get(i));
			}
		}
		predictConf.set(LogisticConfigureKeySet.columns,genereateDistinctValueString(columns));
		
		Map<String, String[]> columnMap = ((LogisticRegressionHadoopModel)model).getCharColumnMap();
		if(columnMap!=null){
			for (String key : columnMap.keySet()) {
				predictConf.set(AlpineHadoopConfKeySet.ALPINE_PREFIX+key, genereateDistinctValueString(columnMap.get(key)));
			}
		}
		
		double reShapeTmp=beta[beta.length-1];
		double[] reShapeBeta=new double[beta.length];
		for(int i=1;i<beta.length;i++){
			reShapeBeta[i]=beta[i-1];
		}
		reShapeBeta[0]=reShapeTmp;
 
		predictConf.set(LogisticConfigureKeySet.beta,Arrays.toString(reShapeBeta));
		predictConf.set(LogisticConfigureKeySet.good,((LogisticRegressionHadoopModel)model).getGood());
		predictConf.set(LogisticConfigureKeySet.bad,((LogisticRegressionHadoopModel)model).getBad());
	}
	
	protected void init(HadoopAnalyticSource hadoopSource) throws Exception{
		super.init(hadoopSource) ;
		String resultLocaltion = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsLocation();
		String resultsName = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsName();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion+resultsName;
		
		setMapReduceCompress(outputFileFullName, predictConf);
	}



	@Override
	public Object runAlgorithm(AnalyticSource source) throws  Exception {
		init((HadoopAnalyticSource)source);
		HadoopPredictorConfig config=(HadoopPredictorConfig)source.getAnalyticConfig();
		Model model = config.getTrainedModel().getModel();
		 
		setPredictJobValue(model);
		 
 		String[] args =new String[1];
 
		args[0]=hadoopSource.getFileName();
		String resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		HadoopHDFSFileManager hdfsManager=HadoopHDFSFileManager.INSTANCE;
		if(hdfsManager.exists(outputFileFullName, hadoopConnection)){//true===override??
			if(Resources.YesOpt.equals(config.getOverride())){
				hdfsManager.deleteHadoopFile(outputFileFullName, hadoopConnection);
			}
			else{
				AnalysisException e = new AnalysisException("file already exist");
				itsLogger.error(e);
				throw new AnalysisException(e);
			}
		}
		try {
			ToolRunner.run(this, args);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		finally{
			deleteTemp();
		}
		return null;
	}
}
