/**
 * ClassName VisualizationModelText.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;


public class VisualizationModelLine extends AbstractVisualizationModelChart {
	
	private List<VisualLine> lines=null;

	
	
 
	public List<VisualLine> getLines() {
		return lines;
	}
 
	public void setLines(List<VisualLine> lines) {
		this.lines = lines;
	}
 
	public VisualizationModelLine(String title,List<VisualLine> lines) {
		super(TYPE_LINE_CHART,title);
		 
		this.lines=lines;
		super.sethGrid(true) ;
		super.setvGrid(true) ;
	 
	}
 
}
