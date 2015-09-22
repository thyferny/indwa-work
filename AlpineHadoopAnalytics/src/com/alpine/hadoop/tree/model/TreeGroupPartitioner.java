/**
 * 

* ClassName TreeGroupPartitioner.java
*
* Version information: 1.00
*
* Date: 2013-1-21
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.tree.model;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @author Shawn
 *
 *  
 */

public class TreeGroupPartitioner extends Partitioner<SplitRecordWritable,Text> {
	@Override
	public int getPartition(SplitRecordWritable key, Text value, int numPartitions) {
		return Math.abs(key.getColumnIndex().get() * 127) % numPartitions;
	}
}
