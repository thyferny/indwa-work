/**
 * ClassName BarchartSeries.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

public class BarchartSeries {
	
	String seriesValue;
	float yValues[];// y value, index is xvalue
	public BarchartSeries(String seriesValue, float[] yValues){
		this.seriesValue=seriesValue;
		this.yValues=yValues;
	}
	public BarchartSeries(){
		
	}

	public String getSeriesValue() {
		return seriesValue;
	}

	public void setSeriesValue(String seriesValue) {
		this.seriesValue = seriesValue;
	}
 
	public float[] getYValues() {
		return yValues;
	}

	public void setYValues(float[] yValues) {
		this.yValues = yValues;
	}

}
