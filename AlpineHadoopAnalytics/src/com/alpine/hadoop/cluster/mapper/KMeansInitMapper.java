/**
 * ClassName KMeansInitMapper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-4
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.cluster.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author John
 * 
 */
public class KMeansInitMapper extends
		Mapper<LongWritable, Text, Text, DoubleArrayWritable> {

	private static Logger itsLogger = Logger.getLogger(KMeansInitMapper.class);

	private List<Double> minList = new ArrayList<Double>();
	private List<Double> maxList = new ArrayList<Double>();
	private boolean ifInit = false;
	MapReduceHelper helper;

	private List<Integer> columnIds;
	DoubleArrayWritable outputValue = new DoubleArrayWritable();
	Writable[] doubleArray;

	private Text keyRandomPoint =new Text(KmeansConfigureKeySet.KEY_RANDOM_POINT);

	private int lineCount =0 ; 

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		List<String[]> lines = helper.getCleanData(value, false);
		if (lines != null) {
			for (String[] line : lines) {
				ArrayList<Double> vector = new ArrayList<Double>();

				for (Integer pt : this.columnIds) {
					String point = line[pt].trim();
					vector.add(Double.parseDouble(point));
				}
				// max min --------------------------------------

				if (ifInit == false) {
					for (int i = 0; i < vector.size(); i++) {
						minList.add(Double.MAX_VALUE);
						maxList.add(Double.MIN_VALUE);
					}
					ifInit = true;
				}
				for (int i = 0; i < vector.size(); i++) {
					Double realValue = vector.get(i);
					Double minValue = minList.get(i);
					Double maxValue = maxList.get(i);
					if (realValue < minValue) {
						minList.set(i, realValue);
					}
					if (realValue > maxValue) {
						maxList.set(i, realValue);
					}
				}

				int i = 0;
				for (Double d : vector) {
					doubleArray[i] = new DoubleWritable(d);
					i++;
				}

				outputValue.set(doubleArray);
				context.write(keyRandomPoint ,outputValue);
				lineCount = lineCount + 1 ;

			}

		}
	}

	protected void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		columnIds = helper.getColumnIds(KmeansConfigureKeySet.COLUMNS);
		helper.setInvolvedColumnIds(columnIds);
		doubleArray = new DoubleWritable[columnIds.size()];
	}

	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
		try {
			writeMaxMin(context, minList, maxList);
			outputValue
			.set(new DoubleWritable[] { new DoubleWritable(lineCount) });
			context.write(new Text("linenumbercount"), outputValue);
		} catch (Exception e) {
			itsLogger.error("Cleanup failed.  Could not write output", e);
			throw new RuntimeException(
					"Cleanup failed.  Could not write output");
		}
		
	}

	private void writeMaxMin(Context context, List<Double> minList,
			List<Double> maxList) throws IOException, InterruptedException {
		for (int i = 0; i < minList.size(); i++) {
			Double min = minList.get(i);
			Double max = maxList.get(i);
			DoubleArrayWritable doubleArrayWritable = new DoubleArrayWritable();
			Writable[] doubleArray = new DoubleWritable[2];
			doubleArray[0] = new DoubleWritable(min);
			doubleArray[1] = new DoubleWritable(max);
			doubleArrayWritable.set(doubleArray);
			context.write(
					new Text(KmeansConfigureKeySet.KEY_MAX_MIN
							+ String.valueOf(i)), doubleArrayWritable);
		}
	}
}
