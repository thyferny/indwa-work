package com.alpine.datamining.api.impl.visual;

import org.eclipse.swt.graphics.Image;

import com.alpine.datamining.api.impl.visual.widgets.VisualizationChart;

public class MultiChartImageVisualizationOutput extends
		ImageVisualizationOutPut {
	
	private int showWidth =0;
	private int showHeight =0;
	private VisualizationChart visualizationChart;
	private Image image;
	
	public MultiChartImageVisualizationOutput(VisualizationChart visualizationChart) {
		super();
		this.visualizationChart = visualizationChart;
	}
	
	@Override
	public Object getVisualizationObject() {
		return visualizationChart;
	}
	public int getShowWidth() {
		return showWidth;
	}
	public void setShowWidth(int showWidth) {
		this.showWidth = showWidth;
	}
	public int getShowHeight() {
		return showHeight;
	}
	public void setShowHeight(int showHeight) {
		this.showHeight = showHeight;
	}

	public VisualizationChart getVisualizationChart() {
		return visualizationChart;
	}
	public void setVisualizationChart(VisualizationChart visualizationChart) {
		this.visualizationChart = visualizationChart;
	}

	public void setImage(Image image) {
		this.image=image;		
	}
	public Image getImage() {
		return image;
	}
	
}
