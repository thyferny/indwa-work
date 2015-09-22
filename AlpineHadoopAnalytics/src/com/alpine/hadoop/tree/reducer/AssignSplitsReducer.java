/**
*
* ClassName AssignSplitsReducer.java
*
* Version information: 1.00
*
* Oct 11, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alpine.hadoop.DecisionTreeConfigureKeySet;
import com.alpine.hadoop.tree.model.SplitRecordWritable;

/**
 * @author Shawn
 *  
 */

public class AssignSplitsReducer extends Reducer<SplitRecordWritable, Text, Text, Text> { 
	private int[] continuousColumns;
	
 

	private String splitor;
	
	
	public void reduce(SplitRecordWritable key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {
		 
		Set<String> categoricalSet = new HashSet<String>();
		String output;
		if(Arrays.binarySearch(this.continuousColumns, key.getColumnIndex().get()) >= 0) {
//			ArrayList<Double> splitValues= new ArrayList<Double>(); 
			
			long size=0;
			Text resultText=new Text("Splitcontinuous_"+key.getColumnIndex());
			// continuous column	
			int first=0;
			String maxValue="";
//			String maxString=0;
//			double minValue=0;
			String tempValue="";
			for(Text value : values) {
			 
				String dataValue=value.toString();
				if(first==0)
				{
					tempValue=dataValue;
					first=1;
					maxValue=dataValue;
//					minValue=dataValue;
					size++;
					context.write(resultText, new Text(tempValue));
					continue;
				}
				if(tempValue.equals(dataValue))
				{
					continue;
				}
				size++;
//				splitValues.add((tempValue+dataValue)/2);
				context.write(resultText, new Text(dataValue));
				
				tempValue=dataValue;
//				minValue=dataValue;
				
				 
			}
		} else {
			int first=0;
			String tempValue="";
			Text resultText=new Text("Splitcategorical_"+key.getColumnIndex());
			for(Text value : values) {
				String dataValue=value.toString();
				if(first==0)
				{
					tempValue=dataValue;
					first=1;
 					context.write(resultText, new Text(tempValue));
					continue;
				}
				if(tempValue.equals(dataValue))
				{
					continue;
				}
  				context.write(resultText, new Text(dataValue));
				tempValue=dataValue;
			}
		}
	}
	
	protected void setup(Context context) {
		Configuration config = context.getConfiguration();
		String[] c = {};
		if(config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS) != null && !config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).isEmpty()) {
			c = config.get(DecisionTreeConfigureKeySet.CONTINUOUS_COLUMNS).split(",");
		}
		
		this.continuousColumns = new int[c.length];
		for(int i = 0; i < c.length; i++) {
			this.continuousColumns[i] = Integer.parseInt(c[i]);
			
		}
		this.splitor=config.get(DecisionTreeConfigureKeySet.DELIMITER_CHAR);
	 
	}
}



