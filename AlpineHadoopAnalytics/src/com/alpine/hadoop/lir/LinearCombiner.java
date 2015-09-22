/**
 * 
 * ClassName LinearReducer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-9-6
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.lir;


import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class LinearCombiner extends
		Reducer<Text, DoubleArrayWritable, Text, DoubleArrayWritable> {

	DoubleArrayWritable out=new DoubleArrayWritable();
	public void reduce(Text key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		double count = 0;
		double[] resultSum = null;
		
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			if (count == 0) {
				resultSum = new double[in.length];
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = ((DoubleWritable) in[i]).get();
				}
			} else {
				for (int i = 0; i < in.length; i++) {
					resultSum[i] += ((DoubleWritable) in[i]).get();
				}
			}
			count=1;
		}
		
		DoubleWritable[] combined = new DoubleWritable[resultSum.length];
		for(int i=0;i<resultSum.length;i++){
			combined[i]=new DoubleWritable(resultSum[i]);
		}
		out.set(combined);
		context.write(key, out);
	}
}

