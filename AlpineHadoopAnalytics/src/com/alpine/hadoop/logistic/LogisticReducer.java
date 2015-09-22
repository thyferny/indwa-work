/**
 * 
 * ClassName LogisticReducer.java
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

import com.alpine.hadoop.LogisticConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.util.Matrix;

/**
 * @author Peter
 * 
 */

public class LogisticReducer extends
		Reducer<Text, Text, Text, Text> {
	
	int iteratorCount=0;
	double[] beta;
	private MapReduceHelper helper;
	@Override
	public void reduce(Text key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {
		
		int columnSize = Integer.valueOf(key.toString());
		int rowSize=(3 * columnSize + columnSize
				* columnSize) / 2 + 2;
		int count = 0;
		Matrix hessian = new Matrix(columnSize, columnSize);
		Matrix derivative = new Matrix(columnSize, 1);
		double[] resultSum = null;
		double dataSize=0;
		
		if(iteratorCount==0){
			beta=new double[columnSize];
			for(int i=0;i<beta.length;i++){
				beta[i]=0;
			}
		}
		
		for (Text valueText : values) {
			String[] rowMatrix=valueText.toString().split(",");
			
			double[] in = new double[rowSize];
			for(int i=0;i<in.length;i++){
				in[i]=Double.parseDouble(rowMatrix[i]);
			}
			if (count == 0) {
				resultSum = new double[in.length];
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = in[i];
				}
				dataSize=Double.parseDouble(rowMatrix[rowSize]);
			} else {
				dataSize=dataSize+Double.parseDouble(rowMatrix[rowSize]);
				for (int i = 0; i < in.length; i++) {
					resultSum[i] = resultSum[i] + in[i];
				 
				}
			}
			count=1;
		}
		hessian = new Matrix(columnSize, columnSize);
		derivative = new Matrix(columnSize, 1);

		for (int i = 0; i < columnSize; i++) {
			derivative.set(i, 0, resultSum[i]);
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
				hessian.set(i, j, h);
				if (i != j) {
					hessian.set(j, i, h);
				}
				index++;
			}
		}
		
		Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new InterruptedException(e.getLocalizedMessage());
    	}
    	
    	double[] delta = new double[beta.length];
		for (int i = 0; i < beta.length; i++)
		{
			delta[i] = 0;
			for(int j = 0; j < beta.length; j++)
			{
				delta[i] += varianceCovarianceMatrix.get(i, j) * derivative.get(j,0);
			}
			if (iteratorCount==0)
			{
				beta[i] = -delta[i];
			}
			else
			{
				beta[i] = beta[i] - delta[i];
			}
		}
		context.write(new Text(LogisticConfigureKeySet.hessian), new Text(Arrays.toString(hessian.getColumnPackedCopy())));
		context.write(new Text(LogisticConfigureKeySet.derivative), new Text(Arrays.toString(derivative.getColumnPackedCopy())));
		context.write(new Text(LogisticConfigureKeySet.beta), new Text(Arrays.toString(beta)));
		context.write(new Text(LogisticConfigureKeySet.fitness), new Text(resultSum[resultSum.length-2]+"")); 
		context.write(new Text(LogisticConfigureKeySet.positive), new Text(resultSum[resultSum.length-1]+""));
		context.write(new Text(LogisticConfigureKeySet.totalnumber), new Text(dataSize+""));
		context.write(new Text(LogisticConfigureKeySet.variance), new Text(Arrays.toString(getVariance(beta.length,hessian))));
	}

	protected double[] getHessian(double[] allData, int start, int length) {
		double[] result = new double[length];

		for (int i = 0; i < length; i++) {
			int j = i + start;
			result[i] = allData[j];
		}
		return result;
	}

	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		iteratorCount=helper.getConfigInt(LogisticConfigureKeySet.iteratorCount);
		if(iteratorCount>0){
			String betaString = helper.getConfigString(LogisticConfigureKeySet.beta);
			betaString = betaString.substring(1, betaString.length() - 1);
			String[] betas = betaString.split(",");
			beta = new double[betas.length];
			for (int i = 0; i < betas.length; i++) {
				beta[i] = Double.parseDouble(betas[i]);
			}
		}
		else{
			
		}
	}
	public double[] getVariance(int betaLength, Matrix hessian) {
		double[] variance = new double[betaLength];
    	Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
    		e.printStackTrace();
    		for (int j = 0; j < betaLength; j++) {
    			variance[j] = Double.NaN;
    		}
    		return variance;
    	}
    	for (int j = 0; j < betaLength; j++) {
   			variance[j] = Math.abs(varianceCovarianceMatrix.get(j, j));
    	}
    	return variance;
	}
	public int arrayContain(Object[] array,Object o){
		for(int i=0;i<array.length;i++){
			if(o.equals(array[i])) return i;
		}
		return -1;
	}
}
