/**
 *
idc * ClassName KmeansOutPutMapper.java
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

/**
 * @author John
 * 
 */

public class KmeansOutPutMapper extends Mapper<LongWritable, Text, Text, Text> {
	// private static final Text TEXT_CONST = new Text();
	private Distance dm;
	private HashMap<Integer, DoubleWritable[]> cPointList;

	private int idColumnIndex = -1;// -1 means no id column
	private String delimiter = ","; // should always use "," for output
	private int k;
	private String[] resultString;
	private DoubleWritable[] doubleResult;
	Text result = new Text( );
	MapReduceHelper helper;
	private List<Integer> columnIds;

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		List<String[]> lines = helper.getCleanData(value, false);

		if (lines != null) {
			for (String[] line : lines) {
				double min = -1.0;
				long centroid = -1;
				String[] in = getStringInput(line);
				DoubleWritable[] doublein = getDoubleWritableInput(line);
				if (in != null) {
					try {
						for (int i = 0; i < this.k; i++) {
							Writable[] point = cPointList.get( i );
							if (point == null) {
								continue;
							}

							double dist = dm
									.<Writable> compute(doublein, point);

							if (min < 0 || (dist < min)) {
								min = dist;
								centroid = i;
							}

						}
						// Compute Distance between vec and each other
						// centroid

					} catch (Exception e) {
						System.out
								.println("Distance could not be computed, make sure input vectors are the same length as centroids"
										+ e);
						// throw new RuntimeException(
						// "Distance could not be computed, make sure input vectors are the same length as centroids",
						// e);
						continue;
					}

					if (idColumnIndex >= 0) {
						result.set(toRersultString(in)
								+ line[idColumnIndex] + delimiter + centroid);
						context.write(result, null);
					} else {
						result.set(toRersultString(in) + centroid);
						context.write(result, null);

					}
				}

			}
		}
	}

	private String toRersultString(String[] in) {
		StringBuilder sb = new StringBuilder();
		for (String value : in) {
			sb.append(value).append(delimiter);
		}
		return sb.toString();
	}

	private String[] getStringInput(String[] line) {
		try {
			for (int i = 0; i < columnIds.size(); i++) {
				resultString[i] = line[columnIds.get(i)];
			}
			return resultString;
		} catch (Exception e) {
			return null;
		}

	}

	private DoubleWritable[] getDoubleWritableInput(String[] line) {
		try {
			for (int i = 0; i < columnIds.size(); i++) {
				doubleResult[i] = new DoubleWritable(
						Double.parseDouble(line[columnIds.get(i)]));
			}
			return doubleResult;
		} catch (Exception e) {
			return null;
		}

	}

	protected void setup(Context context) {

		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		columnIds = helper.getColumnIds(KmeansConfigureKeySet.COLUMNS);
		helper.setInvolvedColumnIds(columnIds);
		resultString = new String[columnIds.size()];
		doubleResult = new DoubleWritable[columnIds.size()];

		this.k = helper.getConfigInt(KmeansConfigureKeySet.K);

		String centorPoints = helper
				.getConfigString(KmeansConfigureKeySet.OUTPUT_CENTER_POINTS);
		cPointList = new HashMap<Integer, DoubleWritable[]>();
		if (centorPoints != null) {
			String[] cpointS = centorPoints.split("\n");
			for (String pointLine : cpointS) {
				String[] strings = pointLine.split("\t");
				String points = strings[1];
				String[] values = points.split(",");
				cPointList.put(Integer.valueOf(strings[0]),
						getDoubleArray(values));
			}

		}

		idColumnIndex = helper.getDependentId(KmeansConfigureKeySet.ID_FIELD);
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

	private DoubleWritable[] getDoubleArray(String[] values) {
		DoubleWritable[] doubles = new DoubleWritable[values.length];
		for (int i = 0; i < values.length; i++) {
			doubles[i] = new DoubleWritable(Double.parseDouble(values[i]));
		}
		return doubles;
	}

}
