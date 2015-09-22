/**
 * 
 * ClassName RocMapper.java
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
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class RocMapper extends
		Mapper<LongWritable, Text, LongWritable, DoubleArrayWritable> {

	int ROC_MAX_POINTS = 200;
	int piIndex = -1;
	int dependentColumId = -1;

	String good = "yes";

	double max = 1;
	double min = 0;
	MapReduceHelper helper;

	DoubleArrayWritable outputValue = new DoubleArrayWritable();
	LongWritable groupKey = new LongWritable(1);

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		List<String[]> lines = helper.getCleanData(value, false);
		if (lines != null) {
			for (String[] columnValues : lines) {

				if (columnValues.length > piIndex) {
					double pi = Double.parseDouble(columnValues[piIndex]);// pi

					double diff = (max - min) / ROC_MAX_POINTS;
					DoubleWritable[] sumAndTp = new DoubleWritable[2 * ROC_MAX_POINTS];
					int cursor = 0;
					for (; cursor < ROC_MAX_POINTS; cursor++) {
						if (cursor == (ROC_MAX_POINTS - 1)) {
							sumAndTp[cursor] = new DoubleWritable(1);
						} else if (pi >= max - (1 + cursor) * diff) {
							sumAndTp[cursor] = new DoubleWritable(1);
						} else {
							sumAndTp[cursor] = new DoubleWritable(0);
						}
					}// sum
					for (; cursor < 2 * ROC_MAX_POINTS; cursor++) {
						if (cursor == (2 * ROC_MAX_POINTS - 1)
								&& good.equals(columnValues[dependentColumId])) {
							sumAndTp[cursor] = new DoubleWritable(1);
						} else if (pi >= max - (1 + cursor - ROC_MAX_POINTS)
								* diff
								&& good.equals(columnValues[dependentColumId])) {
							sumAndTp[cursor] = new DoubleWritable(1);
						} else {
							sumAndTp[cursor] = new DoubleWritable(0);
						}
					}// tp
					outputValue.set(sumAndTp);
					context.write(groupKey, outputValue);

				}
			}
		}
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());

		dependentColumId = helper.getConfigInt(RocKeySet.dependent);
		good = helper.getConfigString(RocKeySet.good);
		max = helper.getConfigDouble(RocKeySet.max_roc_probability);
		min = helper.getConfigDouble(RocKeySet.min_roc_probability);
		ROC_MAX_POINTS = helper.getConfigInt(RocKeySet.max_roc_points);
		piIndex = helper.getConfigInt(RocKeySet.piIndex);

		List<Integer> ids = new ArrayList<Integer>();
		ids.add(piIndex);
		helper.setInvolvedColumnIds(ids);
	}
}
