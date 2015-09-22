/**
* 
* ClassName StaticsReducer.java
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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.util.Matrix;

 

/**
 * @author Shawn,Peter
 *  
 */

public   class StatisticReducer extends
Reducer<Text, Text, Text, Text>{

	Matrix beta;
	Double[] coefficients=null;
	Matrix covariance=null; 
	
	MapReduceHelper utility;
	@Override
	public void reduce(Text arg0, Iterable<Text> values,Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		double r2yesti=0;
		double r2y=0;
		int count=0;
		int size=Integer.parseInt(arg0.toString())-1;

		for(Text value:values) {
			double yesti=Double.valueOf(value.toString().split(",")[0]);
			double y=Double.valueOf(value.toString().split(",")[1]);
			r2yesti=r2yesti+yesti;
			r2y=r2y+y;
			count=(int) (count+Double.valueOf(value.toString().split(",")[2]));
		}
		double r2=1-r2yesti/r2y;
		long dof=count-size-1;
		double s=Math.sqrt(r2yesti/(dof));
		
		
		
		double[][] statistics=caculateStatistics(size, coefficients, s, covariance, dof);
		context.write(new Text(LinearConfigureKeySet.r2),new Text(String.valueOf(r2)));
		context.write(new Text(LinearConfigureKeySet.dof),new Text(String.valueOf(dof)));
		context.write(new Text(LinearConfigureKeySet.s),new Text(String.valueOf(s)));
		context.write(new Text(LinearConfigureKeySet.se),new Text(Arrays.toString(statistics[0])));
		context.write(new Text(LinearConfigureKeySet.t),new Text(Arrays.toString(statistics[1])));
		context.write(new Text(LinearConfigureKeySet.p),new Text(Arrays.toString(statistics[2])));
//		context.write(+";"+
//				 String.valueOf(s)+";"+
//				 String.valueOf(dof)+";"+
//				 Arrays.toString(statistics[0])+";"+
//				 Arrays.toString(statistics[1])+";"+
//				 Arrays.toString(statistics[2])+";"),new Text("")
//				);
	}
	
	protected double[][] caculateStatistics(int columnSize,
			Double[] coefficients, double s,
			Matrix varianceCovarianceMatrix, long dof) {
		double[][] statisticsResult=new double[3][];
		double[] se = new double[columnSize + 1];
		double[] t = new double[columnSize + 1];
		double[] p = new double[columnSize + 1];

		for (int i = 0; i < columnSize; i++) {
			if (varianceCovarianceMatrix != null) {
				if (Double.isNaN(varianceCovarianceMatrix.get(i + 1, i + 1))) {
					se[i] = Double.NaN;
				} else {
					se[i] = s
							* Math.sqrt(Math.abs(varianceCovarianceMatrix.get(
									i + 1, i + 1)));
				}
			} else {
				se[i] = Double.NaN;
			}
			t[i] = coefficients[i] / se[i];
			p[i] = studT(t[i], dof);
		}
		if (varianceCovarianceMatrix != null) {
			if (Double.isNaN(varianceCovarianceMatrix.get(0, 0))) {
				se[columnSize] = Double.NaN;
			} else {
				se[columnSize] = s
						* Math.sqrt(Math
								.abs(varianceCovarianceMatrix.get(0, 0)));
			}
		} else {
			se[columnSize] = Double.NaN;
		}
		t[columnSize] = coefficients[columnSize]
				/ se[columnSize];
		p[columnSize] = studT(t[columnSize], dof);
		;
		statisticsResult[0]=se;
		statisticsResult[1]=t;
		statisticsResult[2]=p;
		return statisticsResult;
	}

	double studT(double t, long dof) {
		t = Math.abs(t);
		double PiD2 = Math.PI / 2;
		double w = t / Math.sqrt(dof);
		double th = Math.atan(w);
		if (dof == 1) {
			return 1 - th / PiD2;
		}
		double sth = Math.sin(th);
		double cth = Math.cos(th);
		if ((dof % 2) == 1) {
			return 1 - (th + sth * cth * statCom(cth * cth, 2, dof - 3, -1))
					/ PiD2;
		} else {
			return 1 - sth * statCom(cth * cth, 1, dof - 3, -1);
		}
	}

	double statCom(double q, int i, long j, int b) {
		double zz = 1;
		double z = zz;
		int k = i;
		while (k <= j) {
			zz = zz * q * k / (k - b);
			z = z + zz;
			k = k + 2;
		}
		return z;
	}

	public void setup(Context context) {
		utility = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		String[] coefficientsArray=utility.getConfigArray(LinearConfigureKeySet.coefficients);
		coefficients= new Double[coefficientsArray.length];
		for(int i=0;i<coefficientsArray.length;i++){
			coefficients[i]=Double.valueOf(coefficientsArray[i]);
		}
		String[] covarianceArray=utility.getConfigArray(LinearConfigureKeySet.covariance);
		covariance= new Matrix((int)Math.sqrt(covarianceArray.length),(int)Math.sqrt(covarianceArray.length));
		for(int i=0;i<(int)Math.sqrt(covarianceArray.length);i++){
			for(int j=0;j<(int)Math.sqrt(covarianceArray.length);j++){
				covariance.set(i, j, Double.valueOf(covarianceArray[i*(int)Math.sqrt(covarianceArray.length)+j]));
			}
		}
	}
}


