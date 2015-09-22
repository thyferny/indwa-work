package com.alpine.hadoop.cluster.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

public class KMeansPostCombiner
		extends
		Reducer<LongWritable, DoubleArrayWritable, LongWritable, DoubleArrayWritable> {

	@Override
	protected void reduce(LongWritable key,
			Iterable<DoubleArrayWritable> values, Context context)
			throws IOException, InterruptedException {
		if (key.get() == -1) {
			handleTottalDistance(key, values, context);
		} else {
			handleSplitAndScatter(key, values, context);
		}
	}

	private void handleTottalDistance(LongWritable key,
			Iterable<DoubleArrayWritable> values, Context context)
			throws IOException, InterruptedException {
		double sum = 0.0;
		double lineNumber = 0;
		for (DoubleArrayWritable vec : values) {
			if(vec.get().length<2){
				System.out.print("xxx");
				System.out.print(vec.get());
			}
			try{
				sum = sum + ((DoubleWritable) vec.get()[0]).get();
				lineNumber = lineNumber + ((DoubleWritable) vec.get()[1]).get();
			}catch(Exception e){
				
			}
		}
		DoubleArrayWritable sumResult = new DoubleArrayWritable();
		DoubleWritable[] sumArray = new DoubleWritable[] {
				new DoubleWritable(sum), new DoubleWritable(lineNumber) };
		sumResult.set(sumArray);
		context.write(key, sumResult);

	}

	private void handleSplitAndScatter(LongWritable key,
			Iterable<DoubleArrayWritable> values, Context context)
			throws IOException, InterruptedException {
		Configuration configuration = context.getConfiguration();

		int limitLine = Integer.parseInt(configuration
				.get(KmeansConfigureKeySet.LIMIT_LINE));
		int i=0;
		for (DoubleArrayWritable daw : values) {// for each line
			if(i<limitLine){
				context.write(key, daw);
			}
			i++;
		}
	}
}
