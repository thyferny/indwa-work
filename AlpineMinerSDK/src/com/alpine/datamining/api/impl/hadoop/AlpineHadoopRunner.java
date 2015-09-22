/**
 * 

* ClassName AlpineHadoopRunner.java
*
* Version information: 1.00
*
* Date: 2012-8-20
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop;

import com.alpine.datamining.api.AnalyticSource;
/**
 * @author Shawn
 *
 *  
 */

public interface AlpineHadoopRunner {
	public static final String ALPINE_MAPREDUCE_JAR = "AlpineHadoopAnalytics.jar";

	public String reducedFile="/part-r-00000";
	public String mappedFile="/part-m-00000";
	public final int columnLimit=100;
	public final int totalLimit=1000;
	
	public final String oneDistinctErr="Can not perform calculation since each selected column only contains one distinct value.";
	public final String logisticDependentErr="Can not perform calculation since logistic regression needs 2 distinct values in depenndent column.";
	public final String tooManyTotalDistinctErr="All chararray columns contain more than "+totalLimit+" distinct value,may cause memory leaking on server.";
	public final String tooManyEachDistinctErr=" contains more than "+columnLimit+" distinct value,may cause memory leaking on server.";
	public final String localJobFailedErr="(JobId:Local Mode) Failed .More details in Alpine.log .";
	public final String trainFailedErr = " could not be created due to no valid input databecause large number of rows had to be discarded due to null values.";
	public final String resultEmptyErr=" is empty file.";
	
	public long getBadCounter();
	public Object runAlgorithm(AnalyticSource source) throws Exception;
	public boolean isLocalMode() ;
	public void stop();
	 
	public boolean isStop();

	
}
