/**   
 * ClassName AlpineUtil.java
 *   
 * Author   john zhao   
 *
 * Version  Ver 3.0
 *   
 * Date     2011-3-29    
 * 
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 */

package com.alpine.miner.workflow.output.visual;

public class MaxMinAxisValue {
	double minX = 0f, minY = 0f;
	double maxX = 0f, maxY = 0f;
	
	public double getMinX() {
		return minX;
	}
	public void setMinX(double minX) {
		this.minX = minX;
	}
	public double getMinY() {
		return minY;
	}
	public void setMinY(double minY) {
		this.minY = minY;
	}
	public double getMaxX() {
		return maxX;
	}
	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}
	public double getMaxY() {
		return maxY;
	}
	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
	public void compareXY(String x ,String y){
		double xValue = Double.parseDouble(x);
		double yValue = Double.parseDouble(y);
		this.compareXY(xValue,yValue);
	}
	public void compareXY(double xValue ,double yValue){
		if(minX>xValue){
			minX=xValue;
		}
		if(maxX<xValue){
			maxX=xValue ;
		}
		if(minY>yValue){
			minY=yValue;
		}
		if(maxY<yValue){
			maxY=yValue ;
		}
	}
	
}
