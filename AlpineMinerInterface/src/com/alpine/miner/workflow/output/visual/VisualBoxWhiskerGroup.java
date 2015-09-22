/**
 * ClassName VisualBoxWhisker.java
 *
 * Version information: 3.00
 *
 * Data: 2011-7-11
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

 //grouped by type value
public class VisualBoxWhiskerGroup  {

	private String fillColor=null;
	public String getFillColor() {
		return fillColor;
	}
	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}
	//a group is a series will all x value (type)
	private String series;
    private boolean active;
	private List<VisualBoxWhisker> boxWhiskers;
	
	public VisualBoxWhiskerGroup(String series, List<VisualBoxWhisker> boxWhiskers, boolean active) {
 
		this.series = series;
		this.boxWhiskers = boxWhiskers;
        this.active = active;
	}
 
	public String getSeries() {
		return series;
	}
	public void setSeries(String series) {
		this.series = series;
	}
	public List<VisualBoxWhisker> getBoxWhiskers() {
		return boxWhiskers;
	}
	public void setBoxWhiskers(List<VisualBoxWhisker> boxWhiskers) {
		this.boxWhiskers = boxWhiskers;
	}

     public boolean getActive()
     {
         return active;
     }

     public void setActive(boolean active)
     {
         this.active = active;
     }
	
 
}
