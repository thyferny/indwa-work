/**
 * ClassName AbstractChartVisualizationModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

import com.alpine.miner.workflow.output.AbstractVisualizationModel;

public abstract class AbstractVisualizationModelChart extends AbstractVisualizationModel implements VisualizationModelChart{
	//if show the point for the line
	private boolean markers=false;
	
	private String xMajorTickStep= null; 
	private String xMinorTickStep= null; 
	// if there are  too many x labels, can rotate it to make a good look
	//default 0 means normal showing...
	int xLableRotation=0;

	int yLableRotation;
	
	public int getxLableRotation() {
		return xLableRotation;
	}

	public void setxLableRotation(int xLableRotation) {
		this.xLableRotation = xLableRotation;
	}
	public int getyLableRotation() {
		return yLableRotation;
	}

	public void setyLableRotation(int yLableRotation) {
		this.yLableRotation = yLableRotation;
	}

	private int height=0;
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	private int width=0;
	
	
	
	public String getxMajorTickStep() {
		return xMajorTickStep;
	}

	public void setxMajorTickStep(String xMajorTickStep) {
		this.xMajorTickStep = xMajorTickStep;
	}

	public String getxMinorTickStep() {
		return xMinorTickStep;
	}

	public void setxMinorTickStep(String xMinorTickStep) {
		this.xMinorTickStep = xMinorTickStep;
	}

	public String getyMajorTickStep() {
		return yMajorTickStep;
	}

	public void setyMajorTickStep(String yMajorTickStep) {
		this.yMajorTickStep = yMajorTickStep;
	}

	public String getyMinorTickStep() {
		return yMinorTickStep;
	}

	public void setyMinorTickStep(String yMinorTickStep) {
		this.yMinorTickStep = yMinorTickStep;
	}

	private String yMajorTickStep= null; 
	private String yMinorTickStep= null; 
	
	public boolean ishGrid() {
		return hGrid;
	}

	public void sethGrid(boolean hGrid) {
		this.hGrid = hGrid;
	}

	public boolean isvGrid() {
		return vGrid;
	}

	public void setvGrid(boolean vGrid) {
		this.vGrid = vGrid;
	}

	//this is minor grid
	private boolean hGrid=false;
	
	private boolean vGrid=false;
	
	private String xAxisTitle;
	//int xAxisUnits;//default=1, 
	private String yAxisTitle;
	//int yAxisUnits;//default=1, 
	private String description;
	
	private String minX;
	private String maxX;
 
	private String minY;
	private String maxY;
	
	//gap between each bar
	//private String gap ="10"; ?
 
	
	//String [0] is value , String[1] is label
	private List<String[]> xLabels;
	private List<String[]> yLabels;
	
	public String getMinX() {
		return minX;
	}

	public void setMinX(String minX) {
		this.minX = minX;
	}

	public String getMaxX() {
		return maxX;
	}

	public void setMaxX(String maxX) {
		this.maxX = maxX;
	}

	public String getMinY() {
		return minY;
	}

	public void setMinY(String minY) {
		this.minY = minY;
	}

	public String getMaxY() {
		return maxY;
	}

	public void setMaxY(String maxY) {
		this.maxY = maxY;
	}
 	
	public AbstractVisualizationModelChart(int typeText,  String title) {
		super(typeText, title);
 	}
	
	public String getxAxisTitle() {
		return xAxisTitle;
	}
	public void setxAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}
	 
	public String getyAxisTitle() {
		return yAxisTitle;
	}
	public void setyAxisTitle(String yAxisTitle) {
		this.yAxisTitle = yAxisTitle;
	}
	 
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
 
	public List<String[]> getxLabels() {
		return this.xLabels;
	}

	public List<String[]> getyLabels() {
		return this.yLabels;
	}

	public void setxLabels(List<String[]> xLabels) {
		this.xLabels=xLabels;
		
	}

	public void setyLabels(List<String[]> yLabels) {
		this.yLabels=yLabels;
		
	}

	public void setMarkers(boolean markers) {
		this.markers = markers;
	}

	public boolean isMarkers() {
		return markers;
	}

	public void generateMaxMinAxis(MaxMinAxisValue maxMin) {
		setMaxX(String.valueOf(maxMin.getMaxX()));
		setMaxY(String.valueOf(maxMin.getMaxY()));
		setMinX(String.valueOf(maxMin.getMinX()));
		setMinY(String.valueOf(maxMin.getMinY()));
	
	}
 

}
