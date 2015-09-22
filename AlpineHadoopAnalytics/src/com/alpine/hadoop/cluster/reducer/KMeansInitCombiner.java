/**
 * ClassName KMeansInitCombiner.java
 *
 * Version information: 1.00
 *
 * Data: 2012-12-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.reducer;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */
public class KMeansInitCombiner extends
Reducer<Text, DoubleArrayWritable, Text, DoubleArrayWritable> {
	private int k;  
	
	@Override
	protected void reduce(Text key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		int count = 0;
//		int batchCount = 0;
		
		if("linenumbercount".equals(key.toString())){
			Double sum=0.0;
			for(DoubleArrayWritable next : values) {
				sum=sum+((DoubleWritable)(next.get()[0])).get();
			}
			long linecount=sum.longValue();
			DoubleArrayWritable countWritable=new DoubleArrayWritable();
			countWritable.set(new DoubleWritable[]{new DoubleWritable(linecount)});
			context.write(new Text("linenumbercount"), countWritable);
		}else if(key.toString().equals(KmeansConfigureKeySet.KEY_RANDOM_POINT)) {
				//random points calculation
			for(DoubleArrayWritable next : values) {
				count++;
				if(count <= this.k){
					context.write(key, next);
				} 
			}	
		}
		else {
			for(DoubleArrayWritable next : values) {
				context.write(key, next);
			}
		}
	}

	protected void setup(Context context) {
		this.k = new Integer(context.getConfiguration().get(KmeansConfigureKeySet.K));
	}
}
