/**
*
* ClassName HadoopDecisionTreePredictRunner.java
*
* Version information: 1.00
*
* Sep 24, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/


package com.alpine.datamining.api.impl.hadoop.runner;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
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
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.mapper.DecisionTreePredictMapper;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jonathan
 *  
 */


public class HadoopDecisionTreePredictRunner extends AbstractHadoopRunner{
	Configuration predictConf=new Configuration();
	public static final String GSON_JAR = "gson-1.7.1.jar";
	protected String resultsName;
  
	protected String resultLocaltion;
	protected String outputTempName; 
	protected String outputFileFullName;
	private List<String> columnsTypeList;
	private String fullPathFileName;
	private static Logger itsLogger = Logger
			.getLogger(HadoopDecisionTreePredictRunner.class);
	public HadoopDecisionTreePredictRunner(AnalyticContext context,String operatorName){
		super(context,operatorName);
	}
	public int run(String[] args) throws Exception {
		 
		Job predictJob=new Job(getConf());
		predictJob.setJobName(HadoopConstants.JOB_NAME.Decision_Tree_Predict);
		FileInputFormat.addInputPath(predictJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(predictJob, new Path(fullPathFileName));//arg[1]decisionOutString));
		predictJob.setJarByClass(DecisionTreePredictMapper.class);
		predictJob.setMapperClass(DecisionTreePredictMapper.class);
		predictJob.setNumReduceTasks(0);
		predictJob.setOutputKeyClass(Text.class);
		predictJob.setOutputValueClass(Text.class);
		super.setInputFormatClass(predictJob) ;
		getContext().registerMapReduceJob(predictJob);
		runMapReduceJob(predictJob,true);
		badCounter=predictJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		return 0;
	}
	
	protected void init(HadoopAnalyticSource hadoopSource) throws Exception  {
		super.init(hadoopSource) ;
		inputFileFullName = hadoopSource.getFileName();
		this.columnsTypeList = fileStructureModel.getColumnTypeList();  

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
		
		super.initHadoopConfig(predictConf,HadoopConstants.JOB_NAME.Decision_Tree_Predict);
		setMapReduceCompress(outputFileFullName, predictConf);
		
		DecisionTreeHadoopModel model  = (DecisionTreeHadoopModel)config.getTrainedModel().getModel();
		
		HadoopTree tree = model.getHadoopTree();
		String modelPath = tmpPath+"PredictorTree"+System.currentTimeMillis();
		
		// serialize tree to HDFS
		FileSystem fs = FileSystem.get(URI.create(modelPath), predictConf);
		Gson gson = new GsonBuilder().serializeNulls().create();
		FSDataOutputStream out = fs.create(new Path(modelPath));
		String ser = gson.toJson(tree);
		
		IOUtils.copyBytes(new ByteArrayInputStream(ser.getBytes()), out, 4096, true);
		
		IOUtils.closeStream(out);
		
		//please mapping it back to hadooptree  
		predictConf.set(DecisionTreeConfigureKeySet.TREE_FILE, modelPath);
		String gsonJarFilePath = 	  getJarRealPath(GSON_JAR);
		StringBuffer columnTypes=new StringBuffer();
		for(int i=0;i<columnsTypeList.size();i++){
			String aType = columnsTypeList.get(i);
			if(i==0){
				columnTypes.append(aType);
			}else{
				columnTypes.append(",").append(aType);
			}
		}
		predictConf.set(DecisionTreeConfigureKeySet.COLUMN_TYPES, columnTypes.toString());
		 
 		String[] args =new String[3];
 		
 		args[0] = "-libjars";
		args[1] =  gsonJarFilePath;
 
		args[2]=hadoopSource.getFileName();
		String resultsName = config.getResultsName();
		String resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		fullPathFileName=resultLocaltion+resultsName;//real path
//		  decisionOutString = fullPathFileName ;
		//String decisionOutString=tmpPath+"DecisionPre"+System.currentTimeMillis();
//		args[3]=decisionOutString;//fullPathFileName;
		if(hdfsManager.exists(outputFileFullName, hadoopConnection)){//true===override??
			if(Resources.YesOpt.equals(config.getOverride())){
				boolean success = hdfsManager.deleteHadoopFile(outputFileFullName, hadoopConnection);
				if(success==false){
					throw new Exception("Can not delete out put directory "+fullPathFileName);
				}
			}
			else{
				AnalysisException e = new AnalysisException("file already exist");
				itsLogger.error(e);
				throw new AnalysisException(e);
			}
		}
		try {
			ToolRunner.run(predictConf, this, args);
//			HadoopHDFSFileManager hdfsFileManager=HadoopHDFSFileManager.INSTANCE;
//			List<HadoopFile> hadoopFiles=hdfsFileManager.getHadoopFiles(decisionOutString, hadoopConnection, true);
//			ArrayList<String> fileList=new ArrayList<String>();
//			for(HadoopFile hFile:hadoopFiles){
//				if(isHadoopReservedResource(hFile)==false){
//					fileList.add(hFile.getFullPath());
//				}
//			}
//			if(hdfsManager.exists(fullPathFileName, hadoopConnection)){
//				if(Resources.YesOpt.equals(config.getOverride())){
//					hdfsManager.deleteHadoopFile(fullPathFileName, hadoopConnection);
//					hdfsFileManager.mergeHadoopFiles(fileList, fullPathFileName, hadoopConnection);
//				}
//				else{
//					AnalysisException e = new AnalysisException("file already exist");
//					itsLogger.error(e);
//					throw e;
//				}
//			}
//			else{
//				hdfsFileManager.mergeHadoopFiles(fileList, fullPathFileName, hadoopConnection);
//			}
			
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		} finally {
			if(tmpFileToDelete.contains(modelPath)==false){
				tmpFileToDelete.add(modelPath);
			}
			deleteTemp();
		}
		return null;
	}
}

