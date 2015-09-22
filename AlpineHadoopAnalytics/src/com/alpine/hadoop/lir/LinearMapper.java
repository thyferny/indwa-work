/**
 * 
 * ClassName LinearMapper.java
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

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class LinearMapper extends
		Mapper<LongWritable, Text, Text, DoubleArrayWritable> {
	int dependentColumId = -1;
	List<Integer> ids;

	private List<HadoopInteractionItem> interactionItems;

	MapReduceHelper helper;
	Text outputKey=new Text();
	DoubleArrayWritable outputValue = new DoubleArrayWritable();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		helper.initInvolvedColumnIds(ids, interactionItems, dependentColumId);
		List<String[]> columnValuesList = helper.getCleanData(value, false);
		if (columnValuesList != null) {
			for (String[] columnValues : columnValuesList) {

				if (columnValues != null) {
					int newColumnSize = helper.getIdSize(ids, interactionItems)+1;
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

					DoubleWritable[] mapped = new DoubleWritable[(3 * newColumnSize + newColumnSize
							* newColumnSize) / 2 + 1];

					DoubleWritable yi = new DoubleWritable(
							Double.valueOf(columnValues[dependentColumId]));
					int index = 0;
					for (int i = 0; i < newColumnSize; i++) {
						mapped[index] = new DoubleWritable(usingColumn[i]
								* yi.get());
						index++;
					}
					for (int j = 0; j < newColumnSize; j++) {
						for (int k = j; k < newColumnSize; k++) {
							mapped[index] = new DoubleWritable(usingColumn[j]
									* usingColumn[k]);
							index++;
						}
					}
					mapped[mapped.length - 1] = new DoubleWritable(1.0);
					outputValue.set(mapped);
					outputKey.set(String.valueOf(newColumnSize));

					context.write(outputKey, outputValue);
				}
			}
		}
	}
	@Override
	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		interactionItems = helper
				.getInteractionItems(LinearConfigureKeySet.interactionItems);
		ids = helper.getColumnIds(LinearConfigureKeySet.columns);

		dependentColumId = helper.getDependentId(LinearConfigureKeySet.dependent);
	}
	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}

}
