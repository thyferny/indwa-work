/**
 * ClassName HadoopKmeansRunner.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-4
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.runner;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopKMeansConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputBasicInfo;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputProfiles;
import com.alpine.datamining.operator.hadoop.output.ClusterRangeInfo;
import com.alpine.datamining.operator.hadoop.output.KmeansValueRange;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.cluster.mapper.KMeansInitMapper;
import com.alpine.hadoop.cluster.mapper.KMeansPostMapper;
import com.alpine.hadoop.cluster.mapper.KmeansMapper;
import com.alpine.hadoop.cluster.mapper.KmeansOutPutMapper;
import com.alpine.hadoop.cluster.mapper.RandomKeyComparator;
import com.alpine.hadoop.cluster.mapper.TextGroupingComparator;
import com.alpine.hadoop.cluster.reducer.KMeansInitCombiner;
import com.alpine.hadoop.cluster.reducer.KMeansInitReducer;
import com.alpine.hadoop.cluster.reducer.KMeansPostReducer;
import com.alpine.hadoop.cluster.reducer.KmeansReducer;
import com.alpine.hadoop.cluster.util.distance.CamberraNumericalDistance;
import com.alpine.hadoop.cluster.util.distance.CosineSimilarityDistance;
import com.alpine.hadoop.cluster.util.distance.DiceNumericalSimilarityDistance;
import com.alpine.hadoop.cluster.util.distance.EuclideanDistance;
import com.alpine.hadoop.cluster.util.distance.GeneralizedIDivergenceDistance;
import com.alpine.hadoop.cluster.util.distance.InnerProductSimilarityDistance;
import com.alpine.hadoop.cluster.util.distance.JaccardNumericalSimilarityDistance;
import com.alpine.hadoop.cluster.util.distance.KLDivergenceDistance;
import com.alpine.hadoop.cluster.util.distance.ManhattanDistance;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansRunner extends AbstractHadoopRunner {


	private static Logger itsLogger = Logger
			.getLogger(HadoopKmeansRunner.class);

	List<String> selectedColumnsNames;// mapped column list
	List<String> selectedColumnsTypes;// mapped column list

	protected HadoopKMeansConfig config;
	private Configuration baseConf=new Configuration();
	//for merge use..
	protected String outputTempName;
	protected String kmeansTempName;

	protected String outputFileFullName;

	public static final String INIT_OUTPUT_NAME="init";
	private static final String POST_OUTPUT_NAME = "post";

	private int completedIterations;

	private String centPointFileName;

	private String kmeansInitReusltFile;

	private double EPSILON = 0.001;

	private String kMeansPostOutFile;
	List<String> postOutput;

	private String minMax;

	private ClusterStatus clusterStatus = null;
	
	ClusterOutputModel model;

	public HadoopKmeansRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	}

	@Override
	public int run(String[] args) throws Exception {
		
		String[] initRes = callKmeansInitJob();
		String centerPointString= initRes[0];
		this.minMax = initRes[1];
		String maxStep = config.getMax_optimization_steps();
		int maxIter = Integer.parseInt(maxStep);

		boolean convergence = false; 
		for(int index = 0 ; index < maxIter ; index++) {	
			callKmeansJob(index,centerPointString);

			centPointFileName= getIterationCenterPointFilePath(index);
			List<DoubleWritable[]> oldPointList = null;
			oldPointList = KmeansMapper.generateCPointList(centerPointString);
			centerPointString = ListUtility.listToString(hdfsManager.readHadoopPathToLineList4All(centPointFileName,hadoopConnection),"\n") ;
			List<DoubleWritable[]> newPointList = KmeansMapper.generateCPointList(centerPointString);
			convergence = isConvergence(oldPointList,newPointList); 
			
			if(convergence==true){
				completedIterations = index + 1;
				break;
			}
		}
		if(convergence==false) {
			completedIterations = maxIter;
		}

		callKmeansOutputJob(centerPointString);
 
		postOutput=callKmeansPostJob(centerPointString);
 
		if(convergence){
			return 1;
		}else{
			return 0;
		}
	}

	private List<String> callKmeansPostJob(String centriodsLines) throws  Exception {
		Configuration kMeansPostConf = new Configuration(baseConf);
		initHadoopConfig(kMeansPostConf,HadoopConstants.JOB_NAME.Kmeans_Post);

		kMeansPostConf.set(KmeansConfigureKeySet.SPLIT_VALUE, config.getSplit_Number());
		kMeansPostConf.set(KmeansConfigureKeySet.LIMIT_LINE, ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT));
		String[] centriodsLine = centriodsLines.split("\n");
		for(String line:centriodsLine){
			String[] centriods = line.split("\t");
			String centriod=centriods[1];
			kMeansPostConf.set(KmeansConfigureKeySet.CENTRIODS+"."+centriods[0], centriod);
		}
		String[] minMax = this.minMax.split("\n");
		for(String line:minMax){
			String[] temp = line.split("\t");
			kMeansPostConf.set("alpine.column."+temp[0].replace(KmeansConfigureKeySet.KEY_MAX_MIN,  ""), temp[1]);
		}
		
		Job kMeansPostJob = createJob(HadoopConstants.JOB_NAME.Kmeans_Post, kMeansPostConf, 
				KMeansPostMapper.class, KMeansPostReducer.class, Text.class, DoubleArrayWritable.class, outputFileFullName, kMeansPostOutFile);

		kMeansPostJob.setMapOutputKeyClass(LongWritable.class);
		runMapReduceJob(kMeansPostJob,true);
		return hdfsManager.readHadoopPathToLineList4All(kMeansPostOutFile,hadoopConnection);
	}

	private String[] callKmeansInitJob( ) throws Exception {
		Configuration aggJobConf = new Configuration(baseConf);

		initHadoopConfig(aggJobConf,HadoopConstants.JOB_NAME.Kmeans_Init);
		int k = Integer.parseInt(config.getK());

		Job kMeansInitJob = createJob(HadoopConstants.JOB_NAME.Kmeans_Init, aggJobConf, 
				KMeansInitMapper.class, KMeansInitReducer.class, Text.class, DoubleArrayWritable.class, inputFileFullName, kmeansInitReusltFile);
		
		kMeansInitJob.setCombinerClass(KMeansInitCombiner.class);
		
		kMeansInitJob.setSortComparatorClass(RandomKeyComparator.class);
		kMeansInitJob.setGroupingComparatorClass(TextGroupingComparator.class);
		kMeansInitJob.setMapOutputKeyClass(Text.class);
		runMapReduceJob(kMeansInitJob,true);
		badCounter=kMeansInitJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		
		boolean error=false;
		String[] parsed = new String[2];
		StringBuilder centroids = new StringBuilder();
		StringBuilder minMax = new StringBuilder();

		Map<String, String> initResult = readPathToKeyMap(kmeansInitReusltFile);
		for (String key : initResult.keySet()) {
			int index = 0;
			if("error1".equals(key)){
				error=true;
				break;
			}
			if (key.startsWith(KmeansConfigureKeySet.KEY_RANDOM_POINT)) {
				centroids.append(index).append("\t");
				index = index + 1;
				centroids.append(initResult.get(key)).append("\n");
			}

			if (key.startsWith(KmeansConfigureKeySet.KEY_MAX_MIN)) {
				minMax.append(key + "\t" + initResult.get(key)).append("\n");
			}
		}
		
		if(error){
			throw new Exception("The data set does not contain enough data to perform this operation. " +
					"Needs at least " +k+
					" data. This number might depend on the parameters");
		}
		parsed[0] = centroids.toString();
		parsed[1] = minMax.toString();
		return parsed;
	}

	private boolean isConvergence(List<DoubleWritable[]> oldPointList,
			List<DoubleWritable[]> newPointList) {
		if(oldPointList.size()!=newPointList.size()){
			return  false;
		}else{
			for(int i = 0;i<oldPointList.size();i++ ){
				DoubleWritable[] oldPoint = oldPointList.get(i);
				DoubleWritable[] newPoint = newPointList.get(i);
				for(int j =0 ;j<oldPoint.length;j++){
					double delta = Math.abs(oldPoint[j].get()-newPoint[j].get());
					if(newPoint[j].get()==0){
						if(delta>EPSILON){
							return false;
						}
					}
					else if((delta/newPoint[j].get())>EPSILON){
						return false;
					}
				}
			}
		}
		return true;
	}

	private void callKmeansJob( int index, String randomPointString) throws Exception {
		Configuration kmeansJobConf = new Configuration(baseConf);
		initHadoopConfig(kmeansJobConf,HadoopConstants.JOB_NAME.Kmeans_Iteration);
		kmeansJobConf.set("mapred.compress.map.output", "true");

		kmeansJobConf.set(KmeansConfigureKeySet.OUTPUT_CENTER_POINTS, randomPointString);
		kmeansJobConf.set(KmeansConfigureKeySet.ITERATION, String.valueOf(index + 1));

		Job kmeansJob = createJob(HadoopConstants.JOB_NAME.Kmeans_Iteration+"_" + (index + 1), kmeansJobConf, 
				KmeansMapper.class, KmeansReducer.class, LongWritable.class, DoubleArrayWritable.class, inputFileFullName, getIterationCenterPointFilePath(index)); 
		
		double algorithmLimit = Integer.parseInt(config.getK());;
		double clusterCapacity = algorithmLimit;
		
		if(clusterStatus != null) {
			clusterCapacity = 1.5 * clusterStatus.getMaxReduceTasks(); 
		} 
		
		kmeansJob.setNumReduceTasks((int) Math.round(Math.min(clusterCapacity, algorithmLimit)));
		runMapReduceJob(kmeansJob,true);
	}

	private boolean callKmeansOutputJob(String centerPointString) throws Exception {
		Configuration kmeansJobConf = new Configuration(baseConf);
		initHadoopConfig(kmeansJobConf,HadoopConstants.JOB_NAME.Kmeans_Output);
		kmeansJobConf.set(KmeansConfigureKeySet.OUTPUT_CENTER_POINTS,centerPointString);
		
		setMapReduceCompress(outputFileFullName, kmeansJobConf);

		Job kmeansOutputJob = createJob(HadoopConstants.JOB_NAME.Kmeans_Output, kmeansJobConf, 
				KmeansOutPutMapper.class, null, Text.class, Text.class, inputFileFullName, outputFileFullName); 
		if(hdfsManager.exists(outputFileFullName,hadoopConnection)==true){
			if(Resources.YesOpt.equals(config.getOverride())){
				boolean success = dropIfExists(outputFileFullName);
				if(success==false){
					throw new Exception("Can not delete out put directory "+outputFileFullName);
				}
			}
			else{
				AnalysisException e = new AnalysisException("File already exist:" + outputFileFullName);
				itsLogger.error(e);
				throw new AnalysisException(e);
			}
		} 
		runMapReduceJob(kmeansOutputJob,true);
		return true;
	}


	private String getIterationCenterPointFilePath( int index) {
		return outputTempName+"_cp_"+(index+1);
	}

	private String getDistanceClassName(String distance) {
		String distanceClassName = "";
		if(distance.equals("Euclidean")){
			distanceClassName=EuclideanDistance.class.getName();
		}else if(distance.equals("CamberraNumerical")){
			distanceClassName=CamberraNumericalDistance.class.getName();
		}else if(distance.equals("CosineSimilarity")){
			distanceClassName=CosineSimilarityDistance.class.getName();
		}else if(distance.equals("DiceNumericalSimilarity")){
			distanceClassName=DiceNumericalSimilarityDistance.class.getName();
		}else if(distance.equals("GeneralizedIDivergence")){
			distanceClassName=GeneralizedIDivergenceDistance.class.getName();
		}else if(distance.equals("InnerProductSimilarity")){
			distanceClassName=InnerProductSimilarityDistance.class.getName();
		}else if(distance.equals("JaccardNumericalSimilarity")){
			distanceClassName=JaccardNumericalSimilarityDistance.class.getName();
		}else if(distance.equals("KLDivergence")){
			distanceClassName=KLDivergenceDistance.class.getName();
		}else if(distance.equals("Manhattan")){
			distanceClassName=ManhattanDistance.class.getName();
		}
		return distanceClassName;
	}


	@Override
	public Object runAlgorithm(AnalyticSource source) throws Exception {
		if(hdfsManager.exists(outputFileFullName,hadoopConnection)==true){
			if(Resources.YesOpt.equals(config.getOverride())){
				boolean success = dropIfExists(outputFileFullName);
				if(success==false){
					throw new Exception("Can not delete out put directory "+outputFileFullName);
				}
			}else{
				throw new Exception("File is existed");
			}		
		} 
		init((HadoopAnalyticSource) source);
		if (itsLogger.isDebugEnabled()) {
			itsLogger.debug("Hadoop Kmeans Runner Start");
		}
		int stable = ToolRunner.run(this, null);
		if (itsLogger.isDebugEnabled()) {
			itsLogger.debug("Hadoop Kmeans Runner End");
		}

		return generateOutputModel(stable);
	}
	
	public void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource) ;

		try {
			this.clusterStatus  = HadoopConnection.getClusterInfo(hadoopConnection);
		} catch (IOException e) {
			System.out.println("Hadoop Cluster Status could not be obtained in Kmeans setup");
		}

		config = (HadoopKMeansConfig) hadoopSource.getAnalyticConfig();

		String resultLocaltion = config.getResultsLocation();
		String resultsName = config.getResultsName();

		if (!StringUtil.isEmpty(resultLocaltion)
				&& resultLocaltion.endsWith(HadoopFile.SEPARATOR) == false) {
			resultLocaltion = resultLocaltion + HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion + resultsName;
		
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();

		String[] columns = config.getColumnNames().split(",");
		selectedColumnsNames = new ArrayList<String>();
		selectedColumnsTypes = new ArrayList<String>();
		for (int i = 0; i < columns.length; i++) {
			selectedColumnsNames.add(columns[i]);
			selectedColumnsTypes.add(columnTypeList.get(columnNameList.indexOf(columns[i])));
		}
		
		baseConf.set(KmeansConfigureKeySet.K, config.getK());
		baseConf.set(KmeansConfigureKeySet.COLUMNS, config.getColumnNames());
		baseConf.set(KmeansConfigureKeySet.ID_FIELD,config.getIdColumn()==null?"":config.getIdColumn());
		baseConf.set(KmeansConfigureKeySet.DISTANCE_TYPE, getDistanceClassName(config.getDistance()));
	}

	public void setOutputTempName(String outputTempName) {
		this.outputTempName = outputTempName;

		this.kmeansTempName = outputTempName+"_k";

		this.kmeansInitReusltFile = kmeansTempName+ HadoopFile.SEPARATOR+INIT_OUTPUT_NAME;

		this.kMeansPostOutFile= kmeansTempName+ HadoopFile.SEPARATOR+POST_OUTPUT_NAME;

	}

	private List<String[]> getMaxMinList() throws Exception {
		List<String[]> maxMinList = new ArrayList<String[]>(); 
		String result = this.minMax;
		String[] rows = result.split("\n") ;
		for (int i = 0; i < rows.length; i++) {
			if(rows[i].startsWith(KmeansConfigureKeySet.KEY_MAX_MIN)){
				maxMinList.add(rows[i].split("\t")[1].split(",")) ;
			}
		}
		return maxMinList;
	}

	private double getAverageDistance() throws Exception {  
		for (String line:postOutput) {
			if(line.startsWith(KmeansConfigureKeySet.KEY_TOTAL_DISTANCE)){
				String[] rowData = line.split("\t");
				String totalDistance = rowData[1].split(",")[0]  ;
				String lineNumbers =  rowData[1].split(",")[1]  ;
				return Double.parseDouble(totalDistance) / (Double.parseDouble(lineNumbers));//*(columnIndex.toString().split(",").length));  
			}
		}
		return 0;
	}
	
	private ClusterOutputModel generateOutputModel(int stable) throws AnalysisException {
		model=new ClusterOutputModel();
		model.setColumnNames(selectedColumnsNames);
		model.setColumnTypes(selectedColumnsTypes);
		String clusterName = AlpineUtil.generateClusterName(selectedColumnsNames);
		int clusterCount = setCentriods();
		setSamplesData(clusterName);
		setProfiles();
		setScatterPlot();
		setBasicInfo(stable,clusterName,clusterCount);
		clearTempFile(completedIterations);
		return model;
	}
	
	private void clearTempFile(int completedIterations) {
		dropIfExists(outputTempName);
		for (int i = 0; i < completedIterations; i++) {
			String path = outputTempName + "_cp_" + (i + 1);
			dropIfExists(path);
		}
	}
	
	private void setBasicInfo(int stable,String clusterName, int clusterCount) throws AnalysisException{
		ClusterOutputBasicInfo outputText=new ClusterOutputBasicInfo();
		try {
			String powExpressionTotalDistance = com.alpine.datamining.api.utility.AlpineMath.powExpression(getAverageDistance());
			outputText.setAvgDistanceMeasurement(Double.parseDouble(powExpressionTotalDistance));
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}	
		outputText.setClusterColumName(clusterName);
		outputText.setClusterCount(clusterCount);
		model.setOutputText(outputText);
		if(stable==0){
			model.setStable(false);
		}else{
			model.setStable(true);
		}
	}
	private void setScatterPlot() throws AnalysisException {
		try {
			Map<String,Map<String, List<Double>>> outputScatters=new LinkedHashMap<String,Map<String, List<Double>>>();
			
//			List<String> scatterOutputLinesArray = getScatterOutPut();
			Map<String,List<String[]>> linesMap=new HashMap<String,List<String[]>>();
			for (String scatterOutputLine : postOutput) {
				if (scatterOutputLine.startsWith(KmeansConfigureKeySet.KEY_SCATTER_POINTS)) {
					String[] line = scatterOutputLine.split("\t");
					String cluster = line[0].split("_")[1];
					String[] temp = line[1].split(",");
					if (linesMap.containsKey(cluster)) {
						linesMap.get(cluster).add(temp);
					} else {
						List<String[]> linesList = new ArrayList<String[]>();
						linesList.add(temp);
						linesMap.put(cluster, linesList);
					}
				}
			}
			for(String cluster:linesMap.keySet()){
				List<String[]> list = linesMap.get(cluster);
				Map<String, List<Double>> scatterMap=new HashMap<String, List<Double>>();
				for(String[] ss:list){
					for(int j=0;j<ss.length;j++){
						if(scatterMap.containsKey(selectedColumnsNames.get(j))){
							scatterMap.get(selectedColumnsNames.get(j)).add(Double.parseDouble(ss[j]));
						}else{
							List<Double> sublist=new ArrayList<Double>();
							sublist.add(Double.parseDouble(ss[j]));
							scatterMap.put(selectedColumnsNames.get(j), sublist);
						}
					}
				}
				outputScatters.put(cluster, scatterMap);
			}
			model.setOutputScatters(outputScatters);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
	}
	private void setProfiles()
			throws AnalysisException {
		try {
			ClusterOutputProfiles outputProfiles=new ClusterOutputProfiles(); 
//			List<String> splitOutputLine = getSplitOutPut();//splitOutputLines.split("\n");
			List<ClusterRangeInfo> clusterRangeInfo=new ArrayList<ClusterRangeInfo>();
			int split_Number = Integer.parseInt(config.getSplit_Number());
			long totalRowCounts=0l;
			for(String line:postOutput){
				if (line.startsWith(KmeansConfigureKeySet.KEY_SPLIT_OUTPUT)) {
					String[] split = line.split("\t");
					String clusterNum = split[0].split("_")[1];
					ClusterRangeInfo info = new ClusterRangeInfo();
					clusterRangeInfo.add(info);
					info.setClusterName(clusterNum);
					String[] temp = split[1].split(",");
					Map<String, List<Long>> columnRangeRowCountMap = new HashMap<String, List<Long>>();
					long clusterRowCounts = 0l;
					for (int i = 0; i < temp.length; i++) {
						if (i % split_Number == 0) {
							List<Long> columnRangeRowCount = new ArrayList<Long>();
							columnRangeRowCountMap.put(
									selectedColumnsNames.get(i / split_Number),
									columnRangeRowCount);
						}
						long rowCount = Double.valueOf(temp[i]).longValue();
						columnRangeRowCountMap.get(
								selectedColumnsNames.get(i / split_Number))
								.add(rowCount);
						clusterRowCounts = clusterRowCounts + rowCount;
					}
					long totalRow = clusterRowCounts
							/ selectedColumnsNames.size();
					info.setClusterRowCounts(totalRow);
					totalRowCounts = totalRowCounts + totalRow;
					info.setColumnRangeRowCountMap(columnRangeRowCountMap);
				}
			}	
			outputProfiles.setClusterRangeInfo(clusterRangeInfo);
			outputProfiles.setTotalRowCounts(totalRowCounts);
		 
			
			Map<String, List<KmeansValueRange>> columnRangeMap=new HashMap<String, List<KmeansValueRange>>();
			outputProfiles.setColumnRangeMap(columnRangeMap);
			
			List<String[]> maxMinList = getMaxMinList();
			DecimalFormat df = new DecimalFormat("#.##");
			for(int i=0;i<maxMinList.size();i++){
				//String[] temp = lines[i].split("\t");
				String[] minMax = maxMinList.get(i);
				double min=Double.valueOf(df.format(Double.parseDouble(minMax[0])));
				double max=Double.valueOf(df.format(Double.parseDouble(minMax[1])));
				double step = Double.parseDouble(df.format((max-min)/(split_Number*1.0)));
				List<KmeansValueRange> columnRangeList=new ArrayList<KmeansValueRange>();
				double currentValue=min;
				for(int j=0;j<split_Number;j++){
					if(j==split_Number-1){
						columnRangeList.add(new KmeansValueRange(currentValue,max));
					}else{
						columnRangeList.add(new KmeansValueRange(currentValue,Double.parseDouble(df.format(currentValue+step))));
					}
					currentValue=Double.parseDouble(df.format(currentValue+step));
				}	
				columnRangeMap.put(selectedColumnsNames.get(i), columnRangeList);
			}
			model.setOutputProfiles(outputProfiles);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
	}
	private void setSamplesData(String clusterName) throws AnalysisException {
		try {
			List<String[]> dataSampleContents=new ArrayList<String[]>();
			int fileCountLimit = Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT));
			
			List<String> dataSamplesLines = hdfsManager.readHadoopPathToLineList(outputFileFullName, hadoopConnection, fileCountLimit);
			boolean hasIDColumn=StringUtil.isEmpty(config.getIdColumn())==false;
			int resultLength =selectedColumnsNames.size()+1;
			
			if(hasIDColumn==true){
				resultLength=resultLength+1;
			} 
			String[] titles=new String[resultLength];
			for(int i=0;i<selectedColumnsNames.size();i++){
				titles[i]=selectedColumnsNames.get(i);
			}
			if(hasIDColumn==true){
				titles[selectedColumnsNames.size()]=config.getIdColumn();
				titles[selectedColumnsNames.size()+1]=clusterName;
			}else{
				titles[selectedColumnsNames.size()]=clusterName;
			}
			//first line is header
			dataSampleContents.add(titles);
			String delimiter = HadoopUtility.getDelimiterValue(fileStructureModel) ;
			for(String dataSample:dataSamplesLines){
				dataSampleContents.add(dataSample.split(delimiter));
			}
			model.setDataSampleContents(dataSampleContents);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
	}
	
	private int setCentriods() throws AnalysisException {
		int clusterCount = -1;
		try {
			Map<String,Map<String, Double>> centroidsContents=new LinkedHashMap<String,Map<String, Double>>();
			Map<String, String> centriodsMap =  readPathToKeyMap(centPointFileName);
			clusterCount = centriodsMap.size();
			for(String key:centriodsMap.keySet()){
				Map<String, Double> clusterMap=new HashMap<String, Double>();
				String centriod=centriodsMap.get(key);
				String[] temp = centriod.split(",");
				for(int i=0;i<temp.length;i++){
					clusterMap.put(selectedColumnsNames.get(i), Double.parseDouble(AlpineMath.doubleExpression(Double.parseDouble(temp[i]))));
				}
				centroidsContents.put(key, clusterMap);
			}
			model.setCentroidsContents(centroidsContents);
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}
		return clusterCount;
	}
	
	private Map<String, String> readPathToKeyMap(String dirPath) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		List<String> fileInfos = hdfsManager.readHadoopPathToLineList4All(dirPath,hadoopConnection);
		if(fileInfos==null||fileInfos.size()==0) {
			throw new EmptyFileException(dirPath+resultEmptyErr);
		}
		for (String line : fileInfos) {
			String[] lineArray = line.split("\t");
			if(lineArray[0].startsWith("error1")){
				resultMap.put(lineArray[0], "erro1");
			}
			else{
				if(lineArray.length>1){
					resultMap.put(lineArray[0], lineArray[1]);
				}
				else{
					//this may be a error msg
				}
			}
		}
		return resultMap;
	}
}
