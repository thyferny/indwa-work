
package com.alpine.hadoop.tree.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.log4j.Logger;

import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.HadoopTree;
import com.alpine.hadoop.tree.model.IntegerArrayWritable;
import com.alpine.hadoop.tree.model.SplitMappingWritable;
import com.alpine.hadoop.util.MapReduceHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;



public class DecisionTreePreemptionMapper extends Mapper<LongWritable, Text, Text, Text>{
	private static Logger itsLogger = Logger
            .getLogger(DecisionTreeMapper.class);
	MapReduceHelper helper;
	private List<Integer> usedColumns;
	private List<Integer> finalNodesList;
	private int[] colOrdered;
	private HadoopTree model;
	private int classificationIndex;
	private HashMap<String, Double>[] splits;
	private Configuration config;
	private String[] columnIndexs;
	List<String> featureVec = new ArrayList<String>();
	HashMap[] nodeCounts=null;
	Text keyValue=new Text();
	Text dataValue=new Text();
	SplitMappingWritable writable = null;
	MapWritable[] newMap = null;
	StringBuffer realValue= new StringBuffer();
	IntegerArrayWritable newArr = new IntegerArrayWritable();
	IntWritable intValue=new IntWritable();
	public void map(LongWritable key, Text value, Context context)
			throws IOException,InterruptedException {
		helper.setInvolvedColumnIds(usedColumns);
		List<String[]> lines = helper.getCleanData(value, false);
		if(lines!=null){
				for(String[] vec:lines){
					if(vec.length>0)
					{
						featureVec.clear();
						int i = 1;
						for (int pt : this.colOrdered)  {
							String tempValue = vec[pt].trim();
							featureVec.add(tempValue);	
							i++;
						}
						int node = model.computeNode(featureVec);
					 
						if(this.finalNodesList.contains(node))
						{
							return;
						}	
						int j=0;
						for(String selectedValue:featureVec)
						{
							
							keyValue.set(node + "," + j);
							dataValue.set(selectedValue+"\t"+vec[this.classificationIndex].trim());
							context.write(keyValue, dataValue);
							j++;
						}
	
					}
				}
			}
		}

	
	
	protected void setup(Context context) {
		Gson gson = new GsonBuilder().serializeNulls().create();
		Type splitType = new TypeToken<HashMap<String, Double>[]>() {}.getType();
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		config = context.getConfiguration();
		columnIndexs = config.get(DecisionTreeConfigureKeySet.COLUMNS).split(",");
		this.colOrdered = new int[columnIndexs.length];
		usedColumns=new ArrayList<Integer>();
		int i = 0;
		for (String select : this.columnIndexs) {
			colOrdered[i] = Integer.parseInt(select);
			usedColumns.add(Integer.parseInt(select));
			i++;
		}
		
		Arrays.sort(colOrdered);
		FileSystem fs;
		FSDataInputStream in1 = null;
		FSDataInputStream in2 = null;
		ByteArrayOutputStream buf1 = null;
		ByteArrayOutputStream buf2 = null;
		
		String modelFile = config.get(DecisionTreeConfigureKeySet.TREE_FILE);
		String splitsFile = config.get(DecisionTreeConfigureKeySet.SPLIT_FILE);
		
		this.classificationIndex = config.getInt(DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0);
		usedColumns.add(this.classificationIndex);
		try {
		fs = FileSystem.get(config);
		in1 = fs.open(new Path(modelFile));
		in2 = fs.open(new Path(splitsFile));
		// read tree model
		buf1 = new ByteArrayOutputStream();
		IOUtils.copyBytes(in1, buf1, 4096, false);
		String modelJson = buf1.toString();
		//read splits hash
		buf2 = new ByteArrayOutputStream();
		IOUtils.copyBytes(in2, buf2, 4096, false);
		String splitsJson = buf2.toString();
		// deserialize string
		this.model = gson.fromJson(modelJson, HadoopTree.class);
		this.splits = gson.fromJson(splitsJson, splitType);
		this.finalNodesList=new ArrayList<Integer>();
		if(config.get(DecisionTreeConfigureKeySet.FINALNODESLIST)!=null)
		{
			for(String finalNode:config.get(DecisionTreeConfigureKeySet.FINALNODESLIST).split(","))
			{
				finalNodesList.add(Integer.parseInt(finalNode));
			}
		}
		
		
		} catch (Exception e) {
			itsLogger.error("Cannot initialize Splits:", e);
            throw new IllegalArgumentException("Cannot initialize Split mappings:"); 
		} finally {
			IOUtils.closeStream(in1);
			IOUtils.closeStream(in2);
			IOUtils.closeStream(buf1);
			IOUtils.closeStream(buf2);
		}
	}
}
