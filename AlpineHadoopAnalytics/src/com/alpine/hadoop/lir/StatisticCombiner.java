/**
* 
* ClassName StatisticCombiner.java
*
* Version information: 1.00
*
* Date: 2012-9-13
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.lir;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

 

/**
 * @author Peter
 *  
 */

public   class StatisticCombiner extends
Reducer<Text, Text, Text, Text>{
	Text outputValue=new Text();
	@Override
	public void reduce(Text arg0, Iterable<Text> values,Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		double yestiCombine=0;
		double yCombie=0;
		double countCombine=0;
		
		for(Text value:values) {
//			Double.valueOf(value.toString().split(",")[0]);
			double yesti=Double.valueOf(value.toString().split(",")[0]);
			double y=Double.valueOf(value.toString().split(",")[1]);
			double count=Double.valueOf(value.toString().split(",")[2]);
			yestiCombine=yestiCombine+yesti;
			yCombie=yCombie+y;
			countCombine=countCombine+count;
		}
		outputValue.set(yestiCombine+","+yCombie+","+countCombine);
		context.write(arg0,outputValue);
	}
}



