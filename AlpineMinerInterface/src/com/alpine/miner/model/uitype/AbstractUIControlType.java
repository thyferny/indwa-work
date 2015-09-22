/**
 * ClassName AbstractUIControlType.java
 *
 * Version information: 1.00
 *
 * Data: 2011-3-31
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.model.uitype;

import java.util.ArrayList;
import java.util.HashMap;


public abstract class AbstractUIControlType implements UIControlType {
	
	public static final String TEXT_CONTROL_TYPE="text";
	public static final String COMBO_CONTROL_TYPE="combo";
	public static final String BUTTON_CONTROL_TYPE="button";
	public static final String CHECK_CONTROL_TYPE="check";
	public static final String UDF_COLUMNNAME_CONTROL_TYPE="columndialog";
	
	private HashMap<String,String> controlTypeMap=new HashMap<String,String>();
	private ArrayList<String> controlList=new ArrayList<String>();
	
	protected void addControlType(String column,String controlType){
		controlTypeMap.put(column, controlType);
		controlList.add(column);
	}
	
	@Override
	public String getControlType(String column) {
		return controlTypeMap.get(column);
	}
	
	public ArrayList<String> getControlList(){
		return controlList;
	}
	public AbstractUIControlType() {
		init();
	}
	abstract protected void init();

}
