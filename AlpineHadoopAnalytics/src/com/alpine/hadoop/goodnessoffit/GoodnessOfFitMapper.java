/**
 * 
 * ClassName GoodnessOfFitMapper.java
 *
 * Version information: 1.00
 *
 * Date: Sep 9, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.goodnessoffit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.GoodnessOfFitKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class GoodnessOfFitMapper extends
		Mapper<LongWritable, Text, LongWritable, DoubleArrayWritable> {

	// int index = 0;
	ArrayList<Integer> cIndex = new ArrayList<Integer>();
	int dependentColumId = -1;
	ArrayList<String> dependValues = new ArrayList<String>();

	MapReduceHelper helper;
	LongWritable groupKey = new LongWritable();
	DoubleArrayWritable outputValue = new DoubleArrayWritable();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		List<String[]> lines = helper.getCleanData(value, false);
		if (lines != null) {
			for (String[] columnValues : lines) {

				if (columnValues.length > max(cIndex)) {
					DoubleWritable[] mappedArray = new DoubleWritable[3 * dependValues
							.size()];

					for (int cursor = 0; cursor < dependValues.size(); cursor++) {
						if (columnValues[dependentColumId].equals(dependValues.get(cursor))) {
							mappedArray[cursor * 3] = new DoubleWritable(1);//
						} else {
							mappedArray[cursor * 3] = new DoubleWritable(0);//
						}
						if (Double.parseDouble(columnValues[cIndex.get(cursor)]) > 0.5) {
							mappedArray[cursor * 3 + 1] = new DoubleWritable(1);//
						} else {
							mappedArray[cursor * 3 + 1] = new DoubleWritable(0);//
						}
						if (columnValues[dependentColumId].equals(dependValues.get(cursor))
								&& Double.parseDouble(columnValues[cIndex.get(cursor)]) > 0.5) {
							mappedArray[cursor * 3 + 2] = new DoubleWritable(1);//
						} else {
							mappedArray[cursor * 3 + 2] = new DoubleWritable(0);//
						}
					}
					groupKey.set(mappedArray.length);
					outputValue.set(mappedArray);
					context.write(groupKey, outputValue);
				}
			}
		}
	}

	int max(List<Integer> Index) {
		int max = 0;
		for (Integer i : Index) {
			if (i >= max)
				max = i;
		}
		return max;
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		dependentColumId = helper.getConfigInt(GoodnessOfFitKeySet.dependent);
		String[] cString = helper.getConfigArray(GoodnessOfFitKeySet.cIndex);
		for (String s : cString) {
			cIndex.add(Integer.parseInt(s));
		}
		String[] dependString = helper
				.getConfigArray(GoodnessOfFitKeySet.dependValues);
		for (String s : dependString) {
			dependValues.add(s);
		}
		helper.setInvolvedColumnIds(cIndex); 
	}
}
