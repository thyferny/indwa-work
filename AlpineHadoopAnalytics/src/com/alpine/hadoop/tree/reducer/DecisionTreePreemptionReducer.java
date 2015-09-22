/**
 * 

* ClassName DecisionTreePreemptionReducer.java
*
* Version information: 1.00
*
* Date: 2013-1-15
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.tree.reducer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.CategoriCondition;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.hadoop.tree.model.IntegerArrayWritable;
import com.alpine.hadoop.tree.model.NumericCondition;
import com.alpine.hadoop.tree.model.TreeCondition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Shawn
 *
 *  
 */

public class DecisionTreePreemptionReducer extends Reducer<Text, Text, Text, Text>{

	static final double LOG2 = Math.log(2.0);
	float minGain;
	int minNodeSize;
	private HashMap<String, Double>[] splits;
	private HashMap<String, Double> newSplits ;
	ArrayList<Integer> continuousCol = new ArrayList<Integer>();
	String continuousSplit;
	List<String> featureVec = new ArrayList<String>();
	private HadoopTree model;
	private int classificationIndex;
	private int[] colOrdered;
	private String[] columnIndexs;
	private List<Integer> unUsedColumns=new ArrayList<Integer>();
	private Text writeKey=new Text();
	private Text writeValue=new Text();
	private StringBuffer total=new StringBuffer();
	private IntegerArrayWritable writablValues=null;
	
	private static Logger itsLogger = Logger
            .getLogger(DecisionTreeReducer.class);
	
