/**
*
* ClassName DecisionTreeReducer.java
*
* Version information: 1.00
*
* Aug 31, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.reducer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.hadoop.tree.model.NumericCondition;
import com.alpine.hadoop.tree.model.SplitMappingWritable;
import com.alpine.hadoop.tree.model.TreeCondition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Jonathan
 *  
 */

public class DecisionTreeReducer extends Reducer<Text, Text, IntWritable, Text> { 
	
	static final double LOG2 = Math.log(2.0);
	float minGain;
	int minNodeSize;
	private HashMap<String, Double>[] splits;
	private HashMap<String, Double>[] newSplits ;
	ArrayList<Integer> continuousCol = new ArrayList<Integer>();
	String continuousSplit[];
	private int[] colOrdered;
	private String[] columnIndexs;
	private List<Integer> unUsedColumns=new ArrayList<Integer>();
	List<String> featureVec = new ArrayList<String>();
	private HadoopTree model;
//	private int classificationIndex;
	
	private static Logger itsLogger = Logger
            .getLogger(DecisionTreeReducer.class);
	
	public void reduce(Text key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {
//		System.out.println("DecisionTreereducer()  come in");
		String[] splitKey = key.toString().split("\t");
		IntWritable keyN = new IntWritable(Integer.parseInt(splitKey[0]));
		double totalEntropy;
				String continuousSplitVal = "NULL";
		double min = Double.MAX_VALUE;
		int feature = -1;
		ArrayList<TreeCondition> resultsFeatures=new ArrayList<TreeCondition>();
		model.foundFeatures(keyN.get(), resultsFeatures);
		if(resultsFeatures.size()!=0)
		{
			Map<Integer ,TreeCondition> totalConditions=new HashMap<Integer,TreeCondition>();
		
			for(TreeCondition tempCondition:resultsFeatures)
			{
				if(!totalConditions.keySet().contains(tempCondition.getCoditionColumnIndex()))
				{
					totalConditions.put(tempCondition.getCoditionColumnIndex(), tempCondition);
				}
				else{
					if(tempCondition instanceof NumericCondition )
					{
						if(((NumericCondition) tempCondition).getMaxConditionValue()<((NumericCondition)totalConditions.get(tempCondition.getCoditionColumnIndex())).getMaxConditionValue())
						{
							((NumericCondition)totalConditions.get(tempCondition.getCoditionColumnIndex())).setMaxConditionValue(((NumericCondition) tempCondition).getMaxConditionValue());
						}
						if(((NumericCondition) tempCondition).getMinConditionValue()>((NumericCondition)totalConditions.get(tempCondition.getCoditionColumnIndex())).getMinConditionValue())
						{
							((NumericCondition)totalConditions.get(tempCondition.getCoditionColumnIndex())).setMinConditionValue(((NumericCondition) tempCondition).getMinConditionValue());
						}
					}
					else
					{
						//error here
					}
				}
			}
			ArrayList<Map<String, Double>> buildMap = new ArrayList<Map<String, Double>>(splits.length);
			buildMap.add(splits[0]);
	

//		newSplits=new HashMap<String, Double>[]();
			for(int i=1;i<splits.length;i++)
			{
				if(!totalConditions.keySet().contains(colOrdered[i-1]))
				{
//					boolean categorical = null ==splits[i].values().iterator().next();
					buildMap.add(splits[i]);
			
				}else{
					boolean categorical = null ==splits[i].values().iterator().next();
					if(categorical)
					{
						buildMap.add(null);
						unUsedColumns.add(i);
					}
					else
					{
						HashMap<String, Double> newConditon=new HashMap<String, Double>();
						NumericCondition realCondition=(NumericCondition) totalConditions.get(colOrdered[i-1]);
						for(String tempValue:splits[i].keySet())
						{
							if(Double.parseDouble(tempValue)>=realCondition.getMinConditionValue()
								&&Double.parseDouble(tempValue)<=realCondition.getMaxConditionValue())
							{
								newConditon.put(tempValue, splits[i].get(tempValue));
							}
						}
						buildMap.add(newConditon);
					
					}
				}
			}
			newSplits=new HashMap[buildMap.size()];
			newSplits = (HashMap<String, Double>[]) buildMap.toArray(newSplits);
		}else
		{
			newSplits=splits;
		}
		
		
		SplitMappingWritable aggregateHash = null;
		HashMap[] nodeCounts=null;
		// aggregate the count hashes for this node
		for(Text iterationValue : values) {
			featureVec.clear();
			for(String data:iterationValue.toString().split(AlpineHadoopConstants.SPECIAL_SEP_STRING))
			{
				featureVec.add(data);
			}
			
			if(nodeCounts==null)
			{
				nodeCounts=new LinkedHashMap[featureVec.size() * 2];
				for(int  i = 0; i < nodeCounts.length; i++) {
					nodeCounts[i] = new LinkedHashMap<String, int[]>();
				}
					
				for(int  i = 1; i < newSplits.length; i++) {	
					if(newSplits[i]==null)
					{
						continue;
					}
					boolean categorical = null ==newSplits[i].values().iterator().next();
					if(categorical)
					{
						for(Object distincValue:newSplits[i].keySet())
						{
							if(!nodeCounts[i * 2].containsKey(distincValue)){
								int[] vals = new int[newSplits[0].size()];
								nodeCounts[i * 2].put(distincValue, vals);
							}
						}
					}
				}
			}
//			for(int i = 0; i < nodeCounts.length; i++) {
//				nodeCounts[i].clear();
//			}
			SplitMappingWritable.populateHash(nodeCounts, featureVec, newSplits);
 
		}
//		MapWritable[] nodeCounts = (MapWritable[]) aggregateHash.toArray();
		double[] entropies = new double[nodeCounts.length/2-1];
		this.continuousSplit = new String[nodeCounts.length/2-1];
 
		for(int p = 0; p < this.continuousSplit.length; p++){
			this.continuousSplit[p] = "NULL";
		}
		int[] totalDistribution =(int[]) nodeCounts[0].get("total");
 		if(totalDistribution==null){
			return;
		}
 		int[] nodesDistribution= null;
//		IntWritable[] total = (IntWritable[]) totalDistribution.toArray();
		
		// aggregate the total records processed at this node
		int recordsSeen = 0;
		StringBuffer distribution=new StringBuffer();

		for(int t : totalDistribution) {
			recordsSeen += t;
			distribution.append(t+",");
		}
		distribution.deleteCharAt(distribution.length()-1);
				
		// if there are not enough records, return
		if(recordsSeen < minNodeSize) {
			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal + "\t" + distribution));
			return;
		}
		
