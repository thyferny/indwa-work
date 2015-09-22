/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WoeCalculate
 * Jan 10, 2012
 */
package com.alpine.miner.impls.woe;


/**
 * @author Gary
 * For calculate request and response.
 */
public class WoeCalculateElement {

	private String 	columnName;
	private WOEModelUI.DataType dataType;
	private double 	gini,
					inforValue;
	
	private WoeCalculateInfoNode[] InforList;
	
	public static class WoeCalculateInfoNode{
		private String groupInfo;
		private double WOEValue;
		
		private String 	bottom,// for dataType with NUMERIC
						upper;// for dataType with NUMERIC
		
		private String[] choosedList;//for dataType with TEXT

		public String getGroupInfo() {
			return groupInfo;
		}

		public void setGroupInfo(String groupInfo) {
			this.groupInfo = groupInfo;
		}

		public double getWOEValue() {
			return WOEValue;
		}

		public void setWOEValue(double wOEValue) {
			WOEValue = wOEValue;
		}

		public String getBottom() {
			return bottom;
		}

		public void setBottom(String bottom) {
			this.bottom = bottom;
		}

		public String getUpper() {
			return upper;
		}

		public void setUpper(String upper) {
			this.upper = upper;
		}

		public String[] getChoosedList() {
			return choosedList;
		}

		public void setChoosedList(String[] choosedList) {
			this.choosedList = choosedList;
		}
		
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public WOEModelUI.DataType getDataType() {
		return dataType;
	}

	public void setDataType(WOEModelUI.DataType dataType) {
		this.dataType = dataType;
	}

	public double getGini() {
		return gini;
	}

	public void setGini(double gini) {
		this.gini = gini;
	}

	public double getInforValue() {
		return inforValue;
	}

	public void setInforValue(double inforValue) {
		this.inforValue = inforValue;
	}

	public WoeCalculateInfoNode[] getInforList() {
		return InforList;
	}

	public void setInforList(WoeCalculateInfoNode[] inforList) {
		InforList = inforList;
	}

}
