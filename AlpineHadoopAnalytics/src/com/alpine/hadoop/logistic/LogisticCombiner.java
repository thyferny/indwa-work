/**
 * 
 * ClassName LogisticCombiner.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.logistic;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author Peter
 * 
 */

public class LogisticCombiner extends Reducer<Text, Text, Text, Text> {

	Text outputValue=new Text();
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		int newColumnSize = Integer.parseInt(key.toString());
		int rowSize = (3 * newColumnSize + newColumnSize * newColumnSize) / 2 + 2;
		int flag = 0;

		double[] resultSum = null;

		for (Text valueText : values) {
			String[] rowMatrix = valueText.toString().split(",");

			double[] in = new double[rowSize + 1];
			for (int i = 0; i < rowSize + 1; i++) {
				in[i] = Double.parseDouble(rowMatrix[i].trim());
			}
			if (flag == 0) {
				resultSum = new double[in.length];
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = in[i];
				}
			} else {
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = resultSum[i] + in[i];
				}
			}
			flag = 1;
		}
		String rowMatrix = Arrays.toString(resultSum);
		rowMatrix = rowMatrix.substring(1, rowMatrix.length() - 1);
		outputValue.set(rowMatrix);
		context.write(key, outputValue);
	}
}
