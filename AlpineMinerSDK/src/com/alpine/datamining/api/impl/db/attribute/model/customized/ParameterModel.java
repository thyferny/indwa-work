/**
 * ClassName ParameterModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-28
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.customized;

import java.io.Serializable;
import java.util.List;

public class ParameterModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2927680405294048657L;
	String paraName;
	String paraType;
	String defaultValue;
	List<String> optionalValue;
	String dataType;
	String position;
	
	
	public ParameterModel(String paraName) {
		this.paraName = paraName;
	}
	public String getParaName() {
		return paraName;
	}
	public void setParaName(String paraName) {
		this.paraName = paraName;
	}
	public String getParaType() {
		return paraType;
	}
	public void setParaType(String paraType) {
		this.paraType = paraType;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public List<String> getOptionalValue() {
		return optionalValue;
	}
	public void setOptionalValue(List<String> optionalValue) {
		this.optionalValue = optionalValue;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
}
