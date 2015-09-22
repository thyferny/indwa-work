/**
*
*
* Version information: 1.00
*
* Sep 6, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.datamining.api.impl.hadoop.runner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.LineReader;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopDecisionTrainConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.mapper.AssignSplitsVarMapper;
import com.alpine.hadoop.tree.mapper.DecisionTreeMapper;
import com.alpine.hadoop.tree.mapper.DecisionTreePreemptionMapper;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.hadoop.tree.model.SplitRecordWritable;
import com.alpine.hadoop.tree.model.TreeGroupPartitioner;
import com.alpine.hadoop.tree.model.TreeGroupingComparator;
import com.alpine.hadoop.tree.model.TreeRecordComparator;
import com.alpine.hadoop.tree.reducer.AssignSplitsReducer;
import com.alpine.hadoop.tree.reducer.DecisionTreeReducer;
import com.alpine.hadoop.tree.reducer.DecisionTreePreemptionReducer;
import com.alpine.utility.db.Resources;
import com.alpine.utility.hadoop.HadoopConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Shawn
 *  
 */

public class HadoopDecisionTreeTrainRunner extends AbstractHadoopRunner {
		public static final String GSON_JAR = "gson-1.7.1.jar";
	
		List<String> columnsList;// mapped column list
		protected String resultsName;
 
		private int integerDistinct=2;
		protected String resultLocaltion;
		protected HadoopDecisionTrainConfig dtreeConfig;
 
		 private static final Runtime s_runtime = Runtime.getRuntime ();
		private String splitsOut;
		private Integer[] continuousColumns;
		private HadoopTree resultModel; 
		
	
		private ArrayList<Integer> continuousColumnsInt= new ArrayList<Integer>();
		
	 
		private Integer[] categoricalColumns;

		
		private static Logger itsLogger = Logger
				.getLogger(HadoopDecisionTreeTrainRunner.class);
 
		private String continuousSplitAmount;
		private String categoryLimit;
		private String continuousColumnsString;
		private AnalyticContext context;
		private HadoopDecisionTrainConfig config;
		private StringBuffer columnSelect;
		
		public HadoopDecisionTreeTrainRunner(AnalyticContext context,String operatorName) {
			super(context,operatorName);
		}

		@Override
		public int run(String[] args) throws Exception {
			
			
		
	
			
			Configuration jobConfig = getConf();
			
			Map<String, Double>[] buildMap=generateDistinctMap(jobConfig);
				
			resultModel = analyze(hadoopSource.getFileName(), tmpPath+"DecisionTree_"+System.currentTimeMillis(), jobConfig,buildMap);
			return 0;
		}

		private void setParameters(Configuration jobConfig) {
			jobConfig.set(DecisionTreeConfigureKeySet.COLUMNS, this.columnSelect.toString());
	        jobConfig.set(DecisionTreeConfigureKeySet.SPLIT_FILE, tmpPath+"DecisionTreeSplits_"+System.currentTimeMillis());// location of file with continuous split breakdown
	        jobConfig.set(DecisionTreeConfigureKeySet.ROOT_LABEL, "DecisionTree");// name of root node
			jobConfig.set("tree.mr.binCount", this.continuousSplitAmount);// quantiles for continuous values
	        jobConfig.set("tree.mr.splits", splitsOut);// unique categorical values
	        jobConfig.setInt(DecisionTreeConfigureKeySet.LABEL_INDEX, 0);// starting id of root. subsequent nodes are monotonically increasing values
	        jobConfig.setInt(DecisionTreeConfigureKeySet.FEATURE_LENGTH, this.columnSelect.toString().split(",").length + 1);// length of feature vector (including dependant column) 
	        jobConfig.setFloat(DecisionTreeConfigureKeySet.MINIMUM_GAIN, Float.parseFloat(config.getMinimal_gain()));
			jobConfig.setInt(DecisionTreeConfigureKeySet.MINIMUM_NODE_SIZE, Integer.parseInt(config.getMinimal_size_for_split()));
			jobConfig.set(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS, this.continuousColumnsString);// comma separated list of continuous column indices
			jobConfig.set(DecisionTreeConfigureKeySet.TREE_FILE, tmpPath+"DecisionTreeModel_"+System.currentTimeMillis());// path for the tree model to be stored at
			jobConfig.setInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, fileStructureModel.getColumnNameList().indexOf(config.getDependentColumn()));// index of dependant variable
			jobConfig.setInt(DecisionTreeConfigureKeySet.MAX_DEPTH, Integer.parseInt(config.getMaximal_depth()));
		}

		
		@Override
		public Object runAlgorithm(AnalyticSource source) throws  Exception {
			config=(HadoopDecisionTrainConfig)source.getAnalyticConfig();
			init((HadoopAnalyticSource)source);
			Configuration jobConfig = new Configuration(); 
			
			super.initHadoopConfig(jobConfig,HadoopConstants.JOB_NAME.DecisionTree_Parse_Splits);
			
//			config=(HadoopDecisionTrainConfig)source.getAnalyticConfig();
			
	 		String gsonJarFilePath = getJarRealPath(GSON_JAR);
	 		String[] args =new String[4];	
			args[0] = "-libjars";
			args[1] =   gsonJarFilePath;
			args[2]=hadoopSource.getFileName();
			String decisionOut = tmpPath +"DecisionTree_"+System.currentTimeMillis();
			args[3]=decisionOut;//fullPathFileName;
			runGetDistictJob(jobConfig);
			
			setParameters(jobConfig);
 			try {
				ToolRunner.run(jobConfig, this, args);
			} catch (Exception e) {
				itsLogger.error(e);
				throw new AnalysisException(e);
			}
			finally{
				deleteTemp();
			}
			return this.resultModel;
		}

		private void runGetDistictJob(Configuration jobConfig)
				throws Exception {
			jobConfig.set(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS, this.continuousColumnsString);
			jobConfig.set(DecisionTreeConfigureKeySet.COLUMNS, this.columnSelect.toString());
			jobConfig.setInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, fileStructureModel.getColumnNameList().indexOf(config.getDependentColumn()));
			Job splitsParse = createJob(HadoopConstants.JOB_NAME.DecisionTree_Parse_Splits, jobConfig, AssignSplitsVarMapper.class, AssignSplitsReducer.class, 
							Text.class, Text.class, hadoopSource.getFileName(), splitsOut);
