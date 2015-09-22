/**
 * ClassName VisualPoint.java
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

/**a line can have multiple points... not a simple line(two points)...
 * */
public class VisualPointGroup  {
	
	 List<VisualPoint> points;
	 
		String label=null;
		
		private String color=null;
		public String getLabel() {
			return label;
		}
	 
		public void setLabel(String label) {
			this.label = label;
		}

		
	public VisualPointGroup(String label) {
		this.label=label;
	}
	
	public VisualPointGroup( ) {
	}
	public VisualPointGroup(String label,List<VisualPoint> points) {
		this.label=label;
		this.points=points;
	}

	public List<VisualPoint> getPoints() {
		return points;
	}

	public void setPoints(List<VisualPoint> points) {
		this.points = points;
	}
	public void addVisualPoint(VisualPoint point){
		if(points==null){
			points =new ArrayList<VisualPoint> ();
		}
		points.add(point) ;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}


}
