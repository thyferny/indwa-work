package com.alpine.hadoop.ext;


public class XPathItem {
	int columnIndex =-1; //start from 0  -1 means this is a virtru node xpath is not in column
	 String thisLevelXPath;

	public XPathItem(int columnIndex, String thisLevelXPath) {
		super();
		this.columnIndex = columnIndex;
		this.thisLevelXPath = thisLevelXPath;
	}
	@Override
	public String toString() {
		return "XPathElement [columnIndex=" + columnIndex +   ", thisLevelXPath=" + thisLevelXPath + "]";
	}


	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
 
	
	
	public  String getThisLevelXPath() {
		return thisLevelXPath;
	}
	public void setThisLevelXPath(String thisLevelXPath) {
		this.thisLevelXPath = thisLevelXPath;
	}
}
