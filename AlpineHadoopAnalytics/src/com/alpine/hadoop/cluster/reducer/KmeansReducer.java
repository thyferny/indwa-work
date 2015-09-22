/**
*
* ClassName KmeansReducer.java
*
* Version information: 1.00
*
* Aug 23, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.cluster.reducer;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Jonathan
 *  
 */

public class KmeansReducer extends Reducer<LongWritable, DoubleArrayWritable, LongWritable, DoubleArrayWritable> { 
 
	int width = -1;
	DoubleArrayWritable out = new DoubleArrayWritable();
	DoubleWritable[] doubles;
 
	public void reduce(LongWritable key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		double total = 0.0;
		Double[] sum = null;
	
		for(DoubleArrayWritable vec : values) {
			total++;
			
			Writable[] vector = (Writable[]) vec.get();
			
			if(this.width == -1) {
				this.width = vector.length;
				doubles = new DoubleWritable[this.width];
			}
			
			if(sum == null) {
				sum = new Double[this.width];
				Arrays.fill(sum, new Double(0.0));
			}
			
			for(int i = 0; i < vector.length; i++){
				sum[i] += ((DoubleWritable) vector[i]).get();
	        }
	    }
		
		if(total == 0.0) {
			throw new IllegalArgumentException("Input Vector set empty for cluster on iteration " + 
					context.getConfiguration().get(KmeansConfigureKeySet.ITERATION));
		}
		
		for(int m = 0; m < sum.length; m++){
			sum[m] /= total;
		}
		
	//	DoubleWritable[] doubles = new DoubleWritable[sum.length];
		
		for(int d = 0; d < sum.length; d++) {
			doubles[d] = new DoubleWritable(sum[d]);
		}
		
		//DoubleArrayWritable out = new DoubleArrayWritable();
		out.set(doubles);
		context.write(key, out);
	}
}