	public void reduce(Text key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {
		String[] splitKey = key.toString().split(",");
		String node=splitKey[0];
		HashMap<String, int[]> dataCounts=new HashMap<String, int[]>();
		HashMap<String, int[]> dataGreaterCounts=null;
		int columnid=Integer.parseInt(splitKey[1]);
//		int[] vals=null;
//		colOrdered.
		boolean categorical = null == splits[columnid+1].values().iterator().next();
		if(categorical) 
		{
//			 vals = new int[splits[0].size()];
		}else{
//			 vals = new int[2*splits[0].size()];
			dataGreaterCounts=new HashMap<String, int[]>();
		}
		IntWritable keyN = new IntWritable(Integer.parseInt(node));

		double totalEntropy;
		ArrayList<TreeCondition> resultsFeatures=new ArrayList<TreeCondition>();
		model.foundFeatures(keyN.get(), resultsFeatures);
		TreeCondition totalCondition=null;
		 
			newSplits=new  HashMap<String, Double>();
		 
		if(resultsFeatures.size()!=0)
		{
			for(TreeCondition tempCondition:resultsFeatures)
			{
				if( colOrdered[columnid]==tempCondition.getCoditionColumnIndex())
				{
					if(!categorical)
					{
						if(totalCondition==null)
						{
							totalCondition=tempCondition;
						}
						else{
							if(((NumericCondition) tempCondition).getMaxConditionValue()<((NumericCondition)totalCondition).getMaxConditionValue())
							{
								((NumericCondition)totalCondition).setMaxConditionValue(((NumericCondition) tempCondition).getMaxConditionValue());
							}
							if(((NumericCondition) tempCondition).getMinConditionValue()>((NumericCondition)totalCondition).getMinConditionValue())
							{
								((NumericCondition)totalCondition).setMinConditionValue(((NumericCondition) tempCondition).getMinConditionValue());
							}
						}
					}
					else
					{
						//error here
						return ;
					}
				}
			}
			if(totalCondition!=null)
			{
				if(categorical)
				{
					newSplits=splits[columnid+1];
//					return ; //do nothing here
				}
				else
				{
//					HashMap<String, Double> newConditon=new HashMap<String, Double>();
					NumericCondition realCondition=(NumericCondition) totalCondition;
					for(String tempValue:splits[columnid+1].keySet())
					{
						try{
						if(Double.parseDouble(tempValue)>=realCondition.getMinConditionValue()
							&&Double.parseDouble(tempValue)<=realCondition.getMaxConditionValue())
						{
							newSplits.put(tempValue, splits[columnid+1].get(tempValue));
						}
						}
						catch(Exception e)
						{
							int whatisthefuck=0;
							whatisthefuck++;
						}
					}
//					newSplits=newConditon;
				}
			}
			else
			{
				newSplits=splits[columnid+1];
			}
		}
		else
		{
			newSplits=splits[columnid+1];
		}
//			ArrayList<Map<String, Double>> buildMap = new ArrayList<Map<String, Double>>(splits.length);
//			buildMap.add(splits[0]);
	
//			newSplits=new HashMap<String, Double>[]();
//			buildMap.add(e)
//				for(int i=1;i<splits.length;i++)
//				{
//					if(totalCondition.getCoditionColumnIndex()== colOrdered[i-1])
//					{
//						 
			
//							buildMap.add(newConditon);
						
//						}
//					}else
//					{
//						buildMap.add(null);
//					}
			 
			
//			} else
//			{
//				newSplits=splits;
//			}
			
		
		if(categorical)
		{
			for(String distincValue:newSplits.keySet())
			{
				if(!dataCounts.containsKey(distincValue)){
					int[] vals = new int[splits[0].size()];
					dataCounts.put(distincValue, vals);
				}
			}
		}
		
		
		
		continuousSplit = "NULL";
		double min = Double.MAX_VALUE;
		int feature = -1;
		int[]  totVals = new int[splits[0].size()];
		
		for(Text iterationValue : values) {
		
		String realValues[]=iterationValue.toString().split("\t");
		String category = realValues[1];
		int index = splits[0].get(category).intValue();
		totVals[index] += 1;
//		vals[index]++;
		
		// first hash in splits is a mapping of classification variable
		// to index in counts array
		if(splits.length==0||splits[0]==null||splits[0].get(category)==null){
			return;
		}
		 
		writablValues= new IntegerArrayWritable();
		
			 
			if(categorical) {
				if(!dataCounts.containsKey(realValues[0])){
					int[] vals = new int[splits[0].size()];
					vals[index] = 1;
					dataCounts.put(realValues[0], vals);
				} else {
				dataCounts.get(realValues[0])[index]++;				//TODO
 				}
			} else {
				for(Entry<String, Double> compare : newSplits.entrySet()) {
					try{
					if(Double.parseDouble(realValues[0]) <= compare.getValue()) {
						if(!dataCounts.containsKey(compare.getKey())){
							int[] vals = new int[splits[0].size()];
							vals[index] = 1;
							dataCounts.put(compare.getKey(), vals);
						} else {
							dataCounts.get(compare.getKey())[index]++;
						}
						// counts hash is indexed as 2 * splits index.  
						// even i is less than, odd i is greater than counts
					} else {
						if(!dataGreaterCounts.containsKey(compare.getKey())){
							int[] vals = new int[splits[0].size()];
							vals[index] = 1;
							dataGreaterCounts.put(compare.getKey(), vals);
						} else {
							dataGreaterCounts.get(compare.getKey())[index]++;
						}
					}
				}catch(Exception e)
				{
					int whatis=0;
					whatis++;
				}
				}
			}
			 
		}
		
		
//		int[] totalDistribution =(int[]) nodeCounts[0].get("total");
// 		if(totalDistribution==null){
//			return;
//		}
		int[] nodesDistribution= null;
 		int recordsSeen = 0; 
		double miniGain;
		StringBuffer distribution=new StringBuffer();
		for(int t : totVals) {
			recordsSeen += t;
			distribution.append(t+",");
		}
		if(distribution.length()>1)
		{
			distribution.deleteCharAt(distribution.length()-1);
		}
		if(recordsSeen < minNodeSize) {
//			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal + "\t" + distribution));
			return;
		}
		double tempGain;
		if(categorical)
		{
			tempGain= mapEntropy(dataCounts, recordsSeen);
		}
		else
		{	
			tempGain  =compressContinuous(dataCounts, dataGreaterCounts, recordsSeen);
		}
		if(tempGain < min) {
			min = tempGain;
			feature =colOrdered[columnid];
//			continuousSplitVal = this.continuousSplit;
			if(this.continuousCol.contains(colOrdered[columnid])) {
				nodesDistribution=new int[totVals.length*2];
//			nodeCounts[i*2].get(continuousSplitVal);
//			nodeCounts[i*2+1].get(continuousSplitVal);
				for(int k=0;k<totVals.length;k++)
				{
					if( ((int[])dataCounts.get(continuousSplit))!=null){
						nodesDistribution[k]=((int[])dataCounts.get(continuousSplit))[k];
					}
					else{
						nodesDistribution[k]=0;
					}
					if(( dataGreaterCounts.get(continuousSplit))!=null)
					{
						nodesDistribution[k+totVals.length]=(dataGreaterCounts.get(continuousSplit))[k];
					}else
					{
						nodesDistribution[k+totVals.length]=0;
					}
				}
			 
			}else
			{
				nodesDistribution=new int[totVals.length*dataCounts.size()];
				int totalIndex=0;
				for( Object tempKey: dataCounts.keySet() )
				{
					for(int k=0;k<totVals.length;k++)
					{
						nodesDistribution[totalIndex]=(dataCounts.get(tempKey))[k];
						totalIndex=totalIndex+1;
					}
				}
			}
		totalEntropy = shannonEntropy(totVals, recordsSeen);
//		writeKey.set(splitKey[0]+","+"columnid"+countKey.toString().replaceAll(",", "_"));
//		total.setLength(0);
//		for(int countValue:dataCounts.get(countKey))
//		{
//			total.append(countValue).append(",");
//		}
		
		if((totalEntropy - min) < this.minGain) {
			 
			return;
		}
		
		StringBuffer nodeDistribution=new StringBuffer();

		for(int t : nodesDistribution) {
			nodeDistribution.append(t+",");
		}
		if(nodesDistribution.length>1)
		{
			nodeDistribution.deleteCharAt(nodeDistribution.length()-1);
		}
		context.write(new Text( keyN+""), new Text(colOrdered[columnid]+","+min+","+ continuousSplit +"\t" + distribution+ "\t" + nodeDistribution));

//		total.deleteCharAt(total.length()-1);
//		writeValue.set(total.toString());
//		context.write(writeKey, writeValue);
//		if((totalEntropy - min) < this.minGain) {
////			context.write(keyN, new Text(String.valueOf(-1) + "," + continuousSplitVal+"\t" + distribution));
//			return;
//		}else
//		{
//			StringBuffer nodeDistribution=new StringBuffer();
//		}
		}
//			for(int t : nodesDistribution) {
//				nodeDistribution.append(t+",");
//			}
//			if(nodesDistribution.length>1)
//			{
//				nodeDistribution.deleteCharAt(nodeDistribution.length()-1);
//			}
//			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal +"\t" + distribution+ "\t" + nodeDistribution));

		
		
		// first item of counts hash is a HashMap with the total unsplit counts
//		hash[0].put("total", totVals);
		
		
		
		
		
//		SplitMappingWritable aggregateHash = null;
//		HashMap[] nodeCounts=null;
//		// aggregate the count hashes for this node
//		for(Text iterationValue : values) {
//			featureVec.clear();
//			for(String data:iterationValue.toString().split(AlpineHadoopConstants.SPECIAL_SEP_STRING))
//			{
//				featureVec.add(data);
//			}
//			
//			if(nodeCounts==null)
//			{
//				nodeCounts=new LinkedHashMap[featureVec.size() * 2];
//				for(int  i = 0; i < nodeCounts.length; i++) {
//					nodeCounts[i] = new LinkedHashMap<String, int[]>();
//				}
//					
//				for(int  i = 1; i < splits.length; i++) {	
//					boolean categorical = null ==splits[i].values().iterator().next();
//					if(categorical)
//					{
//						for(Object distincValue:splits[i].keySet())
//						{
//							if(!nodeCounts[i * 2].containsKey(distincValue)){
//								int[] vals = new int[splits[0].size()];
//								nodeCounts[i * 2].put(distincValue, vals);
//							}
//						}
//					}
//				}
//			}
////			for(int i = 0; i < nodeCounts.length; i++) {
////				nodeCounts[i].clear();
////			}
//			SplitMappingWritable.populateHash(nodeCounts, featureVec, splits);
// 
//		}
////		MapWritable[] nodeCounts = (MapWritable[]) aggregateHash.toArray();
//		double[] entropies = new double[nodeCounts.length/2-1];
//		this.continuousSplit = new String[nodeCounts.length/2-1];
// 
//		for(int p = 0; p < this.continuousSplit.length; p++){
//			this.continuousSplit[p] = "NULL";
//		}
//		int[] totalDistribution =(int[]) nodeCounts[0].get("total");
// 		if(totalDistribution==null){
//			return;
//		}
// 		int[] nodesDistribution= null;
////		IntWritable[] total = (IntWritable[]) totalDistribution.toArray();
//		
//		// aggregate the total records processed at this node
//		int recordsSeen = 0;
//		StringBuffer distribution=new StringBuffer();
//
//		for(int t : totalDistribution) {
//			recordsSeen += t;
//			distribution.append(t+",");
//		}
//		distribution.deleteCharAt(distribution.length()-1);
//				
//		// if there are not enough records, return
//		if(recordsSeen < minNodeSize) {
//			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal + "\t" + distribution));
//			return;
//		}
//		
//		if(splitKey.length < 2) {
//			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal + "\t" + distribution));
//			return;
//		}
//
//		String[] colMap = splitKey[1].split(",");
//		
//		// calculate entropies
//		for(int i = 1; i < entropies.length + 1; i++) {
//				if(this.continuousCol.contains(Integer.parseInt(colMap[i-1]))) {
//					entropies[i -1] = compressContinuous(nodeCounts[i * 2], nodeCounts[i * 2 + 1], recordsSeen, i-1);
//				} else {
//					entropies[i -1] = mapEntropy(nodeCounts[i * 2], recordsSeen);
//				}
//				if(entropies[i -1] < min) {
//					min = entropies[i -1];
//					feature = Integer.parseInt(colMap[i -1]);
//					continuousSplitVal = this.continuousSplit[i -1];
//					if(this.continuousCol.contains(Integer.parseInt(colMap[i-1]))) {
//						nodesDistribution=new int[totalDistribution.length*2];
////					nodeCounts[i*2].get(continuousSplitVal);
////					nodeCounts[i*2+1].get(continuousSplitVal);
//						for(int k=0;k<totalDistribution.length;k++)
//						{
//							if( ((int[])nodeCounts[i*2].get(continuousSplitVal))!=null){
//								nodesDistribution[k]=((int[])nodeCounts[i*2].get(continuousSplitVal))[k];
//							}
//							else{
//								nodesDistribution[k]=0;
//							}
//							if(((int[])nodeCounts[i*2+1].get(continuousSplitVal))!=null)
//							{
//								nodesDistribution[k+totalDistribution.length]=((int[])nodeCounts[i*2+1].get(continuousSplitVal))[k];
//							}else
//							{
//								nodesDistribution[k+totalDistribution.length]=0;
//							}
//						}
//					 
//					}else
//					{
//						nodesDistribution=new int[totalDistribution.length*nodeCounts[i*2].size()];
//						int totalIndex=0;
//						for( Object tempKey: nodeCounts[i*2].keySet() )
//						{
//							for(int k=0;k<totalDistribution.length;k++)
//							{
//								nodesDistribution[totalIndex]=((int[])nodeCounts[i*2].get(tempKey))[k];
//								totalIndex=totalIndex+1;
//							}
//						}
//					}
//				}
//		}
//		
//	 
//			
//		 
//		totalEntropy = shannonEntropy(totalDistribution, recordsSeen);
//		
//		
//		if((totalEntropy - min) < this.minGain) {
//			context.write(keyN, new Text(String.valueOf(-1) + "," + continuousSplitVal+"\t" + distribution));
//			return;
//		}
////		nodeCounts		
//		// Text of: feature index, continuous split value, node count
//		StringBuffer nodeDistribution=new StringBuffer();
//
//		for(int t : nodesDistribution) {
//			nodeDistribution.append(t+",");
//		}
//		nodeDistribution.deleteCharAt(nodeDistribution.length()-1);
//		context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal +"\t" + distribution+ "\t" + nodeDistribution));
//		System.out.println("DecisionTreereducer()  over");
	}
		
	private double compressContinuous(HashMap nodeCounts2, HashMap nodeCounts3, double totalLength) {
		HashMap<String, Double> entropyHash = new HashMap<String, Double>();
		String splitVal = null;
		double minEntropy = Double.MAX_VALUE;
		
		for(Object mapEl : nodeCounts2.entrySet()) {		
			String key = (String) ((Entry<Object, Object>)mapEl).getKey();
			
			int[] valLessThan = (int[])  ((Entry<Object, Object>)mapEl).getValue();
			int[] valGreaterThan =   (int[]) nodeCounts3.get(key);
			int[] countArrGreaterThan;
			
			if(valGreaterThan == null) {
				countArrGreaterThan = new int[0];
			} else {
				countArrGreaterThan =   valGreaterThan ;
			}
			
			int[] countArrLessThan =   valLessThan ;			

			double totalEnt = shannonEntropy(countArrLessThan, totalLength) + shannonEntropy(countArrGreaterThan, totalLength);
			
			entropyHash.put(key.toString(), totalEnt);
		}
		
		for(Entry<String, Double> entropyEl : entropyHash.entrySet()) {
			String idx = entropyEl.getKey();
			double val = entropyEl.getValue();
			
			if(val < minEntropy) {
				minEntropy = val;
				splitVal = idx;
			}
		}
		
		this.continuousSplit = splitVal;
		
		return minEntropy;
	}

	public double mapEntropy(HashMap nodeCounts2, double totalLength) {
		double entropy = 0.0;
		 
		// each K,V represents a value of this feature split
		for(Object mapEl : nodeCounts2.entrySet() ) {
//		for(Entry<Object, Object> mapEl : nodeCounts2.entrySet() ) {
			int[] val =  (int[]) ((Entry<Object, Object>)mapEl).getValue();
			int[] countArr =   val;
						
			entropy += shannonEntropy(countArr, totalLength);
		}
		
		return entropy;
	}
	
	public double shannonEntropy(int[] arr, double totalLength) {
		double shannEnt = 0.0;
		double subLength = 0.0;
		for(int t : arr) {
			subLength += t ;
		}
		
		double probTotal = subLength / totalLength;
		
		for(int feat : arr) {
			double prob = ((double) feat ) / subLength;
			if(prob > 0) {
				shannEnt -= prob * (Math.log(prob) / LOG2);
			}
		}
		return probTotal * shannEnt;
	}
	
	protected void setup(Context context) {
		Gson gson = new GsonBuilder().serializeNulls().create();
		Type splitType = new TypeToken<HashMap<String, Double>[]>() {}.getType();
		Configuration config = context.getConfiguration();
		this.classificationIndex = config.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
		minGain = config.getFloat(DecisionTreeConfigureKeySet.MINIMUM_GAIN, 0.1f);
		minNodeSize = config.getInt(DecisionTreeConfigureKeySet.MINIMUM_NODE_SIZE, 4);
		FileSystem fs;
		FSDataInputStream in2 = null;
		ByteArrayOutputStream buf2 = null;
		ByteArrayOutputStream buf1 = null;
		buf1 = new ByteArrayOutputStream();
		columnIndexs = config.get(DecisionTreeConfigureKeySet.COLUMNS).split(",");
		this.colOrdered = new int[columnIndexs.length];
		int i = 0;
		for (String select : this.columnIndexs) {
			colOrdered[i] = Integer.parseInt(select);
//			usedColumns.add(Integer.parseInt(select));
			i++;
		}
		
		try {
			fs = FileSystem.get(config);
			FSDataInputStream in1 = null;
			String modelFile = config.get(DecisionTreeConfigureKeySet.TREE_FILE);
			in1 = fs.open(new Path(modelFile));
			IOUtils.copyBytes(in1, buf1, 4096, false);
			String modelJson = buf1.toString();
			this.model = gson.fromJson(modelJson, HadoopTree.class);

		String splitsFile = config.get(DecisionTreeConfigureKeySet.SPLIT_FILE);
	 
		this.model = gson.fromJson(modelJson, HadoopTree.class);
		in2 = fs.open(new Path(splitsFile));
		buf2 = new ByteArrayOutputStream();
		IOUtils.copyBytes(in2, buf2, 4096, false);
		String splitsJson = buf2.toString();
		this.splits = gson.fromJson(splitsJson, splitType);
		String continuous = config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS);
		if(continuous != null && !continuous.equals("")) {
			for(String col : config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).split(",")) {
				this.continuousCol.add(new Integer(col));
			}
		}
		} catch (Exception e) {
			itsLogger.error("Cannot initialize Splits:", e);
            throw new IllegalArgumentException("Cannot initialize Split mappings:"); 
		} finally {
 
			IOUtils.closeStream(in2);
 
			IOUtils.closeStream(buf2);
		}
	}
}
