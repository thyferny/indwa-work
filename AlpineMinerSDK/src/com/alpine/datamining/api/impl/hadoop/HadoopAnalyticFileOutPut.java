/**
 * ClassName HadoopAnalyticFileOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;


import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.utility.hadoop.HadoopConnection;
/**
 * @author Eason
 * 
 */

public class HadoopAnalyticFileOutPut extends AbstractAnalyzerOutPut{
	private static final long serialVersionUID = 1L;
	private String resultDir;
	private HadoopConnection hadoopConnetion;

	public HadoopAnalyticFileOutPut() {
		super();
	}
	
	public HadoopAnalyticFileOutPut(String resultDir,HadoopConnection hadoopConnetion) {
		super();
		this.resultDir = resultDir;
	}

	public String getResultDir() {
		return resultDir;
	}

	public void setResultDir(String resultDir) {
		this.resultDir = resultDir;
	}

	public HadoopConnection getHadoopConnetion() {
		return hadoopConnetion;
	}

	public void setHadoopConnetion(HadoopConnection hadoopConnetion) {
		this.hadoopConnetion = hadoopConnetion;
	}
	
}
