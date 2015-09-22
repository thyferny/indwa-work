/**
 * ClassName MultiDataTextAndTableListVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.List;

import com.alpine.miner.view.ui.dataset.MultiTextAndTableListEntity;

public class MultiDataTextAndTableListVisualizationOutPut extends
		TableVisualizationOutPut {

	private MultiTextAndTableListEntity multiTextAndTableListEntity;
	private List<DataTextAndTableListVisualizationOutPut> textAndTableListOutput;

	public MultiDataTextAndTableListVisualizationOutPut(MultiTextAndTableListEntity table){
		this.multiTextAndTableListEntity = table;
	}
	
	public MultiTextAndTableListEntity getMultiTextAndTableListEntity() {
		return multiTextAndTableListEntity;
	}


	@Override
	public Object getVisualizationObject() {
		return getMultiTextAndTableListEntity();
	}

	public List<DataTextAndTableListVisualizationOutPut> getTextAndTableListOutput() {
		return textAndTableListOutput;
	}

	public void setTextAndTableListOutput(
			List<DataTextAndTableListVisualizationOutPut> textAndTableListOutput) {
		this.textAndTableListOutput = textAndTableListOutput;
	}
	
}
