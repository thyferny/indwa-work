/**
 * ClassName KmeansValueRange.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.operator.hadoop.output;
/**
 * @author Jeff Dong
 *
 */
public class KmeansValueRange {
	private double minValue;
	private double maxValue;
	
	public KmeansValueRange(double minValue, double maxValue) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	public double getMinValue() {
		return minValue;
	}
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	public double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
}
