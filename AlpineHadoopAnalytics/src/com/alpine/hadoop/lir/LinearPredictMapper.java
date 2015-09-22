/**
 * 
 * ClassName PredictMapper.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.lir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Shawn,Peter
 * 
 */

public class LinearPredictMapper extends Mapper<LongWritable, Text, Text, Text> {

	String Splitor = ",";

	ArrayList<String> columnsIdString = new ArrayList<String>();
	double[] beta;

	MapReduceHelper helper;
	Text output=new Text();

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		Map<String[], Boolean> columnValuesLegalMap = helper
				.getAllDataWithCleanFlag(value, columnsIdString);
		if (columnValuesLegalMap != null) {
			for (String[] columnValues : columnValuesLegalMap.keySet()) {
				if (columnValuesLegalMap.get(columnValues)) {
					String[] transformed=helper.projectNewColumnValue(columnValues);
					int newColumnSize = columnsIdString.size();
					double[] usingColumn = new double[newColumnSize];
					for (int i = 0; i < columnsIdString.size(); i++) {
						String column = columnsIdString.get(i);
						if ("-1".equals(column))
							usingColumn[i] = 1;
						else if (column.indexOf(":") == -1) {
							usingColumn[i] = Double
									.parseDouble(transformed[Integer
											.valueOf(column)]);
						} else {
							String left = column.split(":")[0];
							String right = column.split(":")[1];
							double leftValue = Double
									.parseDouble(transformed[Integer
											.valueOf(left)]);
							double rightValue = Double
									.parseDouble(transformed[Integer
											.valueOf(right)]);
							usingColumn[i] = leftValue * rightValue;
						}
					}
					double yesti = 0;
					for (int i = 0; i < newColumnSize; i++) {
						yesti += beta[i] * usingColumn[i];
					}
					output.set(helper.generateOutputLine(columnValues)
							+ Splitor + String.valueOf(yesti));
					context.write(output, null);
				} else {
//					output.set(helper.generateOutputLine(columnValues)
//							+ Splitor);
					helper.dirtyAdd(columnValues);
					context.write(value, null);
				}
			}
		}
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());

		String[] columnStrings = helper.getConfigArray(LinearConfigureKeySet.columns);
		
		columnsIdString.add("-1");
		for (String columnName : columnStrings) {
			columnName=columnName.trim();
			if(columnName.indexOf(":")==-1){
				int id=helper.getIndex(columnName);
				columnsIdString.add(String.valueOf(id));
			}
			else{
				int left = helper.getIndex(columnName.split(":")[0]);
				int right = helper.getIndex(columnName.split(":")[1]);
				columnsIdString.add(left+":"+right);
			}
		}
		
		String[] betaArray = helper.getConfigArray(LinearConfigureKeySet.beta);
		beta = new double[betaArray.length];
		beta[0]=Double.valueOf(betaArray[betaArray.length-1]);
		for (int i = 1; i < betaArray.length; i++) {
			beta[i] = Double.valueOf(betaArray[i-1]);
		}
	}

	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}
}
