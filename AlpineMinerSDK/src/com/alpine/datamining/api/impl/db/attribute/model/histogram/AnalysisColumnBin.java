/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.histogram;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;


/**
 * @author zhaoyong
 * 
 */
public class AnalysisColumnBin {

	public static final String TAG_NAME = "ColumnBin";

	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_BIN = "bin";
	
	private static final String ATTR_TYPE = "type";
	
	private static final String ATTR_IS_MIN = "isMin";
	
	private static final String ATTR_MIN = "min";
	
	private static final String ATTR_IS_MAX = "isMax";
	
	private static final String ATTR_MAX = "max";
	
	private static final String ATTR_WIDTH = "width";
	
	public static final int TYPE_BY_NUMBER = 0;
	
	public static final int TYPE_BY_WIDTH = 1;

	private String columnName = null;
	private int type=TYPE_BY_NUMBER;
	private Integer bin = 10;//Default Value;
	private double width = 0.0;//Default Value;
	private double min= 0.0;//Default Value;
	private double max= 100.0;//Default Value;
	private boolean isMin=false;
	private boolean isMax=false;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Integer getBin() {
		return bin;
	}

	public void setBin(Integer bin) {
		this.bin = bin;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public boolean isMin() {
		return isMin;
	}

	public void setIsMin(boolean isMin) {
		this.isMin = isMin;
	}

	public boolean isMax() {
		return isMax;
	}

	public void setIsMax(boolean isMax) {
		this.isMax = isMax;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public AnalysisColumnBin(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public AnalysisColumnBin(String columnName, Integer bin) {
		this.columnName = columnName;
		this.bin = bin;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AnalysisColumnBin columnBin=new AnalysisColumnBin( columnName,bin);
		columnBin.setType(type);
		columnBin.setWidth(width);
		columnBin.setMin(min);
		columnBin.setMax(max);
		columnBin.setIsMin(isMin);
		columnBin.setIsMax(isMax);
		return columnBin;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AnalysisColumnBin) {
			AnalysisColumnBin column = (AnalysisColumnBin) obj;
			return ModelUtility.nullableEquales(columnName, column
					.getColumnName())
					&& ModelUtility.nullableEquales(bin, column.getBin())
					&& ModelUtility.nullableEquales(isMin ,column.isMin())
					&& ModelUtility.nullableEquales(isMax ,column.isMax())
					&& ModelUtility.nullableEquales(type ,column.getType())
					&& ModelUtility.nullableEquales(width ,column.getWidth())
					&& ModelUtility.nullableEquales(min ,column.getMin())
					&& ModelUtility.nullableEquales(max ,column.getMax())
					;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ATTR_COLUMNNAME).append(":").append(columnName).append(",");
		sb.append(ATTR_TYPE).append(":").append(type).append(",");
		sb.append(ATTR_BIN).append(":").append(bin);
		sb.append(ATTR_WIDTH).append(":").append(width);
		sb.append(ATTR_IS_MIN).append(":").append(isMin).append(",");
		sb.append(ATTR_IS_MAX).append(":").append(isMax).append(",");
		sb.append(ATTR_MIN).append(":").append(min);
		sb.append(ATTR_MAX).append(":").append(max).append(",");
		return sb.toString();
	}

}