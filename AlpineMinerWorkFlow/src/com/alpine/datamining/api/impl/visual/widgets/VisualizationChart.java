/**
 * ClassName VisualizationTreeChart.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual.widgets;

import org.eclipse.draw2d.FreeformLayeredPane;

public class VisualizationChart {
	protected FreeformLayeredPane treeChart = null;
	protected int showHeight;
	protected int showWidth;
	
	public int getShowHeight() {
		return showHeight;
	}
	public void setShowHeight(int showHeight) {
		this.showHeight = showHeight;
	}
	public int getShowWidth() {
		return showWidth;
	}
	public void setShowWidth(int showWidth) {
		this.showWidth = showWidth;
	}
	
	public FreeformLayeredPane getChart() {
		return treeChart;
	}
}
