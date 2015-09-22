/**
 * AnalyzerOutPutBoxAndWhisker.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.output;

/**
 * @author Jimmy
 *
 */
public class BoxAndWhiskerItem {

	private String series;
	private String type;
	private Number max,min,q1,q3,mean,median;
	public String getSeries() {
		return series;
	}
	public void setSeries(String series) {
		this.series = series;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Number getMax() {
		return max;
	}
	public void setMax(Number max) {
		this.max = max;
	}
	public Number getMin() {
		return min;
	}
	public void setMin(Number min) {
		this.min = min;
	}
	public Number getQ1() {
		return q1;
	}
	public void setQ1(Number q1) {
		this.q1 = q1;
	}
	public Number getQ3() {
		return q3;
	}
	public void setQ3(Number q3) {
		this.q3 = q3;
	}
	public Number getMean() {
		return mean;
	}
	public void setMean(Number mean) {
		this.mean = mean;
	}
	public Number getMedian() {
		return median;
	}
	public void setMedian(Number median) {
		this.median = median;
	}
	
	private String seriesName;
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	private String typeName;
	private String variableName;
	
}
