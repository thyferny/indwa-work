package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.utility.hadoop.HadoopConnection;

public class TimeSeriesHadoopPredictorConfig extends HadoopPredictorConfig{
	private String aheadNumber;
	private HadoopConnection hadoopInfo;
	
	public static final String ConstNAhead = "aheadNumber";

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstNAhead);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
    public TimeSeriesHadoopPredictorConfig() {
		setParameterNames(parameters);
	}

	public String getAheadNumber() {
		return aheadNumber;
	}

	public void setAheadNumber(String aheadNumber) {
		this.aheadNumber = aheadNumber;
	}

	public HadoopConnection getHadoopInfo() {
		return hadoopInfo;
	}

	public void setHadoopInfo(HadoopConnection hadoopInfo) {
		this.hadoopInfo = hadoopInfo;
	}

}
