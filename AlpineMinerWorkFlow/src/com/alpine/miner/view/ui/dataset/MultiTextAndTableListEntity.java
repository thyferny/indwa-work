/**
 * ClassName MultiTextAndTableListEntity.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-18
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.view.ui.dataset;

import java.util.List;
import java.util.Map;


public class MultiTextAndTableListEntity{
	private List<TextAndTableListEntity> textAndTableListEntityList;
	private String[] avaiableValue;
	private Map<String,TextAndTableListEntity> nameEntityMap;

	public List<TextAndTableListEntity> getTextAndTableListEntityList() {
		return textAndTableListEntityList;
	}

	public void setTextAndTableListEntityList(
			List<TextAndTableListEntity> textAndTableListEntityList) {
		this.textAndTableListEntityList = textAndTableListEntityList;
	}

	public String[] getAvaiableValue() {
		return avaiableValue;
	}

	public void setAvaiableValue(String[] avaiableValue) {
		this.avaiableValue = avaiableValue;
	}

	public Map<String, TextAndTableListEntity> getNameEntityMap() {
		return nameEntityMap;
	}

	public void setNameEntityMap(Map<String, TextAndTableListEntity> nameEntityMap) {
		this.nameEntityMap = nameEntityMap;
	}
	
}
