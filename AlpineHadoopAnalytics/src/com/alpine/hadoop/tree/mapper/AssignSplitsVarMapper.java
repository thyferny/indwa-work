/**
 *
 * ClassName AssignSplits.java
 *
 * Version information: 1.00
 *
 * Aug 31, 2012
 * 
 * COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
 *
 */

package com.alpine.hadoop.tree.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.SplitRecordWritable;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Shawn
 * 
 */

public class AssignSplitsVarMapper extends
		Mapper<LongWritable, Text, SplitRecordWritable, Text> {
	private static Logger itsLogger = Logger
			.getLogger(AssignSplitsVarMapper.class);

	// ArrayList first item is min, second is max
	// private HashMap<Integer, ArrayList<Double>> minMax;

	private HashMap<String, Integer> categoricalValues;
	private Configuration config;
 
	private int[] continuousColumns;


	private List<Integer> colInt;

	private String[] columnIndexs;
	private String[] columnTypes;
	MapReduceHelper helper;

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		helper.setInvolvedColumnIds(colInt);
		List<String[]> lines = helper.getCleanData(value, false);
			if (lines != null) {
				for (String[] vec : lines) {//for line in each lines
					if(vec.length>0){

					// int cIdx = 0;
					for (int pt : this.colInt) {//for element in the line
						if(vec.length<columnTypes.length)
						{
							continue;
						}
						String v = vec[pt].trim();

						// cIdx++;
					 

						if (Arrays.binarySearch(this.continuousColumns, pt) >= 0) {
							SplitRecordWritable tempKey = new SplitRecordWritable(
									pt, new Text(v), true);
							context.write(tempKey, new Text(v));
						} else {
							// categorical column
							String test = v + "\n" + pt;
							if (!categoricalValues.containsKey(test)) {
								categoricalValues.put(test, new Integer(pt));
							}
						}
					}
				}
				}
			}
//		}
	}

	protected void setup(Context context) {
		config = context.getConfiguration();
		
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		// minMax = new HashMap<Integer, ArrayList<Double>>();
		columnIndexs = config.get(DecisionTreeConfigureKeySet.COLUMNS).split(
				",");
		colInt = new ArrayList<Integer>();

		for (int i = 0; i < columnIndexs.length; i++) {
			colInt.add(Integer.parseInt(columnIndexs[i]));
		}

		colInt.add(config.getInt(
				DecisionTreeConfigureKeySet.DEPENDANT_COLUMN, 0));
		Collections.sort(colInt);

//		Arrays.sort(colInt);

		String[] c = {};
		String val = config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS);
		if (val != null && !val.isEmpty()) {
			c = config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS)
					.split(",");
		}

		this.categoricalValues = new HashMap<String, Integer>();
		this.continuousColumns = new int[c.length];
		for (int i = 0; i < c.length; i++) {
			this.continuousColumns[i] = Integer.parseInt(c[i]);
			// if(Arrays.binarySearch(colInt, Integer.parseInt(c[i])) >= 0){
			// minMax.put(new Integer(c[i]), null);
			// }
		}
 
		columnTypes = config
				.get(DecisionTreeConfigureKeySet.COLUMN_TYPES).split(",");

	 
	}

	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
		try {
			for (Entry<String, Integer> cat : categoricalValues.entrySet()) {
				SplitRecordWritable tempKey = new SplitRecordWritable(
						cat.getValue(), new Text(cat.getKey().split("\n")[0]));
				context.write(tempKey, new Text(cat.getKey().split("\n")[0]));
			}

		} catch (Exception e) {
			itsLogger.error("Cannot initialize Splits:", e);
			throw new IllegalArgumentException(
					"Cannot initialize Split mappings:");
		}
	}
}
