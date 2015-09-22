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

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class GoodnessOfFitCombiner extends
		Reducer<LongWritable, DoubleArrayWritable, LongWritable, DoubleArrayWritable> {
	
	DoubleArrayWritable outputValue = new DoubleArrayWritable();
	@Override
	public void reduce(LongWritable key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		DoubleWritable[] resultSum = new DoubleWritable[(int) key.get()+1];
		for(int i=0;i<(int) key.get()+1;i++){
			resultSum[i]=new DoubleWritable(0);
		}
		long count=0;
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			for (int i = 0; i < in.length; i++) {
				if(((DoubleWritable) in[i]).get()==1){
					resultSum[i].set(resultSum[i].get() + 1);
				}
			}
			count++;
		}
		resultSum[resultSum.length-1].set(count);
		outputValue.set(resultSum);
		context.write(key,outputValue);
	}
}

