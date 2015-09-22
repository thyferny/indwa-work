/**
 * ClassName DataTableVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;

public class DataTextAndTableListVisualizationOutPut extends TableVisualizationOutPut {

	private TextAndTableListEntity textAndTable;

	public DataTextAndTableListVisualizationOutPut(TextAndTableListEntity table){
		this.textAndTable = table;
	}
	public TextAndTableListEntity getTableEntityList(){
		return textAndTable;
	}
	@Override
	public Object getVisualizationObject() {
		return getTableEntityList();
	}
}
