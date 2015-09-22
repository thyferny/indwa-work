/**
 * 
 * ClassName StaticsMapper.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.lir;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.util.Matrix;

/**
 * @author Shawn,Peter
 * 
 */

public class StatisticMapper extends Mapper<LongWritable, Text, Text, Text> {

	String headerLineValue = "";

	int dependentColumId = -1;
	List<Integer> ids;
	Matrix beta;
	List<HadoopInteractionItem> interactionItems;
	double yavg = 0;

	MapReduceHelper utility;
	Text outputKey=new Text();
	Text outputValue=new Text();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		utility.initInvolvedColumnIds(ids, interactionItems, dependentColumId);
		List<String[]> columnValuesList = utility.getCleanData(value, false);
		if (columnValuesList != null) {
			for (String[] columnValues : columnValuesList) {

				if (columnValues != null) {

					int newColumnSize = utility.getIdSize(ids, interactionItems)+1;

					double[] usingColumn = new double[newColumnSize];
					int ks = 0;
					usingColumn[ks] = 1;
					for (ks = 1; ks < ids.size() + 1; ks++) {
						usingColumn[ks] = Double.valueOf(columnValues[ids
								.get(ks - 1)]);
					}
					for (HadoopInteractionItem item : interactionItems) {
						if (item.getInteractionType().indexOf("*") != -1) {
							Integer left = item.getLeftId();
							Integer right = item.getRightId();
							usingColumn[ks] = item
									.getInteractionResult(columnValues);
							if (!ids.contains(left)) {
								usingColumn[ks + 1] = Double
										.valueOf(columnValues[left]);
								ks++;
							}
							if (!ids.contains(right)) {
								usingColumn[ks + 1] = Double
										.valueOf(columnValues[right]);
								ks++;
							}
						} else if (item.getInteractionType().indexOf(":") != -1) {
							usingColumn[ks] = item
									.getInteractionResult(columnValues);
						}
						ks++;
					}

					double yesti = 0;
					for (int i = 0; i < newColumnSize; i++) {
						yesti += beta.get(i, 0) * usingColumn[i];
					}
					double y = Double.valueOf(columnValues[dependentColumId]);
					double ryesti = (yesti - y) * (yesti - y);
					double ryavg = (y - yavg) * (y - yavg);
					outputKey.set(String.valueOf(newColumnSize));
					outputValue.set(ryesti+ "," + ryavg + ",1");
					context.write(outputKey, outputValue);
				}
			}
		}

	}

	public void setup(Context context) {
		utility = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		interactionItems = utility
				.getInteractionItems(LinearConfigureKeySet.interactionItems);
		ids = utility.getColumnIds(LinearConfigureKeySet.columns);

		dependentColumId = utility.getDependentId(LinearConfigureKeySet.dependent);
		String[] betaArray = utility.getConfigArray(LinearConfigureKeySet.beta);
		beta = new Matrix(betaArray.length, 1);
		for (int i = 0; i < betaArray.length; i++) {
			beta.set(i, 0, Double.valueOf(betaArray[i]));
		}
		yavg = utility.getConfigDouble(LinearConfigureKeySet.dependent_avg);
	}
}
