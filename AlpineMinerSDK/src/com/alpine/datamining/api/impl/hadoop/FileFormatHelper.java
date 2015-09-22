/**
 * ClassName FileFormatHelper.java
 *
 * Version information: 1.00
 *
 * Date: Oct 30, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.alpine.datamining.api.AnalysisException;

/**
 * @author John Zhao
 *
 */
public interface FileFormatHelper {
	public void initHadoopConfig(Configuration hadoopConf) throws AnalysisException;
	
	public void setInputFormatClass(Job job  ) ;
	 
}
