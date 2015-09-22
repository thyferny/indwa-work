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
 * @author John Zhao
 * 
 */
public class AnalyzerOutPutSampling extends AbstractAnalyzerOutPut {
	// Object like doublelist and double data

	/**
	 * 
	 */
	private static final long serialVersionUID = -1155669583658641360L;

	private List<AnalyzerOutPutTableObject> sampleTables;
	private boolean isHadoopSampling = false;

	public String toString() {
		return "";
	}

	public void setSampleTables(List<AnalyzerOutPutTableObject> sampleTables) {
		this.sampleTables = sampleTables;
	}

	public List<AnalyzerOutPutTableObject> getSampleTables() {
		return sampleTables;
	}

	public boolean isHadoopSampling() {
		return isHadoopSampling;
	}

	public void setHadoopSampling(boolean isHadoopSampling) {
		this.isHadoopSampling = isHadoopSampling;
	}


}
