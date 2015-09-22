/**
 * ClassName HadoopAnalyticFileOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output.hadoop;


import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
/**
 * @author Eason
 */

public class HadoopAnalyticFileOutPut extends AbstractAnalyzerOutPut{
	private static final long serialVersionUID = 1L;
	private String resultDir;
	private String lines;

	public HadoopAnalyticFileOutPut(String resultDir) {
		super();
		this.resultDir = resultDir;
	}
	public HadoopAnalyticFileOutPut(String resultDir, String lines) {
		super();
		this.resultDir = resultDir;
		this.lines = lines;
	}

	public String getResultDir() {
		return resultDir;
	}

	public void setResultDir(String resultDir) {
		this.resultDir = resultDir;
	}

	public String getLines() {
		return lines;
	}

	public void setLines(String lines) {
		this.lines = lines;
	}
}
