/**
 * ClassName VisualizationModelChart.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

public interface VisualizationModelChart extends VisualizationModel {
 
	//default is 1 ,2 ,3,4,5...
	public List<String[]> getxLabels();
	public List<String[]> getyLabels();
	
	//String [0] is value , String[1] is label
	public void setxLabels(List<String[]> xLabels);
	public void setyLabels(List<String[]> yLabels);
 
	public String getxAxisTitle();
	public void setxAxisTitle(String xAxisTitle);
 
	public String getyAxisTitle();
	public void setyAxisTitle(String yAxisTitle);

	//some chart need the description
	public String getDescription();
	public void setDescription(String description);

	//this min and mx x and y is for the chart's axis
	//if not set, the chart will use the default value
	public String getMinX() ;
	public void setMinX(String minX) ;
	public String getMaxX() ;
	public void setMaxX(String maxX) ;
	public String getMinY() ;
	public void setMinY(String minY) ;
	public String getMaxY() ;
	public void setMaxY(String maxY) ;

	//this is for the tick of the axis
	public String getxMajorTickStep();
	public void setxMajorTickStep(String xMajorTickStep);
	public String getxMinorTickStep();
	public void setxMinorTickStep(String xMinorTickStep) ;
	public String getyMajorTickStep() ;
	public void setyMajorTickStep(String yMajorTickStep) ;
	public String getyMinorTickStep() ;
	public void setyMinorTickStep(String yMinorTickStep) ;

	//height and width, the size of chart
	//if not set, will use default 420*420 defined in the chart.js
	public int getHeight()  ;
	public void setHeight(int height) ;
	public int getWidth();
	public void setWidth(int width) ;
	public int getxLableRotation();
	public void setxLableRotation(int xLableRotation);
	public int getyLableRotation();
	public void setyLableRotation(int yLableRotation);
	 

}
