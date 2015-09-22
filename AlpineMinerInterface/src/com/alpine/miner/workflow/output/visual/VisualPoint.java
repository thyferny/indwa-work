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


public class VisualPoint  {
    //use string to avoid the scale problem for float and double
	String x;
	String y;
	
 
	
	public String getX() {
		return x;
	}



	public void setX(String x) {
		this.x = x;
	}



	public String getY() {
		return y;
	}



	public void setY(String y) {
		this.y = y;
	}



	public VisualPoint(String x,String y) {
		this.x=x;
		this.y=y;
	}
	
	 
	 

}
