/**
*
* ClassName SanitizeDataMapper.java
*
* Version information: 1.00
*
* Aug 20, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.utily.conversion;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Jonathan
 *  
 */

 public class SanitizeDataMapper extends Mapper<LongWritable, Text, LongWritable, DoubleArrayWritable> {
 
	
	public void map(LongWritable key, Text value,Context context)
			throws IOException,InterruptedException {
//		String delimiter = context.getConfiguration().get("sanitize.delim");
//		
//		if(context.getConfiguration().get("sanitize.delim") == null) {
//			// default to CSV
//			delimiter = ",";
//		} 
//		
//		String[] line = value.toString().split(delimiter);
//		DoubleWritable[] doubles = new DoubleWritable[line.length];
//		int i = 0;
//		
//        for(String point : line) {
//        	point = point.trim();
//        	
//            if(point.isEmpty()) {
//            	context.getCounter(KmeansDriver.Kmeans.MISSING).increment(1);
//           
//                if(itsLogger.isDebugEnabled()){
//                    itsLogger.debug("Empty point found! Moving on");
//                }
//                return;
//            }
//
//            try{
//                doubles[i] = DoubleWritable(point);
//            } catch (NumberFormatException e) {
//            	context.getCounter(KmeansDriver.Kmeans.MALFORMED).increment(1);
//       
//                itsLogger.error("Cannot parse string: Must be a float:", e);
//                return;
//            }
//            i++;
//        }
//        DoubleArrayWritable out = new DoubleArrayWritable();
//		out.set(doubles);
//
//		context.write(new LongWritable(key.get() % (context.getNumReduceTasks() * 2)), out);
	}
}

