/**
*
* ClassName AggregateNodeSplitsCombiner.java
*
* Version information: 1.00
*
* Sep 6, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.combiner;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.log4j.Logger;

import com.alpine.hadoop.tree.model.SplitMappingWritable;

/**
 * @author Jonathan
 *  
 */

public class AggregateNodeSplitsCombiner extends Reducer<Text, SplitMappingWritable, Text, SplitMappingWritable> { 
	private static Logger itsLogger = Logger
            .getLogger(AggregateNodeSplitsCombiner.class);
	
	public void reduce(Text key, Iterable<SplitMappingWritable> values,
			Context context) throws IOException, InterruptedException {
		SplitMappingWritable aggregateHash = null;
		
		for(SplitMappingWritable hash : values) {
			if(aggregateHash == null) {
//				aggregateHash = hash.clone();
			} else {
//				aggregateHash.aggregate(hash);
			}
		}
		
		context.write(key, aggregateHash);
	}
}
