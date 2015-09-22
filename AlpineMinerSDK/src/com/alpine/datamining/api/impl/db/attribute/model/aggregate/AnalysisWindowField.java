/**
 * ClassName :WindowField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.aggregate;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;

/**
 * @author zhaoyong
 * 
 */
public class AnalysisWindowField {
	public static final String TAG_NAME = "WindowField";
	
	String resultColumn;
	String windowFunction;
	String windowSpecification;
	String dataType;

	/**
	 * @param resultColumn2
	 * @param windowFunction2
	 * @param windowSpecification2
	 * @param dataType2
	 */
	public AnalysisWindowField(String resultColumn, String windowFunction,
			String windowSpecification, String dataType) {
		this.resultColumn = resultColumn;
		this.windowFunction = windowFunction;
		this.windowSpecification = windowSpecification;
		this.dataType = dataType;
	}

	public String getResultColumn() {
		return resultColumn;
	}

	public void setResultColumn(String resultColumn) {
		this.resultColumn = resultColumn;
	}

	public String getWindowFunction() {
		return windowFunction;
	}

	public void setWindowFunction(String windowFunction) {
		this.windowFunction = windowFunction;
	}

	public String getWindowSpecification() {
		return windowSpecification;
	}

	public void setWindowSpecification(String windowSpecification) {
		this.windowSpecification = windowSpecification;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisWindowField(resultColumn, windowFunction,
				windowSpecification, dataType);
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AnalysisWindowField) {
			AnalysisWindowField field = (AnalysisWindowField) obj;
			return ModelUtility.nullableEquales(resultColumn,
					field.getResultColumn())
					&& ModelUtility.nullableEquales(windowFunction,
							field.getWindowFunction())
					&& ModelUtility.nullableEquales(windowSpecification,
							field.getWindowSpecification())
					&& ModelUtility.nullableEquales(dataType,
							field.getDataType());

		} else {
			return false;
		}
	}

}