//			getContext().registerMapReduceJob(splitsParse);
			if(jobConfig.get(DecisionTreeConfigureKeySet.Hadoop_Util_Reduce_Number_Seted)==null
					||!jobConfig.get(DecisionTreeConfigureKeySet.Hadoop_Util_Reduce_Number_Seted).equals(Resources.TrueOpt))
			{
				int maxReduceTaskNumber=(int) (1.75*getMaxReduceNumber());
				int columnNumber=config.getColumnNames().split(",").length;
				splitsParse.setNumReduceTasks(Math.min(maxReduceTaskNumber, columnNumber));
			}
			splitsParse.setSortComparatorClass(TreeRecordComparator.class);
			splitsParse.setGroupingComparatorClass(TreeGroupingComparator.class);
			splitsParse.setPartitionerClass(TreeGroupPartitioner.class);
			splitsParse.setMapOutputKeyClass(SplitRecordWritable.class);
			super.setInputFormatClass(splitsParse);
			if(!runMapReduceJob(splitsParse,true)){
				itsLogger.error("Parse of splits failed");
				throw new Exception("Parse of splits Failed");
			}
			badCounter=splitsParse.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
			 
		}

		
		protected void init(HadoopAnalyticSource hadoopSource)   throws Exception {
			super.init(hadoopSource) ;
		 	this.continuousSplitAmount = config.getNumericalGranularity() == null ? "1000000" : config.getNumericalGranularity();
			categoryLimit=config.getCategoryLimit()== null ? "1000" : config.getCategoryLimit();
			splitsOut = tmpPath+"DecisionTreeInitialParse_"+System.currentTimeMillis();
			StringBuffer colBuf = new StringBuffer();
			List<Integer> contList = new ArrayList<Integer>();
			List<Integer> catList = new ArrayList<Integer>();
			this.categoricalColumns = new Integer[0];
			this.continuousColumns = new Integer[0];
			this.columnSelect = generateColumnIndex();
			this.columnsList = fileStructureModel.getColumnNameList();
			int i = 0;	
			for(String el : fileStructureModel.getColumnTypeList()) {
				if(el.equals("chararray")) {	 
					catList.add(i);
				} else {
					contList.add(i);
					colBuf.append(i + ","); 
				}
				i++;
			}
			if(colBuf.length() > 0) {
				colBuf.deleteCharAt(colBuf.length()- 1);
			}
			this.continuousColumnsString = colBuf.toString();
			this.categoricalColumns = catList.toArray(this.categoricalColumns);
			this.continuousColumns = contList.toArray(this.continuousColumns);

		}
			
		public AnalyticContext getContext() {
			return context;
		}

		public void setContext(AnalyticContext context) {
			this.context = context;
		}

 
		public HadoopTree getModel() {
			return this.resultModel;
		}

		public void setHistogramBinCount(String count) {
			this.continuousSplitAmount = count;
		}
		
 
 
	    
	    private StringBuffer generateColumnIndex() {
			StringBuffer columnIndex = new StringBuffer();
			String[] columns = config.getColumnNames().split(",");

			int i = 0;
//			columnIndex.append(fileStructureModel.getColumnNameList().indexOf(
//					columns[0]));
			List<String> columnRef=fileStructureModel.getColumnNameList();

			int j=0;
			for (i = 0; i < columnRef.size(); i++) {
				String column=columnRef.get(i);
				if(j==0){
					boolean choosed=false;
					for(String selectColumn:columns){
						if(column.equals(selectColumn)){
							choosed=true;
							break;
						}
					}
					if(choosed){
						columnIndex.append(i);
						j=1;
					}
				}
				else{
					boolean choosed=false;
					for(String selectColumn:columns){
						if(column.equals(selectColumn)){
							choosed=true;
							break;
						}
					}
					if(choosed){
						columnIndex.append(",").append(i);
					}
				}
//				columnIndex.append(",");
//				columnIndex.append(fileStructureModel.getColumnNameList().indexOf(
//						columns[i]));
			}
			return columnIndex;
		}
	    
	    
	    // the driver program for the algorithm.  keeps track of state and the iterations
	    public HadoopTree analyze(String inputPath, String outputPath, Configuration iterationConfig, Map<String, Double>[] splitMap) throws Exception {
		 		
			double algorithmLimit = -1;
			double  clusterCapacity = 1.75  * this.getMaxReduceNumber();
 			boolean nodesToExpand;
//			int featureLength = iterationConfig.getInt(DecisionTreeConfigureKeySet.FEATURE_LENGTH, 100);
			int currDepth = 0;
			String[] selectedColumns = iterationConfig.get(DecisionTreeConfigureKeySet.COLUMNS).split(",");
			int[] selectedColumnsInt = new int[selectedColumns.length];
			
			int mm = 0;
			for(String s : selectedColumns) {
				selectedColumnsInt[mm] = Integer.parseInt(s);
				mm++;
			}
			
			int maxDepth =Integer.parseInt(config.getMaximal_depth());// iterationConfig.getInt(DecisionTreeConfigureKeySet.MAX_DEPTH, 5);
	 
//			int classificationIndex = iterationConfig.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
//			String jsonModel;
			 	
			// initialize tree
			String rootName = iterationConfig.get(DecisionTreeConfigureKeySet.ROOT_LABEL);
			int labelIndex = iterationConfig.getInt(DecisionTreeConfigureKeySet.LABEL_INDEX, 0);
			HadoopTree treeRoot = new HadoopTree(rootName, labelIndex, 0, true, false);
			treeRoot.setColumnMap(columnsList);
			treeRoot.setDemap(selectedColumnsInt);
			treeRoot.setRoot(true);
			treeRoot.updateUniqueId(1);
			Gson gson = new GsonBuilder().serializeNulls().create();
			Type splitType = new TypeToken<HashMap<String, Double>[]>() {}.getType();
			
			 
			
			String[] c = {};
			if(iterationConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS) != null && !iterationConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).isEmpty()) {
				c = iterationConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).split(",");
			}
			for(int i = 0; i < c.length; i++) {
				this.continuousColumnsInt.add(i, Integer.parseInt(c[i]))  ;
			}
			
