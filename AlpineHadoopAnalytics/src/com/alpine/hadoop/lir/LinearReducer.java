/**
 * 
 * ClassName LinearReducer.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.lir;


import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.Matrix;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class LinearReducer extends
		Reducer<Text, DoubleArrayWritable, Text, Text> {

	public void reduce(Text key, Iterable<DoubleArrayWritable> values,
			Context context) throws IOException, InterruptedException {
		
		int columnSize = Integer.valueOf(key.toString());
		long count = 0;
		double yavg = 0;
		Matrix xl = new Matrix(columnSize, columnSize);
		Matrix xyl = new Matrix(columnSize, 1);
		double[] resultSum = null;
	 
		for (DoubleArrayWritable value : values) {
			Writable[] in = value.get();
			if (count == 0) {
				resultSum = new double[in.length];
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = ((DoubleWritable) in[i]).get();
				}
			} else {
				for (int i = 0; i < in.length; i++) {
					resultSum[i] += ((DoubleWritable) in[i]).get();
				}
			}
			count=1;
//			count=count+(long) resultSum[resultSum.length-1];
		}
		yavg = resultSum[0] / resultSum[resultSum.length-1];
		xl = new Matrix(columnSize, columnSize);
		xyl = new Matrix(columnSize, 1);

		for (int i = 0; i < columnSize; i++) {
			xyl.set(i, 0, resultSum[i]);
		}
		double[] triArray = this.getHessian(resultSum, columnSize, (columnSize)
				* (columnSize + 1) / 2);
		int index = 0;
		for (int i = 0; i < columnSize; i++) {
			for (int j = i; j < columnSize; j++) {
				double h = 0.0;
				if (!Double.isNaN(triArray[index])) {
					h = triArray[index];
				}
				xl.set(i, j, h);
				if (i != j) {
					xl.set(j, i, h);
				}
				index++;
			}
		}
		Matrix varianceCovarianceMatrix = null;
		try {
			varianceCovarianceMatrix = xl.SVDInverse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Matrix betaMatrix = varianceCovarianceMatrix.times(xyl);

		Double[] coefficients = new Double[columnSize];
		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = 0.0;
		}
		for (int i = 0; i < betaMatrix.getRowDimension(); i++) {
			if (i == 0) {
				coefficients[betaMatrix.getRowDimension() - 1] = betaMatrix
						.get(i, 0);
			} else {
				coefficients[i - 1] = betaMatrix.get(i, 0);
			}
		}

		context.write(new Text(LinearConfigureKeySet.totalnumber), new Text(resultSum[resultSum.length-1]+""));
		context.write(new Text(LinearConfigureKeySet.beta), new Text(Arrays.toString(betaMatrix.getRowPackedCopy())));
		context.write(new Text(LinearConfigureKeySet.coefficients), new Text(Arrays.toString(coefficients)));
		context.write(new Text(LinearConfigureKeySet.covariance), new Text(Arrays.toString(varianceCovarianceMatrix.getRowPackedCopy())));
		context.write(new Text(LinearConfigureKeySet.dependent_avg), new Text(yavg+""));
	}

	protected double[] getHessian(double[] allData, int start, int length) {
		double[] result = new double[length];

		for (int i = 0; i < length; i++) {
			int j = i + start;
			result[i] = allData[j];
		}
		return result;
	}

}
