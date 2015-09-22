/**
 * 
 * ClassName UnionReducer.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.union;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.UnionKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author John Zhao
 * 
 */

public class UnionReducer extends Reducer<Text, Text, Text, Text> {

	MapReduceHelper helper;
	public static final String UNION = "UNION";
	public static final String UNION_ALL = "UNION ALL";
	public static final String INTERSECT = "INTERSECT";
	public static final String EXCEPT = "EXCEPT";
	Text txtWriter = new Text();

	List<String> fileCountList = new ArrayList<String> ();

	private String unionType;
	private String firstTable;
	List<String> outputNames;
	List<String> outputTypes;
	private List<String> inputFiles;
	private ArrayList<List<String>> inputColumnIndexList;
	Text outPutKeyWriter = new Text();
 
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {// same column in the same
														// group of reducer
		outPutKeyWriter.set(key) ;
		if(UNION.equals(unionType)){
			context.write(outPutKeyWriter, null);
		}else if (EXCEPT.equals(unionType)){
			for (Text fileName : values) {
				 if(fileName.toString().equals(firstTable)==false){
					 return;
				 }
		    	
			 }
			
			context.write(outPutKeyWriter, null);
	 
		}else if (INTERSECT.equals(unionType)){
			fileCountList.clear();
			for (Text fileName : values) {
				if(fileCountList.contains(fileName.toString())==false){
					fileCountList.add(fileName.toString()) ;
				}
			}
			if(fileCountList.size()==inputFiles.size()){
		    	context.write(outPutKeyWriter, null);
			}

		}
	}

	@Override
	public void setup(Context context) {
		Configuration config = context.getConfiguration();
		helper = new MapReduceHelper(config, context.getTaskAttemptID());
		outputNames = Arrays.asList(config.get(UnionKeySet.COLUMN_NAMES).split(
				","));
		outputTypes = Arrays.asList(config.get(UnionKeySet.COLUMN_TYPES).split(
				","));

		firstTable = config.get(UnionKeySet.union_first_table);
		unionType = config.get(UnionKeySet.union_type);

		inputFiles = Arrays.asList(config.get(UnionKeySet.union_input_files)
				.split(","));

		inputColumnIndexList = new ArrayList<List<String>>();
		// file1column1,file1column2:file2column1,file2columns2,
		String[] fileColumns = config.get(UnionKeySet.union_input_column_index)
				.split(":");
		for (int i = 0; i < fileColumns.length; i++) {
			inputColumnIndexList.add(Arrays.asList(fileColumns[i].split(",")));
		}

	}
}