//			int classifyMap = 0;
			
			// build splits data structure
//			Text placeholder = new Text();
//			double bins = Double.parseDouble(iterationConfig.get("tree.mr.binCount"));
			
//			FileStatus[] nodesSplit = fs.globStatus(new Path(iterationConfig.get("tree.mr.splits")).suffix("/part-*"));
//			splitMap = generateDistinctMap(classificationIndex, fs, splitMap,
//					buildMap, classifyMap, placeholder, bins, nodesSplit);
			
			String modelPath = iterationConfig.get(DecisionTreeConfigureKeySet.TREE_FILE);
			FileSystem fs = FileSystem.get(URI.create(modelPath), iterationConfig);
			String splitsPath = iterationConfig.get(DecisionTreeConfigureKeySet.SPLIT_FILE);
			FSDataOutputStream outS = fs.create(new Path(splitsPath));
			// write tree to HDFS
			FSDataOutputStream out = fs.create(new Path(modelPath));
//			String splitsPath = iterationConfig.get(DecisionTreeConfigureKeySet.SPLIT_FILE);
			// serialize splits to HDFS
//			FSDataOutputStream outS = fs.create(new Path(splitsPath));
			tmpFileToDelete.add(splitsPath);
			String splitsString = gson.toJson(splitMap, splitType);
			iterationConfig.set(DecisionTreeConfigureKeySet.SPLIT_FILE, splitsPath);
			IOUtils.copyBytes(new ByteArrayInputStream(splitsString.getBytes()), outS, 4096, true);
			IOUtils.closeStream(outS);
			HashMap<String,Integer> dinstinctValue= new HashMap<String,Integer>();
			for(Entry<String, Double> valueIndex: splitMap[0].entrySet())
			{
				dinstinctValue.put(valueIndex.getKey(), valueIndex.getValue().intValue());
			}
			treeRoot.setDependent(dinstinctValue);
			String ser = gson.toJson(treeRoot);
			IOUtils.copyBytes(new ByteArrayInputStream(ser.getBytes()), out, 4096, true);
			IOUtils.closeStream(out);
			// configuration		
			iterationConfig.set(DecisionTreeConfigureKeySet.TREE_FILE, modelPath);
			
			iterationConfig.set("mapred.compress.map.output", "true");
			iterationConfig.set(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS, continuousColumnsString); 
			
			Job iteration=null;
			Configuration iterConf;
