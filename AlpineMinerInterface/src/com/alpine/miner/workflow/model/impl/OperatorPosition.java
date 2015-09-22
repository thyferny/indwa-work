/**
 * ClassName OperatorPosition.java
 *
 * Version information: 1.00
 *
 * Data: 2011-3-31
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model.impl;

public class OperatorPosition {
	
	private int startX;
	private int startY;
	private int x;
	private int y;
	
	
	public OperatorPosition(int x, int y) {
		this(0,0,x,y);
	}


	public OperatorPosition(int startX, int startY, int x, int y) {
		this.startX = startX;
		this.startY = startY;
		this.x = x;
		this.y = y;
	}


	public int getStartX() {
		return startX;
	}


	public void setStartX(int startX) {
		this.startX = startX;
	}


	public int getStartY() {
		return startY;
	}


	public void setStartY(int startY) {
		this.startY = startY;
	}


	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}



	

}
