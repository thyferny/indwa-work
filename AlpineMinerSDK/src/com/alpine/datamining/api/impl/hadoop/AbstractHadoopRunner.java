/**
 * 

* ClassName AbstractHadoopRunner.java
*
* Version information: 1.00
*
* Date: 2012-8-20
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapred.TaskCompletionEvent.Status;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.TransformerKeySet;
import com.alpine.hadoop.utily.transform.ColumnDistinct;
import com.alpine.hadoop.utily.transform.DistinctMapper;
import com.alpine.hadoop.utily.transform.DistinctReducer;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.common.VariableModelUtility;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
/**
 * @author Shawn
 *
 *  
 */

public abstract class AbstractHadoopRunner extends Configured implements Tool,AlpineHadoopRunner {
	protected AnalyticContext context;
	protected static String tmpPath = null;
	public AnalyticContext getContext() {
		return context;
	}

	public void setAnalyticContext(AnalyticContext context) {
		this.context = context;
	}

	protected HadoopConnection hadoopConnection;
	protected AnalysisFileStructureModel fileStructureModel;
	protected HadoopAnalyticSource hadoopSource;
	private String mrJarFilePath = null;
	protected FileFormatHelper fileFormatHelper = null;
	protected String inputFileFullName;
	//to tell the user whether it is a local mode
	private boolean localMode = false;
	private boolean stop =false;
	private String operatorName;
	private int maxReduceNumber;
	protected ClusterStatus clusterStatus = null;
	public HadoopHDFSFileManager hdfsManager = HadoopHDFSFileManager.INSTANCE;

	private static Logger itsLogger = Logger.getLogger(AbstractHadoopRunner.class);
	public static final List<String> Hadoop_Reserved_Folders = Arrays.asList(new String[]{"_logs","_temporary"}) ;
	public static final List<String> Hadoop_Reserved_Files = Arrays.asList(new String[]{"_SUCCESS"}) ;
	
	public List<String> tmpFileToDelete = new ArrayList<String>();
	protected long badCounter=0;
	
	public long getBadCounter() {
		return badCounter;
	}

	public boolean isHadoopReservedResource(HadoopFile hfile){
		return (hfile.isDir()==true&&Hadoop_Reserved_Folders.contains(hfile.getName()))
				||(hfile.isDir()==false&&Hadoop_Reserved_Files.contains(hfile.getName()));
	}
	
 

	public AbstractHadoopRunner(AnalyticContext analyticContext,String operatorName) {
		mrJarFilePath = getJarRealPath(ALPINE_MAPREDUCE_JAR);
		setAnalyticContext(analyticContext);
		this.operatorName=operatorName;
	}

	public static String getJarRealPath(String jarName) {
		try {
			File f = new File(AbstractHadoopRunner.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI());

			String path = f.getPath();

			String jarFilePath = path.replace("AlpineMinerSDK.jar", jarName);
			// note: \ does not work for pig
			jarFilePath = jarFilePath.replace('\\', '/');

			String systemName = System.getProperty("os.name");
			// special prefix for windows ...
			if (systemName.toLowerCase().startsWith("window")) {
				jarFilePath = "file:///" + jarFilePath;
			}
			return jarFilePath;
		} catch (URISyntaxException e) {
			itsLogger.error(e.getMessage(), e);
		}
		return "";
	}
	
	protected void initHadoopConfig(Configuration hadoopConf,String jobName) throws AnalysisException{
		HadoopConnection connection = hadoopSource.getHadoopInfo();
		try {
			initRunnerConf(hadoopConf, connection,fileStructureModel,hadoopSource.getFileName(),hadoopSource.getVariableMap(),jobName);
		} catch (Exception e) {
			throw new AnalysisException(e.getLocalizedMessage(),e);
		}
	}

