/**
 * 
 * ClassName LogisticPredictorMapper.java
 *
 * Version information: 1.00
 *
 * Date: Sep 9, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.logistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.AlpineHadoopConstants;
import com.alpine.hadoop.LogisticConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Peter
 * 
 */

public class LogisticPredictorMapper extends
		Mapper<LongWritable, Text, Text, Text> {

	String Splitor = ",";

	List<String> columnsIdString = new ArrayList<String>();
	String good = "yes";// default
	String bad = "no";// default
	double[] beta;
	int iteratorCount = 0;

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
					String predict = good;
					if (computerpi(beta, usingColumn) < 0.5) {
						predict = bad;
					}
					if(predict.indexOf(",")!=-1){
						predict="\""+predict+"\"";
					}
					output.set(helper.generateOutputLine(columnValues)
									+ Splitor + predict + Splitor
									+ computerpi(beta, usingColumn) + Splitor
									+ (1.0 - computerpi(beta, usingColumn)));
//					output
					context.write(output,null);
				} else {
					helper.dirtyAdd(columnValues);
//					output.set(helper.generateOutputLine(columnValues) + Splitor + Splitor + Splitor);
					context.write(value,null);
				}
			}
		}
	}

	public double computerpi(double[] beta, double[] x) {
		double gx, pi;
		int i;
		double tmp = 0;
		i = 0;
		gx = 0;

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

		bad = helper.getConfigString(LogisticConfigureKeySet.bad);
		good = helper.getConfigString(LogisticConfigureKeySet.good);
		String[] columnStrings = helper.getConfigArray(LogisticConfigureKeySet.columns);
		
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
		String[] betaArray = helper.getConfigArray(LogisticConfigureKeySet.beta);
		beta = new double[betaArray.length];
		for (int i = 0; i < betaArray.length; i++) {
			beta[i] = Double.valueOf(betaArray[i]);
		}
	}

	@Override
	protected void cleanup(Context context) {
		helper.cleanUpAlpineHadoopMap(context);
	}

}
