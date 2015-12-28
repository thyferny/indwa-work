
package com.alpine.hadoop.roc;


import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.RocKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;



public class RocCombiner extends
		Reducer<LongWritable, DoubleArrayWritable, LongWritable, DoubleArrayWritable> {
	
	int ROC_MAX_POINTS=200;
	MapReduceHelper helper;

	LongWritable groupKeyOne=new LongWritable(1);
	DoubleArrayWritable outputValue = new DoubleArrayWritable();
	
	@Override
	public void reduce(LongWritable key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		DoubleWritable[] resultSum = new DoubleWritable[2*ROC_MAX_POINTS];
		for(int i=0;i<2*ROC_MAX_POINTS;i++){
			resultSum[i]=new DoubleWritable(0);
		}
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			for (int i = 0; i < 2 * ROC_MAX_POINTS; i++) {
				resultSum[i].set(resultSum[i].get() + ((DoubleWritable) in[i]).get());
			}
		}
		outputValue.set(resultSum);
		context.write(groupKeyOne,outputValue);
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		ROC_MAX_POINTS=helper.getConfigInt(RocKeySet.max_roc_points);
	}
}

