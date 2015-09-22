/**
 * 
 * ClassName FindMinMaxMapper.java
 *
 * Version information: 1.00
 *
 * Date: Sep 9, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.roc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.RocKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Peter
 * 
 */

public class FindMinMaxMapper extends
		Mapper<LongWritable, Text, LongWritable, DoubleWritable> {

	int piIndex = -1;
	MapReduceHelper helper;
	LongWritable groupKeyOne = new LongWritable(1);
	DoubleWritable outputValue = new DoubleWritable();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		List<String[]> lines = helper.getCleanData(value, false);
		if (lines != null) {
			for (String[] columnValues : lines) {

				if (columnValues.length > piIndex) {
					outputValue.set(Double.parseDouble(columnValues[piIndex]));
					context.write(groupKeyOne, outputValue);
				}
			}
		}
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		piIndex = helper.getConfigInt(RocKeySet.piIndex);
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(piIndex);
		helper.setInvolvedColumnIds(ids);
	}
}
