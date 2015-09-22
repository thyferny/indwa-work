/**
 * 
 * ClassName LogisticMapper.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.logistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
 
import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.LogisticConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;
import com.alpine.hadoop.util.HadoopInteractionItem;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Peter
 * 
 */

public class LogisticMapper extends Mapper<LongWritable, Text, Text, Text> {

	// int rowIndex = 0;
	int dependentColumId = -1;
	int currentIterationNumber = 0;
	DoubleArrayWritable outter = new DoubleArrayWritable();
	String good = "yes";

	List<Integer> ids = new ArrayList<Integer>();

	double[] beta;

	MapReduceHelper helper;
	private List<HadoopInteractionItem> interactionItems;
	
	Text outputKey=new Text();
	Text outputValue=new Text();
	private int newColumnSize;
	private double[] x;
	private DoubleWritable[] rowMatrix;

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		 
		List<String[]> columnValuesList = helper.getCleanData(value, false);
		if (columnValuesList == null) {
			return;
		}
			for (String[] columnValues : columnValuesList){
				if (columnValues == null){
					continue;
				}  
					int ks = 0;
					x[ks] = 1;
					for (ks = 1; ks < ids.size() + 1; ks++) {
						x[ks] = Double.valueOf(columnValues[ids.get(ks - 1)]);
					}
					 handleInterActionColumns(columnValues, ks);

					 
					double y;
					if (good.equals(columnValues[dependentColumId])) {
						y = 1;
					} else {
						y = 0;
					}

					double fitness = 0;// in last bit of mapped[]
					int cursor = 0;
					double pi;
					if (currentIterationNumber == 0) {// xwz
						pi = (y + 0.5) / 2;
						double foo;
						double eta = Math.log(pi / (1 - pi));
						double exp_eta = pi / (1 - pi);
						double mu_eta_dev = 0;
						if (eta > AlpineHadoopConstants.ALPINE_MINER_THRESH
								|| eta < AlpineHadoopConstants.ALPINE_MINER_MTHRESH) {
							mu_eta_dev = AlpineHadoopConstants.DBL_EPSILON;
						} else {
							mu_eta_dev = exp_eta
									/ ((1 + exp_eta) * (1 + exp_eta));
						}

						foo = pi * (1 - pi) * (eta + (y - pi) / mu_eta_dev);
						for (int i = 0; i < newColumnSize; i++) {
							rowMatrix[cursor].set  (x[i] * foo);
							cursor++;
						}
					} else {// derivative
						pi = computerPI(beta, x);
						double foo = y - pi;
						for (int i = 0; i < newColumnSize; i++) {
							rowMatrix[cursor] .set(x[i] * foo);
							cursor++;
						}
					}

					for (int j = 0; j < newColumnSize; j++) {
						for (int k = j; k < newColumnSize; k++) {
							rowMatrix[cursor] .set(-x[j] * x[k]
									* pi * (1 - pi));
							cursor++;
						}
					}// worked
					if (y == 1) {
						fitness = Math.log(pi);
					} else {
						fitness = Math.log(1 - pi);
					}
					rowMatrix[cursor].set(fitness);
					cursor++;
					rowMatrix[cursor].set(y);

					
					outter.set(rowMatrix);
					outputKey.set(String.valueOf(newColumnSize));
					outputValue.set(outter.toString().trim() + ",1");

					context.write(outputKey,outputValue);
			}	 
	 
	}

	private void handleInterActionColumns(String[] columnValues, int ks) {
		for (HadoopInteractionItem item : interactionItems) {
			if (item.getInteractionType().indexOf("*") != -1) {
				Integer left = item.getLeftId();
				Integer right = item.getRightId();
				x[ks] = item.getInteractionResult(columnValues);
				if (!ids.contains(left)) {
					x[ks + 1] = Double.valueOf(columnValues[left]);
					ks++;
				}
				if (!ids.contains(right)) {
					x[ks + 1] = Double.valueOf(columnValues[right]);
					ks++;
				}
			} else if (item.getInteractionType().indexOf(":") != -1) {
				x[ks] = item.getInteractionResult(columnValues);
			}
			ks =ks + 1;
		}
		 
	}

	public double computerPI(double[] beta, double[] x) {
		double gx = 0, pi;
		int i = 0;
		double tmp = 0;

		/* compute gx */
		while (i < beta.length && i < x.length) {
			gx = gx + beta[i] * x[i];
			i = i + 1;
		}
		/* compute pi */
		// pi = 1.0/(1.0 + exp(-1.0*gx));
		if (gx > AlpineHadoopConstants.ALPINE_MINER_THRESH) {
			tmp = 1.0 / AlpineHadoopConstants.DBL_EPSILON;
			// pi = 1.0/(1.0+DBL_EPSILON);
		} else if (gx < AlpineHadoopConstants.ALPINE_MINER_MTHRESH) {
			tmp = AlpineHadoopConstants.DBL_EPSILON;
			// pi = 1.0/(1.0 + 1.0/DBL_EPSILON);
			// pi = DBL_EPSILON;
		} else {
			tmp = Math.exp(gx);
		}
		// pi = 1.0/(1.0 + tmp);
		pi = tmp / (1.0 + tmp);
		return pi;
	}

	@Override
	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
		
		interactionItems = helper.getInteractionItems(LogisticConfigureKeySet.interactionItems);
		ids = helper.getColumnIds(LogisticConfigureKeySet.columns);

		good = helper.getConfigString(LogisticConfigureKeySet.good);
		dependentColumId = helper.getDependentId(LogisticConfigureKeySet.dependent);
		currentIterationNumber = helper.getConfigInt(LogisticConfigureKeySet.iteratorCount);

		helper.initInvolvedColumnIds( ids, interactionItems, dependentColumId);// update
		// all
		// the
		// column
		// index
		
		newColumnSize = helper.getIdSize(ids, interactionItems)+1;
		x = new double[newColumnSize];

		rowMatrix = new DoubleWritable[(3 * newColumnSize + newColumnSize * newColumnSize) / 2 + 2];
		for (int i = 0; i < rowMatrix.length; i++) {
			rowMatrix[i] = new DoubleWritable();
		}
		if(currentIterationNumber>0){
			String betaString = helper.getConfigString(LogisticConfigureKeySet.beta);
			betaString = betaString.substring(1, betaString.length() - 1);
			String[] betas = betaString.split(",");
			beta = new double[betas.length];
			for (int i = 0; i < betas.length; i++) {
				beta[i] = Double.parseDouble(betas[i]);
			}
		}
		else{
			//only need handle the first time
			beta=new double[helper.getIdSize(ids, interactionItems)+1];
			Arrays.fill(beta, 0.0);
		}
	}
	
	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}
}
 