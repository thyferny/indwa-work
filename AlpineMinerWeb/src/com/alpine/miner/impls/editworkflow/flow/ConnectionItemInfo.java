/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ConnectionItemInfo.java
 */
package com.alpine.miner.impls.editworkflow.flow;

/**
 * @author Gary
 * Aug 2, 2012
 */
public class ConnectionItemInfo {

	private String 	sourceId,
					targetId;
	private int x1, y1, x2, y2;
	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	public ConnectionItemInfo() {
		
	}
	
	public ConnectionItemInfo(String sourceId, String targetId, int x1, int y1, int x2, int y2){
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
}
