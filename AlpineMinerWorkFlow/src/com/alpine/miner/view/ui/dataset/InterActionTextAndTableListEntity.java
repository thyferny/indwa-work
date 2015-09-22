/**
 * ClassName InterActionTextAndTableListEntity.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.view.ui.dataset;

import java.util.HashMap;

public class InterActionTextAndTableListEntity extends TextAndTableListEntity {
	private HashMap<String, String[]> columnColumnMap;
	private String opName;
	private boolean check=true;
	
	public HashMap<String, String[]> getColumnColumnMap() {
		return columnColumnMap;
	}

	public void setColumnColumnMap(HashMap<String, String[]> columnColumnMap) {
		this.columnColumnMap = columnColumnMap;
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	
}
