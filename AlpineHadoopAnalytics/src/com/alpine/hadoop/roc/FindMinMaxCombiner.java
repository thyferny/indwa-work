/**
 * 
 * ClassName LogisticReducer.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.roc;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author Peter
 * 
 */

public class FindMinMaxCombiner extends
		Reducer<LongWritable, DoubleWritable, LongWritable, DoubleWritable> {
	LongWritable groupKeyOne = new LongWritable(1);

	@Override
	public void reduce(LongWritable key, Iterable<DoubleWritable> values,
			Context context) throws IOException, InterruptedException {
		double maxPi = 0;
		double minPi = 1;
		for (DoubleWritable pi : values) {
			if (minPi > pi.get()) {
				minPi = pi.get();
				context.write(groupKeyOne, pi);
			}
			if (maxPi < pi.get()) {
				maxPi = pi.get();
				context.write(groupKeyOne, pi);
			}
		}
	}
}
