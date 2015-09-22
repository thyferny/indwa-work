/**
 * ImageSizeConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.dataset;

/**
 * @author Jimmy
 *
 */
public class ImageSizeConfig {

	private String outputName;
	private int outputHeight;
	private int outputWidth;
	public String getOutputName() {
		return outputName;
	}
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
	public int getOutputHeight() {
		return outputHeight;
	}
	public void setOutputHeight(int outputHeight) {
		this.outputHeight = outputHeight;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
}
