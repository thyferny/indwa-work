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

import com.alpine.miner.workflow.output.AbstractVisualizationModel;

public class VisualizationModelBoxWhisker extends AbstractVisualizationModel {
	// type value, series
	List<VisualBoxWhiskerGroup> boxWhiskers;// group is by series value
	String xDataType;// number or category for x axis lable

	
	// Y value
	String valueDomain;
	// x value
	String typeDomain;
	
	// z value (series...)
	String seriesDomain;
	
	private List<VisualLabel> xLabels;

    private List<String> xValues;
	private double maxX;

	private double minX;
	private double maxY;
	private double minY;

    private double numX;

	public VisualizationModelBoxWhisker(String title,
			List<VisualBoxWhiskerGroup> boxWhiskers, String seriesDomain,
			String typeDomain, String valueDomain) {
		super(TYPE_BOXANDWHISKER, title);
		this.boxWhiskers = boxWhiskers;
		this.seriesDomain = seriesDomain;
		this.typeDomain = typeDomain;
		this.valueDomain = valueDomain;

	}

	public List<VisualBoxWhiskerGroup> getBoxWhiskers() {
		return boxWhiskers;
	}

	public void setBoxWhiskers(List<VisualBoxWhiskerGroup> boxWhiskers) {
		this.boxWhiskers = boxWhiskers;
	}

	public String getSeriesDomain() {
		return seriesDomain;
	}

	public void setSeriesDomain(String seriesDomain) {
		this.seriesDomain = seriesDomain;
	}

	public String getTypeDomain() {
		return typeDomain;
	}

	public void setTypeDomain(String typeDomain) {
		this.typeDomain = typeDomain;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;

	}

	public void setMinX(double minX) {
		this.minX = minX;

	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;

	}

	public void setMinY(double minY) {
		this.minY = minY;

	}

    public void setNumX(double numX) {
        this.numX = numX;
    }

	public String getValueDomain() {
		return valueDomain;
	}

	public void setValueDomain(String valueDomain) {
		this.valueDomain = valueDomain;
	}

	public String getxDataType() {
		return xDataType;
	}

	public void setxDataType(String xDataType) {
		this.xDataType = xDataType;
	}

	public List<VisualLabel> getxLabels() {
		return xLabels;
	}

	public void setxLabels(List<VisualLabel> xLabels) {
		this.xLabels = xLabels;
	}

    public List<String> getxValues() {
        return xValues;
    }

    public void setxValues(List<String> xValues) {
        this.xValues = xValues;
    }

	public double getMaxX() {
		return maxX;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinY() {
		return minY;
	}

    public double getNumX() {
        return numX;
    }

}