	protected void initRunnerConf(Configuration hadoopConf,
			HadoopConnection connection, AnalysisFileStructureModel fsModel,String fileName,Map<String, String> variableMap,String jobName) throws AnalysisException, Exception {
		connection.fillSecurityInfo2Config(hadoopConf);
		
		hadoopConf.set("hadoop.job.ugi",  connection.getUserName()
				+ ","	+ connection.getGroupName());
		hadoopConf.set("fs.default.name", connection.getHDFSUrl());
		if(hdfsManager.exists(fileName, connection)==false){
			throw new AnalysisException("Input does not exist");
		}
		if(hdfsManager.getTotalFileSize(fileName, connection,false)==0){
			throw new AnalysisException("The input from the preceding operator was empty. Please check the input file or the filter condition.");
		}
		this.localMode = hdfsManager.isLocalModelNeeded(
				fileName, connection);
		if (this.localMode == false) {
			hadoopConf.set("mapred.job.tracker", connection.getJobHostName()
					+ ":"+ connection.getJobPort());
		} 

		hadoopConf.set("mapred.jar", mrJarFilePath);

		List<String> columnsTypeList = fsModel
				.getColumnTypeList();
		List<String> columnsNameList = new ArrayList<String>();
		for(String columnName:fsModel
				.getColumnNameList())
		{ 
			columnsNameList.add(columnName.replace(",", "_"));
		}
		 
		hadoopConf.set(AlpineHadoopConfKeySet.COLUMN_TYPES,ListUtility.listToString(columnsTypeList, ","));
		hadoopConf.set(AlpineHadoopConfKeySet.COLUMN_NAMES,ListUtility.listToString(columnsNameList, ","));
		
		fileFormatHelper.initHadoopConfig(hadoopConf);
		
		VariableModelUtility.replaceHadoopMapReduceVariable(variableMap, hadoopConf, getOperatorName(), jobName);
		VariableModelUtility.replaceHadoopVariable(variableMap, hadoopConf);
		
	}

 
	/**
	 * @param hadoopSource
	 * @throws Exception 
	 */
	protected void init(HadoopAnalyticSource hadoopSource) throws Exception  { 
		this.hadoopSource = hadoopSource;
		this.hadoopConnection = hadoopSource.getHadoopInfo();
		this.fileStructureModel = hadoopSource.getHadoopFileStructureModel();
		this.fileFormatHelper = createFileFormatHelper(fileStructureModel) ;
		this.inputFileFullName = hadoopSource.getFileName();
		try {
			this.clusterStatus  = HadoopConnection.getClusterInfo(hadoopConnection);
		} catch (IOException e) {
			itsLogger.debug("Cluster Status could not be obtained in  setup");//TODO
		}
	
 		if(this.clusterStatus  != null) {
		// heuristic for the optimal number f reducers based on maximum 
		// the cluster supports
 			maxReduceNumber =   this.clusterStatus.getMaxReduceTasks(); 
		}
 		tmpPath = VariableModelUtility.replaceHadoopTmpPath(hadoopSource.getVariableMap());
	}

	/**
	 * @param fileStructureModel
	 * @return
	 */
	protected FileFormatHelper createFileFormatHelper(
			AnalysisFileStructureModel fileStructureModel) {
		if(fileStructureModel instanceof AnalysisCSVFileStructureModel){
			return new CSVFileFormatHelper((AnalysisCSVFileStructureModel)fileStructureModel,hadoopSource) ;
		}else if(fileStructureModel instanceof AnalysisXMLFileStructureModel){
			return new XMLFileFormatHelper((AnalysisXMLFileStructureModel)fileStructureModel,hadoopSource) ;	
		}else if(fileStructureModel instanceof AnalysisJSONFileStructureModel){
			return new JSONFileFormatHelper((AnalysisJSONFileStructureModel)fileStructureModel,hadoopSource) ;	
		}else if(fileStructureModel instanceof AnalysisLogFileStructureModel){
			return new LogFileFormatHelper((AnalysisLogFileStructureModel)fileStructureModel,hadoopSource) ;	
		}else{
			return null;	
		} 
		
	}

	protected void setMapReduceCompress(String path,Configuration config){
		if(StringUtil.isEmpty(path)==false&&path.endsWith(".gz")){
			config.set(AlpineHadoopConfKeySet.KEY_MR_COMPRESS, Resources.TrueOpt);
			config.set(AlpineHadoopConfKeySet.KEY_MR_COMPRESS_CODE, GzipCodec.class.getName());
		}
	}
	
	/**
	 * @param job
	 */
	public void setInputFormatClass(Job job) {
		fileFormatHelper.setInputFormatClass(job) ;
		
	}

	public boolean isLocalMode() {
		return localMode;
	}

//	public void setLocalMode(boolean localMode) {
//		this.localMode = localMode;
//	}
	
	public void stop(){
		this.stop = true;
		deleteTemp();
	}
	
	public boolean isStop(){
		return this.stop;
	}

	public String getOperatorName() {
		return operatorName;
	}

	protected boolean runMapReduceJob(Job job,boolean waitforcompelet) throws Exception {
		boolean isSuccess = false;
		if (isStop() == false) {		
			isSuccess = job.waitForCompletion(waitforcompelet);
			if (isSuccess == false) {
				if (this.localMode == true) {
					throw new Exception("Job "+job.getJobName()+localJobFailedErr);
				} else {
					TaskCompletionEvent[] taskCompletionEvents = job.getTaskCompletionEvents(0);
					for (TaskCompletionEvent event : taskCompletionEvents) {
						if (event.getTaskStatus() == Status.FAILED) {
							String taskDiagnostics = getTaskInfo(
									event.getTaskAttemptId(),
									event.getTaskTrackerHttp(),
									"&filter=syslog");
							if (taskDiagnostics != null) {
								String errMsg = "";
								for (String diagnostics : taskDiagnostics.split("\n")) {
									if (diagnostics.contains("WARN")) {
										System.out.print(diagnostics);
									} else if (diagnostics.contains("java")) {
										errMsg = errMsg + diagnostics + "\n\r";
									}
								}
								if ("".equals(errMsg) == false) {
									throw new Exception(errMsg);
								}
							}
						}
					}
				}
			}
			return isSuccess;
		} else {
			return false;
		}
	}
	
