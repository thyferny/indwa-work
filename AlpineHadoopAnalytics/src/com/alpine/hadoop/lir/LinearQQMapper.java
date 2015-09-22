/**
 * 

 * ClassName LinearQQMapper.java
 *
 * Version information: 1.00
 *
 * Date: 2012-8-23
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.lir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.LinearConfigureKeySet;
import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Shawn
 * 
 * 
 */

public class LinearQQMapper extends Mapper<LongWritable, Text, Text, Text> {
	String headerLineValue = "";
	// int index = 0;

	int dependentColumId = -1;
	int totalnumber = 1;
	int counter=0;
	int linenumber = 200;
	List<String> columnsIdString = new ArrayList<String>();
	double[] beta;

	MapReduceHelper utility;
	Text outputKey=new Text();

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		Map<String[], Boolean> columnValuesLegalMap = utility
				.getAllDataWithCleanFlag(value, columnsIdString);
		if (columnValuesLegalMap != null) {
			for (String[] columnValues : columnValuesLegalMap.keySet()) {
				if (columnValuesLegalMap.get(columnValues)) {
					if(counter>=linenumber)
					{
						return;
					}else
					if (counter % ((double) totalnumber / linenumber) < 1) {
						counter=counter+1;
						String[] transformed=utility.projectNewColumnValue(columnValues);
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
						outputKey.set(transformed[dependentColumId]+ ","+ String.valueOf(yesti));
						context.write(outputKey, null);
					}
				} else {
					// illegal filter...do nothing
				}
			}
		}
	}

	@Override
	public void setup(Context context) {
		utility = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());

		String[] columnStrings = utility.getConfigArray(LinearConfigureKeySet.columns);
		
		columnsIdString.add("-1");
		for (String columnName : columnStrings) {
			columnName=columnName.trim();
			if(columnName.indexOf(":")==-1){
				int id=utility.getIndex(columnName);
				columnsIdString.add(String.valueOf(id));
			}
			else{
				int left = utility.getIndex(columnName.split(":")[0]);
				int right = utility.getIndex(columnName.split(":")[1]);
				columnsIdString.add(left+":"+right);
			}
		}
		
		String[] betaArray = utility.getConfigArray(LinearConfigureKeySet.beta);
		beta = new double[betaArray.length];
		for (int i = 0; i < betaArray.length; i++) {
			beta[i] = Double.valueOf(betaArray[i]);
		}
		dependentColumId = utility.getDependentId(LinearConfigureKeySet.dependent);
		totalnumber = utility.getConfigInt(LinearConfigureKeySet.totalnumber);
		linenumber = utility.getConfigInt(LinearConfigureKeySet.linenumber);
	}
}
