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

import java.util.ArrayList;
import java.util.List;


public class VisualizationModelBarChart extends AbstractVisualizationModelChart {
	private String seriesName = "";
	//the gap between the bargroups 
	//private String gap="10" ;
  
  //index mapping to xValues
	private List<BarchartSeries> series= new ArrayList<BarchartSeries>();
 

	public String getSeriesName() {
		return seriesName;
	}


	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}


	public List<BarchartSeries> getSeries() {
		return series;
	}


	public void setSeries(List<BarchartSeries> series) {
		this.series = series;
	}


	public VisualizationModelBarChart(String title,String seriesName,List<BarchartSeries> series) {
		super(TYPE_BAR_CHART,title);
		this.series=series;
		this.seriesName=seriesName;

	 
	}


 
 
}
