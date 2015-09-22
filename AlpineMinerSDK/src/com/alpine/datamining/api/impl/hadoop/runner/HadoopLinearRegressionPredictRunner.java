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
import com.alpine.datamining.api.impl.algoconf.HadoopLinearTrainConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.operator.Model;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.lir.LinearPredictMapper;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
/**
 * @author Shawn
 *
 *  
 */

public class HadoopLinearRegressionPredictRunner  extends AbstractHadoopRunner{
	Configuration predictConf=new Configuration();
//	protected String resultsName;
  
	protected String resultLocaltion;
	protected HadoopLinearTrainConfig config;
//	protected String outputTempName; 
	protected String outputFileFullName;
//	private String fullPathFileName;
//	private String linearOutPath;
	private static Logger itsLogger = Logger
			.getLogger(HadoopLinearRegressionPredictRunner.class);
	public HadoopLinearRegressionPredictRunner(AnalyticContext context,String operatorName){
		super(context,operatorName);
	}
	public int run(String[] args) throws Exception {
		 
		Job predictJob=new Job(predictConf);
		FileInputFormat.addInputPath(predictJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(predictJob, new Path( outputFileFullName));
		predictJob.setJobName(HadoopConstants.JOB_NAME.LinearRegression_Predictor);
		predictJob.setJarByClass(LinearPredictMapper.class);
		predictJob.setMapperClass(LinearPredictMapper.class);
		predictJob.setNumReduceTasks(0);
		predictJob.setOutputKeyClass(Text.class);
		predictJob.setOutputValueClass(Text.class);
		super.setInputFormatClass(predictJob) ;
		getContext().registerMapReduceJob(predictJob) ;
		runMapReduceJob(predictJob,true);
		badCounter=predictJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		return 0;
	}


	private void setPredictJobValue(Model model) throws AnalysisException {
		super.initHadoopConfig(predictConf,HadoopConstants.JOB_NAME.LinearRegression_Predictor);
		setMapReduceCompress(outputFileFullName, predictConf);
		LinearRegressionHadoopModel linearModel = ((LinearRegressionHadoopModel)model);
		//in extends class
		Map<String, String[]> columnMap = ((LinearRegressionHadoopModel)model).getCharColumnMap();
		
		if(columnMap!=null){
			for (String key : columnMap.keySet()) {
				predictConf.set(AlpineHadoopConfKeySet.ALPINE_PREFIX+key, genereateDistinctValueString(columnMap.get(key)));
			}
		}
		
		//the map work
		predictConf.set(LinearConfigureKeySet.columns,genereateDistinctValueString(linearModel.getColumnNames()));
		predictConf.set(LinearConfigureKeySet.beta,Arrays.toString(linearModel.getCoefficients()));
		 
	}
	
	protected void init(HadoopAnalyticSource hadoopSource) throws Exception{
		super.init(hadoopSource) ;
		String resultLocaltion = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsLocation();
		String resultsName = ((HadoopPredictorConfig)hadoopSource.getAnalyticConfig()).getResultsName();
		
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion+resultsName;
	}



	@Override
	public Object runAlgorithm(AnalyticSource source) throws  Exception {
		init((HadoopAnalyticSource)source);
		HadoopPredictorConfig config=(HadoopPredictorConfig)source.getAnalyticConfig();
		Model model = config.getTrainedModel().getModel();
		 
		setPredictJobValue(model);
		 
 		String[] args =new String[1];
 
		args[0]=hadoopSource.getFileName();
//		String resultsName = config.getResultsName();
		String resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
//		  fullPathFileName=resultLocaltion+resultsName;//real path
//		  linearOutPath=tmpPath+"LinearPre"+System.currentTimeMillis();
// 		args[1]=outputFileFullName;//fullPathFileName;
//		tmpFileToDelete.add(linearOutPath);
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
//			HadoopHDFSFileManager hdfsFileManager=HadoopHDFSFileManager.INSTANCE;
//			List<HadoopFile> hadoopFiles=hdfsFileManager.getHadoopFiles(args[1], hadoopConnection, true);
//			ArrayList<String> fileList=new ArrayList<String>();
//			//TODO if a result directory can be shown,delete..next 
//			for(HadoopFile hFile:hadoopFiles){
//				if(isHadoopReservedResource(hFile)==false){
//					fileList.add(hFile.getFullPath());
//				}
//			}
//			if(hdfsManager.exists(fullPathFileName, hadoopConnection)){
//				if(Resources.YesOpt.equals(config.getOverride())){
//					hdfsManager.deleteHadoopFile(fullPathFileName, hadoopConnection);
//					judgeNullFile(fullPathFileName, hdfsFileManager, fileList);
//				}
//				else{
//					AnalysisException e = new AnalysisException("file already exist");
//					itsLogger.error(e);
//					throw e;
//				}
//			}
//			else{
//				judgeNullFile(fullPathFileName, hdfsFileManager, fileList);
//			}
//			
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		finally{
			deleteTemp();
		}
		return null;
	}
//	private void judgeNullFile(String fullPathFileName,
//			HadoopHDFSFileManager hdfsFileManager, ArrayList<String> fileList)
//			throws Exception {
//		ArrayList<String> deleteFileList=new ArrayList<String>();
//		for(String tempPath:fileList)
//		{
//			HadoopFile  f = HadoopHDFSFileManager.INSTANCE.getHadoopFile(tempPath, hadoopConnection) ;
//		 
//			if(f.getLength()==0)
//			{
//				deleteFileList.add(tempPath);
//			}
//		}
//		fileList.removeAll(deleteFileList);
//		if(fileList.size()==0)
//		{
//			throw new Exception("No result");
//		}else
//		{
//			hdfsFileManager.mergeHadoopFiles(fileList, fullPathFileName, hadoopConnection);
//		}
//	}
}
