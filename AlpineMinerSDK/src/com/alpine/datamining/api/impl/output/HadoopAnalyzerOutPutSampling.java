/**
 * ClassName DataAnlyticOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;

/**
 * @author Nihat Hosgur
 *
 */
public class HadoopAnalyzerOutPutSampling extends AbstractAnalyzerOutPut   {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
 
	private List<HadoopMultiAnalyticFileOutPut> sampleTables;

	public String toString(){
 		return "";
	}

	public void setSampleTables(List<HadoopMultiAnalyticFileOutPut> sampleTables) {
		this.sampleTables = sampleTables;
	}

	public List<HadoopMultiAnalyticFileOutPut> getSampleTables() {
		return sampleTables;
	}
 
}
