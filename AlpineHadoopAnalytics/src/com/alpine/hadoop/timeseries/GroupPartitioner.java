/**
 * 
 * ClassName GroupPartitioner.java
 *
 * Version information: 1.00
 *
 * Date: Nov 5, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.timeseries;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
/**
* @author Shawn 
* 
*/
public class GroupPartitioner extends Partitioner<LongSort, Text> {
	@Override
	public int getPartition(LongSort key, Text value, int numPartitions) {
		return Math.abs(key.getGroup().get() * 127) % numPartitions;
	}
}