		if(splitKey.length < 2) {
			context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal + "\t" + distribution));
			return;
		}

		String[] colMap = splitKey[1].split(",");
		
		// calculate entropies
		for(int i = 1; i < entropies.length + 1; i++) {
				if(unUsedColumns.contains(i))
				{
					entropies[i -1]=Double.MAX_VALUE;
					continue;
				}
				if(this.continuousCol.contains(Integer.parseInt(colMap[i-1]))) {
					entropies[i -1] = compressContinuous(nodeCounts[i * 2], nodeCounts[i * 2 + 1], recordsSeen, i-1);
				} else {
					entropies[i -1] = mapEntropy(nodeCounts[i * 2], recordsSeen);
				}
				if(entropies[i -1] < min) {
					min = entropies[i -1];
					feature = Integer.parseInt(colMap[i -1]);
					continuousSplitVal = this.continuousSplit[i -1];
					if(this.continuousCol.contains(Integer.parseInt(colMap[i-1]))) {
						nodesDistribution=new int[totalDistribution.length*2];
//					nodeCounts[i*2].get(continuousSplitVal);
//					nodeCounts[i*2+1].get(continuousSplitVal);
						for(int k=0;k<totalDistribution.length;k++)
						{
							if( ((int[])nodeCounts[i*2].get(continuousSplitVal))!=null){
								nodesDistribution[k]=((int[])nodeCounts[i*2].get(continuousSplitVal))[k];
							}
							else{
								nodesDistribution[k]=0;
							}
							if(((int[])nodeCounts[i*2+1].get(continuousSplitVal))!=null)
							{
								nodesDistribution[k+totalDistribution.length]=((int[])nodeCounts[i*2+1].get(continuousSplitVal))[k];
							}else
							{
								nodesDistribution[k+totalDistribution.length]=0;
							}
						}
					 
					}else
					{
						nodesDistribution=new int[totalDistribution.length*nodeCounts[i*2].size()];
						int totalIndex=0;
						for( Object tempKey: nodeCounts[i*2].keySet() )
						{
							for(int k=0;k<totalDistribution.length;k++)
							{
								nodesDistribution[totalIndex]=((int[])nodeCounts[i*2].get(tempKey))[k];
								totalIndex=totalIndex+1;
							}
						}
					}
				}
		}
		
	 
			
		 
		totalEntropy = shannonEntropy(totalDistribution, recordsSeen);
		
		
		if((totalEntropy - min) < this.minGain) {
			context.write(keyN, new Text(String.valueOf(-1) + "," + continuousSplitVal+"\t" + distribution));
			return;
		}
//		nodeCounts		
		// Text of: feature index, continuous split value, node count
		StringBuffer nodeDistribution=new StringBuffer();

		for(int t : nodesDistribution) {
			nodeDistribution.append(t+",");
		}
		if(nodesDistribution.length>1)
		{
			nodeDistribution.deleteCharAt(nodeDistribution.length()-1);
		}
		context.write(keyN, new Text(String.valueOf(feature) + "," + continuousSplitVal +"\t" + distribution+ "\t" + nodeDistribution));
//		System.out.println("DecisionTreereducer()  over");
	}
		
	private double compressContinuous(HashMap nodeCounts2, HashMap nodeCounts3, double totalLength, int contInd) {
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
		
		this.continuousSplit[contInd] = splitVal;
		
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
//		this.classificationIndex = config.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
		minGain = config.getFloat(DecisionTreeConfigureKeySet.MINIMUM_GAIN, 0.1f);
		minNodeSize = config.getInt(DecisionTreeConfigureKeySet.MINIMUM_NODE_SIZE, 4);
		ByteArrayOutputStream buf1 = null;
		buf1 = new ByteArrayOutputStream();
		FileSystem fs;
		FSDataInputStream in2 = null;
		ByteArrayOutputStream buf2 = null;
		try {
		fs = FileSystem.get(config);
		FSDataInputStream in1 = null;
		String modelFile = config.get(DecisionTreeConfigureKeySet.TREE_FILE);
		in1 = fs.open(new Path(modelFile));
		IOUtils.copyBytes(in1, buf1, 4096, false);
		String modelJson = buf1.toString();
		this.model = gson.fromJson(modelJson, HadoopTree.class);
 
		columnIndexs = config.get(DecisionTreeConfigureKeySet.COLUMNS).split(",");
		colOrdered = new int[columnIndexs.length];
		int i = 0;
		for (String select : this.columnIndexs) {
			colOrdered[i] = Integer.parseInt(select);
			 
			i++;
		}
		
		Arrays.sort(colOrdered);
		String splitsFile = config.get(DecisionTreeConfigureKeySet.SPLIT_FILE);

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