//			treeRoot.setDependent(splitMap[0]);
			ArrayList<Integer> finalNodes=new ArrayList<Integer>(); 
			do {
//				String ser = gson.toJson(treeRoot);
				 
				// configuration		
				iterationConfig.set(DecisionTreeConfigureKeySet.TREE_FILE, modelPath);
			nodesToExpand = false;
			algorithmLimit =treeRoot.getUniqueId()- finalNodes.size();
			StringBuffer finalString=new StringBuffer();
			for(int finalNode:finalNodes)
			{
				finalString.append(finalNode).append(",");
			}
			if(finalString.length()>1)
			{
				finalString.deleteCharAt(finalString.length()-1);
			}
			iterationConfig.set(DecisionTreeConfigureKeySet.FINALNODESLIST, finalString.toString());

			iterConf = new Configuration(iterationConfig);	
	 	 	fileFormatHelper.initHadoopConfig(iterConf) ;

			itsLogger.info("Computation for tree depth " + currDepth + " has begun!");
			
			// set iteration specific configuration
			String iterOut = outputPath + "_" + currDepth + "/Iteration_" + currDepth;
			
			if(algorithmLimit<=0.75*this.getMaxReduceNumber()){
				nodesToExpand=calculatePreemption(iteration, currDepth, iterConf, inputPath, iterOut, treeRoot, finalNodes, clusterCapacity, selectedColumnsInt, splitMap, dinstinctValue);
				}else
				{
					nodesToExpand=	calculateNoPreemption(iteration, currDepth, iterConf, inputPath, iterOut, treeRoot, finalNodes, clusterCapacity, selectedColumnsInt, splitMap, dinstinctValue);
				}
			 String treeSerial = gson.toJson(treeRoot);
			    fs.delete(new Path(modelPath) ,true);
			    tmpFileToDelete.add(modelPath);
				FSDataOutputStream outIter = fs.create(new Path(modelPath));
				IOUtils.copyBytes(new ByteArrayInputStream(treeSerial.getBytes()), outIter, 4096, true);
				
				IOUtils.closeStream(outIter);
				
				// cleanup state
				fs.delete(new Path(outputPath + "_" + currDepth), true);
				
			currDepth++;	
			} while(nodesToExpand && ((currDepth + 1) < maxDepth || maxDepth == -1));
			return treeRoot;
		}

		private boolean calculateNoPreemption(Job iteration,int currDepth,Configuration iterConf,String inputPath,String iterOut,
				HadoopTree treeRoot ,ArrayList<Integer> finalNodes,double clusterCapacity ,int[] selectedColumnsInt,
				Map<String, Double>[] splitMap,HashMap<String, Integer> dinstinctValue
				) throws Exception {				
			iteration = createJob(HadoopConstants.JOB_NAME.DecisionTree_Depth +"_" + currDepth, iterConf, DecisionTreeMapper.class, DecisionTreeReducer.class, 
	 				IntWritable.class, Text.class, inputPath, iterOut);
			super.setInputFormatClass(iteration);
			int algorithmLimit =treeRoot.getUniqueId()- finalNodes.size();
		
			if(clusterCapacity == -1) {
			// if cluster capacity not set, set to the limit of the algorithm
				clusterCapacity = algorithmLimit;
			}
		
			iteration.setMapOutputKeyClass(Text.class);
			iteration.setMapOutputValueClass(Text.class);
			iteration.setOutputFormatClass(SequenceFileOutputFormat.class);
			iteration.setNumReduceTasks((int) Math.max(Math.round(Math.min(clusterCapacity, algorithmLimit)), 1));
			if(!runMapReduceJob(iteration,true)){
				itsLogger.error("Iteration #" + currDepth  + " failed! Please check paramters and resubmit");
				throw new RuntimeException("Iteration #" + currDepth + " failed! Please check paramters and resubmit");
			}
//			InputStream in = hdfsManager.readHadoopFileToInputStream(iterOut, this.hadoopConnection);
		 
//			BufferedReader bfReader = new BufferedReader(new InputStreamReader(
//					in));
//			String lines = null;
			 
//			while ((lines = bfReader.readLine()) != null) {
//				int node=Integer.parseInt(lines.split("\t")[0]);
//				String value=lines.split("\t")[1];
//				
//		
//			}

		FileSystem fs = FileSystem.get(URI.create(iterOut), iterConf);
//		FSDataInputStream inTree = fs.open(new Path(iterOut));
		
		// read tree model
			ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
				

			IOUtils.closeStream(buf1);
		
//			treeRoot = gson.fromJson(jsonModel, HadoopTree.class);
//			treeRoot.setDependent(dinstinctValue);
		// setup SequenceFile reader
			FileStatus[] nodes = fs.globStatus(new Path(iterOut).suffix("/part-*"));
			Path[] paths = FileUtil.stat2Paths(nodes);

			SequenceFile.Reader reader = null;
			boolean nodesToExpand=false;
		// setup SequenceFile reader
		for (Path path : paths) {

			reader = new SequenceFile.Reader(fs, path, iterConf);

			// read sequence file
			Writable key = (Writable) ReflectionUtils.newInstance(
					reader.getKeyClass(), iterConf);
			Writable value = (Writable) ReflectionUtils.newInstance(
					reader.getValueClass(), iterConf);

			while(reader.next(key, value)) {
				int node = ((IntWritable) key).get();
				String[] results = value.toString().split("\t");
				String[] first = results[0].split(",");
				int split = Integer.parseInt(first[0]);
				String continuousVal = first[1];
				HadoopTree currNode = treeRoot.getNode(node);
				
				String[] counts = results[1].split(",");
				int[] distribution = new int[counts.length];		
				
				int i =0;
				 
				for(String s : counts) {
					distribution[i] = Integer.parseInt(s);
					i++;
				}
				
				currNode.setCount(distribution);
				if(!finalNodes.contains(nodes))
				{
					finalNodes.add(node);
				}
				
				
				if(split != -1){
					int[] nodeDistribution= null;	
					if(results.length>2)//TODO else
					{
						String[] nodeCounts = results[2].split(",");
						nodeDistribution=new int[nodeCounts.length];
						int k =0;
						 
						for(String s : nodeCounts) {
							nodeDistribution[k] = Integer.parseInt(s);
							k++;
						}
					}
					// if node is to be expanded
					// build out the tree appropriately
					  nodesToExpand = true;
					int splitReverse = Arrays.binarySearch(selectedColumnsInt, split);
					int idUpdate = currNode.expand(split, 
							splitMap[splitReverse+1], continuousVal, treeRoot.getUniqueId(), selectedColumnsInt, columnsList, dinstinctValue, nodeDistribution);
					treeRoot.updateUniqueId(idUpdate);
				}
			}
			 
		}
		return nodesToExpand;
	}
		private boolean calculatePreemption(Job iteration,int currDepth,Configuration iterConf,String inputPath,String iterOut,
				HadoopTree treeRoot ,ArrayList<Integer> finalNodes,double clusterCapacity ,int[] selectedColumnsInt,
				Map<String, Double>[] splitMap,HashMap<String, Integer> dinstinctValue) throws Exception {
			iteration = createJob(HadoopConstants.JOB_NAME.DecisionTree_Depth +"_" + currDepth, iterConf, DecisionTreePreemptionMapper.class, DecisionTreePreemptionReducer.class, 
					Text.class, Text.class, inputPath, iterOut);
			super.setInputFormatClass(iteration);
			int algorithmLimit =treeRoot.getUniqueId()- finalNodes.size();
//			int numberOfPreemption=this.getMaxReduceNumber();
			if(clusterCapacity == -1) {
			 
				clusterCapacity = algorithmLimit;
			}
			if(algorithmLimit*config.getColumnNames().split(",").length<clusterCapacity)
			{
				clusterCapacity=algorithmLimit*config.getColumnNames().split(",").length;
			}
			iteration.setMapOutputKeyClass(Text.class);
			iteration.setMapOutputValueClass(Text.class);
			iteration.setOutputFormatClass(SequenceFileOutputFormat.class);
			iteration.setNumReduceTasks((int) Math.max(clusterCapacity, 1));
			if(!runMapReduceJob(iteration,true)){
				itsLogger.error("Iteration #" + currDepth  + " failed! Please check paramters and resubmit");
				throw new RuntimeException("Iteration #" + currDepth + " failed! Please check paramters and resubmit");
			}
 
			Map<Integer,String> allNodesInfo=new HashMap<Integer,String>();
	 		FileSystem fs = FileSystem.get(URI.create(iterOut), iterConf);
//	 		FSDataInputStream inTree = fs.open(new Path(iterOut));
	 		FileStatus[] nodes = fs.globStatus(new Path(iterOut).suffix("/part-*"));
			Path[] paths = FileUtil.stat2Paths(nodes);
			boolean nodesToExpand=false;
			SequenceFile.Reader reader = null;
 			for (Path path : paths) {
 				reader = new SequenceFile.Reader(fs, path, iterConf);
// 			ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
 			Writable key = (Writable) ReflectionUtils.newInstance(
					reader.getKeyClass(), iterConf);
			Writable value = (Writable) ReflectionUtils.newInstance(
					reader.getValueClass(), iterConf);

			while(reader.next(key, value)) {
				int node = Integer.parseInt(((Text) key).toString().split(",")[0]);
				
			 
//					int node=Integer.parseInt(lines.split("\t")[0]);
//					String value=lines.split("\t")[1];
					if(allNodesInfo.containsKey(node))
					{
						
						checkMinGain(allNodesInfo,node,value);
						//TODO  check the minigain and decide change it or not
					}
					else{
						allNodesInfo.put(node, value.toString());
					}
				}
			}
		 
			
			for(int node:allNodesInfo.keySet())
			{
				String[] results = allNodesInfo.get(node).split("\t");
				String[] first = results[0].split(",");
				int split = Integer.parseInt(first[0]);
				String continuousVal = first[2];
				HadoopTree currNode = treeRoot.getNode(node);
				
				String[] counts = results[1].split(",");
				int[] distribution = new int[counts.length];		
				
				int i =0;
				 
				for(String s : counts) {
					distribution[i] = Integer.parseInt(s);
					i++;
				}
				
				currNode.setCount(distribution);
				if(!finalNodes.contains(node))
				{
					finalNodes.add(node);
				}
				
				
				if(split != -1){
					int[] nodeDistribution= null;	
					if(results.length>2)//TODO else
					{
						String[] nodeCounts = results[2].split(",");
						nodeDistribution=new int[nodeCounts.length];
						int k =0;
						 
						for(String s : nodeCounts) {
							nodeDistribution[k] = Integer.parseInt(s);
							k++;
						}
					}
					// if node is to be expanded
					// build out the tree appropriately
					 nodesToExpand = true;
					int splitReverse = Arrays.binarySearch(selectedColumnsInt, split);
					int idUpdate = currNode.expand(split, 
							splitMap[splitReverse+1], continuousVal, treeRoot.getUniqueId(), selectedColumnsInt, columnsList, dinstinctValue, nodeDistribution);
					treeRoot.updateUniqueId(idUpdate);
				}
			}
		 return nodesToExpand;
		}

 

		private void checkMinGain(Map<Integer, String> allNodesInfo, int node,
				Writable value) {
			
			if(Double.parseDouble(allNodesInfo.get(node).split("\t")[0].split(",")[1])<
					Double.parseDouble(value.toString().split("\t")[0].split(",")[1]))
			{
				return;
			}else
			{
				allNodesInfo.put(node, value.toString());
			}
		}

		private Map<String, Double>[] generateDistinctMap(Configuration jobConfig)
				throws IOException, Exception {
//				String modelPath = jobConfig.get(DecisionTreeConfigureKeySet.TREE_FILE);
//				boolean nodesToExpand;
//				int featureLength =config.getColumnNames().split(",").length;
//				int featureLength = jobConfig.getInt(DecisionTreeConfigureKeySet.FEATURE_LENGTH, 100);
//				int currDepth = 0;
//				String[] selectedColumns = config.getColumnNames().split(",");
//				int[] selectedColumnsInt = new int[selectedColumns.length];
//				
//				int mm = 0;
//				for(String s : selectedColumns) {
//					selectedColumnsInt[mm] = Integer.parseInt(s);
//					mm++;
//				}
//				
////				int maxDepth = jobConfig.getInt(DecisionTreeConfigureKeySet.MAX_DEPTH, 5);
//				String splitsPath = jobConfig.get(DecisionTreeConfigureKeySet.SPLIT_FILE);
//				int classificationIndex = jobConfig.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
//				String jsonModel;
//				
//				
//				// initialize tree
//				String rootName = jobConfig.get(DecisionTreeConfigureKeySet.ROOT_LABEL);
//				int labelIndex = jobConfig.getInt(DecisionTreeConfigureKeySet.LABEL_INDEX, 0);
//				HadoopTree treeRoot = new HadoopTree(rootName, labelIndex, 0, true, false);
//				treeRoot.setColumnMap(columnsList);
//				treeRoot.setDemap(selectedColumnsInt);
//				treeRoot.setRoot(true);
//				treeRoot.updateUniqueId(1);
//				Gson gson = new GsonBuilder().serializeNulls().create();
//				Type splitType = new TypeToken<HashMap<String, Double>[]>() {}.getType();
//				
				Map<String, Double>[] splitMap = new LinkedHashMap[config.getColumnNames().split(",").length+1];
				ArrayList<Map<String, Double>> buildMap = new ArrayList<Map<String, Double>>(config.getColumnNames().split(",").length+1);
//				
				for(int i = 0; i <  config.getColumnNames().split(",").length+1; i++) {
					buildMap.add(null);
				}
				FileSystem fs = FileSystem.get(URI.create(splitsOut), jobConfig);

//				
				String[] c = {};
				if(jobConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS) != null && !jobConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).isEmpty()) {
					c = jobConfig.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).split(",");
				}
				for(int i = 0; i < c.length; i++) {
					this.continuousColumnsInt.add(i, Integer.parseInt(c[i]))  ;
				}
