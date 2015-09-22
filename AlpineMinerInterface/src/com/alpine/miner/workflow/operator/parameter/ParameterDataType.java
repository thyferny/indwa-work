/**
 * ClassName ParameterDataType.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter;

/**
 * @author zhaoyong
 *
 */

//this type is used for validation in UI
public interface ParameterDataType { 
	public static final String STRING="string";
	public static final String INT="int";
	//float 0-1 ,
	public static final String PERCENT="persent";
	public static final String DOUBLE="double";		
	public static final String BOOLEAN="boolean";
	public static final String OBJECT="object";
	//PT_DATE,
	public static final String UNKNOWN="uknown"; // it is bad to get this one.
}