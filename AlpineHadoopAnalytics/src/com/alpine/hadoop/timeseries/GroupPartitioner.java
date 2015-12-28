
package com.alpine.hadoop.timeseries;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class GroupPartitioner extends Partitioner<LongSort, Text> {
	@Override
	public int getPartition(LongSort key, Text value, int numPartitions) {
		return Math.abs(key.getGroup().get() * 127) % numPartitions;
	}
}