	private String getTaskInfo(TaskAttemptID taskId, String baseUrl,
			String infoType) throws IOException {
		String msg = "";
		if (baseUrl != null) {
			URL taskLogUrl = new URL(baseUrl
					+ "/tasklog?plaintext=true&taskid=" + taskId + infoType);

			try {
				URLConnection connection = taskLogUrl.openConnection();
				BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String logData = null;
				while ((logData = input.readLine()) != null) {
					if (logData.length() > 0) {
						if (logData.contains("WARN") || logData.contains("java"))
							msg = msg + logData + "\n";
					}
				}
			} catch (Exception ioe) {
			}
		}
		return msg;
	}

	
	protected Map<String, String[]> initDistinctMap(String distinctColumnNames) throws Exception {
		Map<String, List<String>> distinctColumnMap = new HashMap<String, List<String>>();
		Map<String, String[]> result = new HashMap<String, String[]>();
		if (distinctColumnNames != null && distinctColumnNames.length() > 0) {
			Configuration distinctConf = new Configuration();
			String distinctResult = tmpPath + "distinct" + System.currentTimeMillis();

			initHadoopConfig(distinctConf,HadoopConstants.JOB_NAME.Distinct_Job);

			distinctConf.set(TransformerKeySet.ID_FIELDS,distinctColumnNames.toString());
			Job distinctJob = createJob(HadoopConstants.JOB_NAME.Distinct_Job, distinctConf, 
					DistinctMapper.class, DistinctReducer.class, Text.class, Text.class, inputFileFullName, distinctResult);

			distinctJob.setMapOutputKeyClass(ColumnDistinct.class);
			setInputFormatClass(distinctJob);

			runMapReduceJob(distinctJob, true);
			
			tmpFileToDelete.add(distinctResult);
			//TODO:wangpan: read line by line 
			List<String> fileInfos = hdfsManager.readHadoopPathToLineList(distinctResult,
					hadoopConnection, -1);
			//TODO:wangpan:  check the number of disticnt value to aovoid the memory issue
//			1 check the nnumber of each clolumn's distinct value 
//			2 check the total ....
			int totalSize=0;
			String[] tmpArray=null;
			for (String line : fileInfos) {
				tmpArray=line.split("\t");
				if(tmpArray==null||tmpArray.length!=2){
					continue;
				}
				String column=tmpArray[0];
				String value=tmpArray[1];
				if(distinctColumnMap.get(column)==null){
					distinctColumnMap.put(column, new ArrayList<String>());
				}
				value=value.replaceAll(":", "_");
				List<String> values=distinctColumnMap.get(column);
				values.add(value);
				totalSize+=1;
				if(totalSize>totalLimit){
					throw new Exception(tooManyTotalDistinctErr);
				}
			} 
			deleteTemp();
			tmpFileToDelete.clear();
		}
		for(String column:distinctColumnMap.keySet()){
			List<String> values=distinctColumnMap.get(column);
			if(values.size()>columnLimit){
				throw new Exception("Column:"+column+tooManyEachDistinctErr);
			}
			result.put(column, values.toArray(new String[values.size()]));
		}
		return result;
	}
	
	public void deleteTemp() {
		for (String fileName : tmpFileToDelete) {
			if (hdfsManager.exists(fileName, hadoopConnection)) {
				hdfsManager.deleteHadoopFile(fileName, hadoopConnection);
			}
		}
	}
	
	protected String genereateDistinctValueString(String[] values) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			if(values[i]!=null){
				// if valune have , need repalce it temporaly 
				sb.append(values[i].replaceAll(",", AlpineHadoopConstants.SPECIAL_SEP_STRING)).append(",");
				 
			}
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1) ;
		}
		return sb.toString();
	}
	
	protected Job createJob(String name, Configuration conf, Class map, Class reduce, 
    		Class outputKey, Class outputValue, String input, String output) throws IOException {
    	Job job = new Job(conf);
    	job.setJobName(name);
    	job.setMapperClass(map);
    	job.setOutputKeyClass(outputKey);
    	if(reduce==null){//if no reduce class set,reduce number=0 to improvement the performance
    		job.setNumReduceTasks(0);
    	}
    	else{
    		job.setReducerClass(reduce);
    	}
    	job.setOutputValueClass(outputValue);
 
       	if(input.indexOf(",")!=-1){
    		FileInputFormat.addInputPaths(job,  input);
    	}else{
    		FileInputFormat.addInputPath(job, new Path(input));
    	}
    	
    	FileOutputFormat.setOutputPath(job, new Path(output));
    	getContext().registerMapReduceJob(job);
    	if(tmpFileToDelete.contains(output)==false){
    		tmpFileToDelete.add(output);
    	}
    	return job;
    }
	
	public boolean dropIfExists(String fullFilePath) {
		if (hdfsManager.exists(fullFilePath, hadoopConnection)) {
			hdfsManager.deleteHadoopFile(fullFilePath, hadoopConnection);
			return true;
		} else {
			return false;
		}
	}
	
	public int getMaxReduceNumber() {
		if(clusterStatus!=null)
		{
			return maxReduceNumber;
		}
		else
			return 1;
	}
	
	
}

