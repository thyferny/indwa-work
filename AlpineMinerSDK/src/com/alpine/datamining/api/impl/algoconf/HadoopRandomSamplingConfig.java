/**
 * ClassName HadoopRandomSamplingConfig.java
 *
 * Version information: 1.00
 *
 * Data: Aug 1, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.sampling.AnalysisSampleSizeModel;
import com.alpine.utility.db.Resources;

/**
 * @author Jeff Dong
 *
 */
public class HadoopRandomSamplingConfig extends HadoopDataOperationConfig {
	
	private String sampleCount;
	private String sampleSizeType;
	private AnalysisSampleSizeModel sampleSize;
	public static final String HD_HADOOP_RANDOMSAMPLING_VISUALIZATIONCLASS="com.alpine.datamining.api.impl.visual.DataOperationSampleVisualizationType";

	
	protected static final String ConstSampleCount = "sampleCount";
	protected static final String ConstSampleSizeType = "sampleSizeType";
	protected static final String ConstSampleSize = "sampleSize";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{
		parameters.add(ConstSampleCount);
		parameters.add(ConstSampleSizeType);
		parameters.add(ConstSampleSize);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
	public HadoopRandomSamplingConfig() {
		super();
		setParameterNames(parameters);
	}

	@Override
	public String getStoreResults() {
		return Resources.TrueOpt;
	}
	public String getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(String sampleCount) {
		this.sampleCount = sampleCount;
	}

	public String getSampleSizeType() {
		return sampleSizeType;
	}

	public void setSampleSizeType(String sampleSizeType) {
		this.sampleSizeType = sampleSizeType;
	}

	public AnalysisSampleSizeModel getSampleSize() {
		return sampleSize;
	}
	public void setSampleSize(AnalysisSampleSizeModel sampleSize) {
		this.sampleSize = sampleSize;
	}
	@Override
	public String getVisualizationTypeClass() {
		return HD_HADOOP_RANDOMSAMPLING_VISUALIZATIONCLASS;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HadoopRandomSamplingConfig [sampleCount=");
		builder.append(sampleCount);
		builder.append(", sampleSizeType=");
		builder.append(sampleSizeType);
		builder.append(", sampleSize=");
		builder.append(sampleSize);
		builder.append("]");
		return builder.toString();
	}
	
	
}