//				
//				int classifyMap = 0;
//				
//				// build splits data structure
//				Text placeholder = new Text();
//				double bins = Double.parseDouble(jobConfig.get("tree.mr.binCount"));
//				Path[] pathsS = FileUtil.stat2Paths(nodesSplit);
				FileStatus[] nodesSplit = fs.globStatus(new Path(splitsOut).suffix("/part-*"));

				boolean notNull=false;
				long bins=Long.parseLong(config.getNumericalGranularity() == null ? "1000000" : config.getNumericalGranularity());
				HashMap<Integer,Integer> columnSize= new HashMap<Integer,Integer>();
				Map<Integer,List<String>> splitsColumns_new= new TreeMap<Integer,List<String>>();
				Path[] pathsS = FileUtil.stat2Paths(nodesSplit);
				for(Path path : pathsS) {
//					InputStream in = hdfsManager.readHadoopFileToInputStream(splitsOut, this.hadoopConnection);
//					
//					
//					BufferedReader bfReader = new BufferedReader(new InputStreamReader(
//							in));
					Text lines = new Text();
				 
					FSDataInputStream stream = fs.open(path);
				LineReader readLine = new LineReader(stream);
				
				while (readLine.readLine(lines) != 0) {
 					notNull=true;
					String[] line = lines.toString().split("\t");
					String[] type_col=line[0].split("_");
					int col=-1;
					if("Splitcategorical".equals(type_col[0])){
						
						col = Integer.parseInt(type_col[1]);
						if(line.length<2)
						{
							continue;
						}
						String spiltValue = line[1];
						if(splitsColumns_new.get(col)==null)
						{
							List<String> newDataList= new ArrayList<String>();
							newDataList.add(spiltValue);
							splitsColumns_new.put(col, newDataList);
							columnSize.put(col,1);
						}
						else
						{
							splitsColumns_new.get(col).add(spiltValue);
							columnSize.put(col,columnSize.get(col)+1);
						}
						if(columnSize.get(col)>Integer.parseInt(categoryLimit))
						{
							throw new Exception("The "+this.fileStructureModel.getColumnNameList().get(col)+" column have too many distinct value : it is more than "+categoryLimit+"." +
									"Please consider grouping or clustering your data first, or increase the value of the Category Limit parameter.");
						}
					}
					else if("Splitcontinuous".equals(type_col[0])){
						col = Integer.parseInt(type_col[1]);
						if(line.length<2)
						{
							continue;
						}
						String spiltValue = line[1];
						if(splitsColumns_new.get(col)==null){
							List<String> newDataList= new ArrayList<String>();
							newDataList.add(spiltValue);
							splitsColumns_new.put(col, newDataList);
							columnSize.put(col,1);
						}
						else{
							splitsColumns_new.get(col).add(spiltValue);
							columnSize.put(col,columnSize.get(col)+1);
						}
						
						if(columnSize.get(col)>bins)
						{
							throw new Exception("The "+this.fileStructureModel.getColumnNameList().get(col)+" column have too many distinct value : it is more than "+bins+"." +
									"Please consider grouping or clustering your data first, or increase the value of the Numerical Granularity parameter.");
						}
					}
					 
				}
				
				IOUtils.closeStream(stream);
			
				}
			 
			if(notNull==false)
			{
				throw new Exception("A decision tree could not be created because large number of rows had to be discarded due to null values.  Please consider using a null value replacement operator prior to the  decision tree in your workflow.");
			}
				
			//TODO just put the distinct value, if you want to calculate int or long as text, you should judge its size.
			ArrayList<Integer> intToCate=new ArrayList<Integer>();
			for(int tempIndex:columnSize.keySet())
			{
				if(columnSize.get(tempIndex)!=null)
				{
					if(columnSize.get(tempIndex)<=integerDistinct)
					{
						intToCate.add(tempIndex);
					}
				}
			}
			int classificationIndex = jobConfig.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
			if(!intToCate.contains(classificationIndex))
			{
				intToCate.add(classificationIndex);
			}
			continuousColumnsInt.removeAll(intToCate);
			
			continuousColumnsString=continuousColumnsInt.toString();
			continuousColumnsString=continuousColumnsString.substring(1, continuousColumnsString.length()-1).replace(" ", "");
			int j = 0;
			int classifyMap = 0;
			for(Map.Entry<Integer, List<String>> sc : splitsColumns_new.entrySet()){
				int col= sc.getKey();
				List<String> quantilesString = sc.getValue();
				if( continuousColumnsInt.contains(col)) {
					// continuous split
//					ArrayList<Double> splits = new ArrayList<Double>();
					Map<String, Double> colMap = new LinkedHashMap<String, Double>();
					if(quantilesString.size()>bins)//TODO
					{
						throw new Exception("The "+this.fileStructureModel.getColumnNameList().get(col)+" column have too many distinct value : "+quantilesString.size()+" is more than "+bins+"." +
								"Please consider grouping or clustering your data first, or increase the value of the Numerical Granularity parameter.");
//						itsLogger.debug("Column "+this.fileStructureModel.getColumnNameList().get(col)+" have been transformed , its distinct value is more than "+bins);
//						int interval=(int) Math.round(quantilesString.size()/(bins));
//						if(interval==0)
//						{
//							interval=1;
//						}
//						for(int index=0;index<quantilesString.size();)
//						{
//							colMap.put(quantilesString.get(index), Double.parseDouble(quantilesString.get(index)) );
//							index=index+interval;
//						}
//						buildMap.set(j, colMap);
					}else{
						for(String tempSplitValue:quantilesString)
						{
							colMap.put(tempSplitValue, new Double(Double.parseDouble(tempSplitValue)));
						}
						buildMap.set(j, colMap);
					}
				} else {
					if(quantilesString.size()>Integer.parseInt(categoryLimit))//TODO
					{
						
						throw new Exception("The "+this.fileStructureModel.getColumnNameList().get(col)+" column have too many distinct value : "+quantilesString.size()+" is more than "+categoryLimit+"." +
								"Please consider grouping or clustering your data first, or increase the value of the Category Limit parameter.");
					}else
					{
						Double index = 0.0;
						Double value = null;
						for(String l : quantilesString){
						
						String categoricalString = l;

						if(col == classificationIndex) {
							value = index;
							index++;
							classifyMap = j;
						}
//						if(!buildMap.contains(j))
//						{
//							buildMap.
//						}
						
						if(buildMap.get(j) != null) {
							buildMap.get(j).put(categoricalString, value);
						} else {
							HashMap<String, Double> tempMap = new LinkedHashMap<String, Double>();
							tempMap.put(categoricalString, value);
							buildMap.set(j, tempMap);
								}
						}
					}
				}
				
				j++;
			}
//			IOUtils.closeStream(stream);
			 
			
			
			s_runtime.freeMemory(); 
			// reformat splits map
			Map<String, Double> classify = buildMap.remove(classifyMap);
			buildMap.add(0, classify);
//			Runtime.freeMemory();
			splitMap = (HashMap<String, Double>[]) buildMap.toArray(splitMap);
			return splitMap;
		}
	}
 