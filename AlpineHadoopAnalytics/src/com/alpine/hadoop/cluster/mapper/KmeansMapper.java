/**
 *
 * ClassName KmeansMapper.java
 *
 * Version information: 1.00
 *
 * Aug 23, 2012
 * 
 * COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
 *
 */

package com.alpine.hadoop.cluster.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.KmeansConfigureKeySet;
import com.alpine.hadoop.cluster.util.distance.Distance;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Jonathan
 * 
 */

public class KmeansMapper extends
		Mapper<LongWritable, Text, LongWritable, DoubleArrayWritable> {
	private Distance dm;

	private List<DoubleWritable[]> cPointList;
	MapReduceHelper helper;
	private List<Integer> columnIds;
	DoubleArrayWritable result = new DoubleArrayWritable();
	HashMap<Long, LongWritable> centroidMap = new HashMap<Long, LongWritable>();
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		List<String[]> lines = helper.getCleanData(value, false);
		if (lines != null) {
			for (String[] line : lines) {
				double min = -1.0;
				long centroid = -1;
				DoubleWritable[] in = getDoubleWritableInput(line);
				if (in != null) {
					try {
						for (int i = 0; i < cPointList.size(); i++) {

							Writable[] point = cPointList.get(i);
							double dist = dm.<Writable> compute(in, point);

							if (min < 0 || (dist < min)) {
								min = dist;
								centroid = i;
							}
						}
						// Compute Distance between vec and each other centroid

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					result.set(in); 
					if(centroidMap.containsKey(centroid) ==false){
						centroidMap.put( centroid ,new LongWritable(centroid));
					} 
 					 
 					
					context.write((LongWritable)centroidMap.get(centroid), result);
				}
			}
		}
		/***
		 * 0 3.2 ,6.4 1 2.9 ,5.5 2 3.0 ,2.9
		 */
	}

	private DoubleWritable[] getDoubleWritableInput(String[] line) {
		try {
			DoubleWritable[] doubles = new DoubleWritable[columnIds.size()];
			for (int i = 0; i < columnIds.size(); i++) {
				doubles[i] = new DoubleWritable(
						Double.parseDouble(line[columnIds.get(i)]));
			}
			return doubles;
		} catch (Exception e) { // =ignore the bad data
			return null;
		}

	}

	protected void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		columnIds = helper.getColumnIds(KmeansConfigureKeySet.COLUMNS);
		helper.setInvolvedColumnIds(columnIds);

		String centorPoints = helper
				.getConfigString(KmeansConfigureKeySet.OUTPUT_CENTER_POINTS);
		cPointList = generateCPointList(centorPoints);
		try {
			String dist = context.getConfiguration().get(
					KmeansConfigureKeySet.DISTANCE_TYPE);
			Class<?> klass = Class.forName(dist);
			this.dm = (Distance) klass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not initialize Distance Measure");
		}
	}

	public static List<DoubleWritable[]> generateCPointList(
			String centorPoints) {

		ArrayList<DoubleWritable[]> cPointList = new ArrayList<DoubleWritable[]>();
		if (centorPoints != null) {
			String[] cpointS = centorPoints.split("\n");
			for (String pointLine : cpointS) {
				String[] strings = pointLine.split("\t");
				String points = strings[1];
				String[] values = points.split(",");// Integer.valueOf(strings[0]),
				cPointList.add(getDoubleArray(values));
			}

		}
		return cPointList;

	}

	private static DoubleWritable[] getDoubleArray(String[] values) {
		DoubleWritable[] doubles = new DoubleWritable[values.length];
		for (int i = 0; i < values.length; i++) {
			doubles[i] = new DoubleWritable(Double.parseDouble(values[i]));
		}
		return doubles;
	}

}
