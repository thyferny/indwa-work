package com.alpine.miner.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:RowInfo
 * 
 * Author kemp zhang, john zhao
 * 
 * Version Ver 1.0
 * 
 * Date 2011-3-29
 * 
 * COPYRIGHT 2010 Alpine Solutions. All Rights Reserved.
 */
public class RowInfo {

	List<String> columnList = new ArrayList<String>();

	List<String[]> items = new ArrayList<String[]>();

	public RowInfo() {

	}

	public void addRow(String[] item) {
		items.add(item);
	}

	public List<String[]> getRowList() {
		return items;
	}

	public void setRowList(List<String[]> items) {
		this.items = items;
	}

	public List<String> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<String> columnList) {
		this.columnList = columnList;
	}

	public void addColumn(String col) {
		columnList.add(col);
	}
}
