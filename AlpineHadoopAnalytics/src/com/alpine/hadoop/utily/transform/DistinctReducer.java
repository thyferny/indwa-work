/**
 * 
 * ClassName DistinctReducer.java
 *
 * Version information: 1.00
 *
 * Date: Aug 9, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.utily.transform;


import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.util.MapReduceHelper;

/**
 * @author Peter
 * 
 */

public class DistinctReducer extends
		Reducer<ColumnDistinct, Text, Text, Text> {
	
	MapReduceHelper helper;
	
	Text column=new Text();
	Text value=new Text();
	@Override
	public void reduce(ColumnDistinct key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {//same column in the same group of reducer
		column.set(helper.getColumnNames()[key.columnId.get()]);
		value.set(key.value.toString());
		context.write(column, value);
	}
	public void setup(Context context) {
		helper = new MapReduceHelper(context.getConfiguration(),
				context.getTaskAttemptID());
	}
}
