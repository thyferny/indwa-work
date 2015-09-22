/**
 * 
 * ClassName GoodnessOfFitReducer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-9-19
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.goodnessoffit;


import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class GoodnessOfFitReducer extends
		Reducer<LongWritable, DoubleArrayWritable, Text, Text> {
	Text outputKey=new Text();
	Text outputValue=new Text();
	@Override
	public void reduce(LongWritable key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		long[] resultSum = new long[(int) key.get()];
		for(int i=0;i<(int) key.get();i++){
			resultSum[i]=0;
		}
		long count=0;
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			for (int i = 0; i < in.length-1; i++) {
				resultSum[i] = (long) (resultSum[i] + ((DoubleWritable) in[i]).get());
			}
			count=(long) (count+((DoubleWritable) in[in.length-1]).get());
		}
		outputKey.set(Arrays.toString(resultSum));
		outputValue.set(String.valueOf(count));
		context.write(outputKey,outputValue);
	}
}

