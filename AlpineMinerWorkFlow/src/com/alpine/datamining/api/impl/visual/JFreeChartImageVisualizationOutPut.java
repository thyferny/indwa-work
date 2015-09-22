/**
 * ClassName JFreeChartImageVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import org.jfree.chart.JFreeChart;


public class JFreeChartImageVisualizationOutPut extends ImageVisualizationOutPut {
	private JFreeChart image;

	public JFreeChartImageVisualizationOutPut(JFreeChart image) {
		this.image = image;
	}

	private JFreeChart getImage() {
		return image;
	}

	@Override
	public Object getVisualizationObject() {
		return getImage();
	}
	
	
}
