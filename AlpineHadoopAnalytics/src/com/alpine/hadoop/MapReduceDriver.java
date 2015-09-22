/**
*
* ClassName MapReduceDriver.java
*
* Version information: 1.00
*
* Sep 4, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop;

import org.apache.hadoop.conf.Configuration;

/**
 * @author Jonathan
 *  
 */

public interface MapReduceDriver {
	public Object analyze(String inputPath, String outputPath, Configuration config) throws Exception;
}

