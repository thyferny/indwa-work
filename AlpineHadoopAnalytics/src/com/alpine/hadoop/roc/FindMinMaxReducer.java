
package com.alpine.hadoop.roc;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.RocKeySet;



public class FindMinMaxReducer extends
		Reducer<LongWritable, DoubleWritable, Text, Text> {
	Text outputKey = new Text();
	Text outputValue = new Text();

	@Override
	public void reduce(LongWritable key, Iterable<DoubleWritable> values,
			Context context) throws IOException, InterruptedException {

		double maxPi = 0;
		double minPi = 1;

		for (DoubleWritable pi : values) {
			if (minPi > pi.get()) {
				minPi = pi.get();
			}
			if (maxPi < pi.get()) {
				maxPi = pi.get();
			}
		}
		outputKey.set(RocKeySet.max_roc_probability);
		outputValue.set(String.valueOf(maxPi));
		context.write(outputKey, outputValue);
		outputKey.set(RocKeySet.min_roc_probability);
		outputValue.set(String.valueOf(minPi));
		context.write(outputKey, outputValue);
	}
}
