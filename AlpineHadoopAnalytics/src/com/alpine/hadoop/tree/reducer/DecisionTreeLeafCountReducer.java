/**
*
* ClassName DecisionTreeLeafCountReducer.java
*
* Version information: 1.00
*
* Oct 31, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.reducer;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.log4j.Logger;

import com.alpine.hadoop.tree.model.IntegerArrayWritable;
import com.alpine.hadoop.tree.model.SplitMappingWritable;

/**
 * @author Jonathan
 *  
 */

public class DecisionTreeLeafCountReducer extends Reducer<Text, SplitMappingWritable, IntWritable, Text> {
	
	private static Logger itsLogger = Logger
            .getLogger(DecisionTreeLeafCountReducer.class);
	
	public void reduce(Text key, Iterable<SplitMappingWritable> values,
			Context context) throws IOException, InterruptedException {
//	SplitMappingWritable aggregateHash = null;
//	String[] splitKey = key.toString().split("\t");
//	IntWritable keyN = new IntWritable(Integer.parseInt(splitKey[0]));
//
//	// aggregate the count hashes for this node
//	for(SplitMappingWritable hashV : values) {
//		if(aggregateHash == null) {
//			aggregateHash = hashV.clone();
//		} else {
//			aggregateHash.aggregate(hashV);
//		}
//	}
//	
//	MapWritable[] features = (MapWritable[]) aggregateHash.toArray();
//	IntegerArrayWritable totalDistribution = ((IntegerArrayWritable) features[0].get(new Text("total")));
//	
//	context.write(keyN, new Text(String.valueOf(-2) + ",LEAF\t" + totalDistribution));//TODO  delete this class 
	}
}

