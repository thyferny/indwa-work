/**
 * 
 * ClassName AlphaBetaMapper.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.variableselection;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter, Sara
 * 
 */

public class AlphaBetaMapper extends
		Mapper<LongWritable, Text, Text, DoubleArrayWritable> {
	int dependentColumId = -1;
	List<Integer> ids;
    private static Logger itsLogger = Logger.getLogger(AlphaBetaMapper.class);

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
					int newColumnSize = ids.size()+1;
					double[] usingColumn = new double[newColumnSize];
					int ks = 0;
                    DoubleWritable yi = new DoubleWritable(
                            Double.valueOf(columnValues[dependentColumId]));

                    usingColumn[ks] = yi.get();   //first spot is the dependent variable
					for (ks = 1; ks < ids.size() + 1; ks++) {
						usingColumn[ks] = Double.valueOf(columnValues[ids
								.get(ks - 1)]);
					}


					DoubleWritable[] mapped = new DoubleWritable[(3 * ids.size()) +2];

                    mapped[0] = new DoubleWritable(yi.get());
					int index = 1;
					for (int i = 1; i < newColumnSize; i++) {
                        mapped[index] = new DoubleWritable(usingColumn[i]* yi.get());
                        mapped[index+1] = new DoubleWritable(usingColumn[i]);
                        mapped[index+2] = new DoubleWritable(usingColumn[i]*usingColumn[i]);
						index+=3;
					}
					mapped[mapped.length - 1] = new DoubleWritable(1.0);   //put count in last spot
					outputValue.set(mapped);
					outputKey.set(String.valueOf(newColumnSize));

					context.write(outputKey, outputValue);
				}
			}
		}
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		interactionItems = helper
				.getInteractionItems(VariableSelectionKeySet.interactionItems);
		ids = helper.getColumnIds(VariableSelectionKeySet.columns);

		dependentColumId = helper.getDependentId(VariableSelectionKeySet.dependent);
	}

	//add bad data count
	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}

}
