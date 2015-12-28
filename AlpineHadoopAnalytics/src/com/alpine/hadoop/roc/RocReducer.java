
package com.alpine.hadoop.roc;


import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.RocKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;



public class RocReducer extends
		Reducer<LongWritable, DoubleArrayWritable, Text, Text> {
	
	int index = 0;
	int ROC_MAX_POINTS=200;
	
	MapReduceHelper helper;
	
	@Override
	public void reduce(LongWritable key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		double[] resultSum = new double[2*ROC_MAX_POINTS];
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			for (int i = 0; i < in.length; i++) {
				resultSum[i] = resultSum[i] + ((DoubleWritable) in[i]).get();
			}
		}
		context.write(new Text(Arrays.toString(resultSum)),new Text());
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		ROC_MAX_POINTS=helper.getConfigInt(RocKeySet.max_roc_points);
	}
}

