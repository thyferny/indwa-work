/**
 * ClassName ParameterInputType.java
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
public interface ParameterInputType {	 
	public static final String SINGLE_SELECT="SS";
	public static final	String	MULTI_SELECT="MS";// return a string separated by ,
	public static final	String	SIMPLE_INPUT="SI";
	public static final	String	CUTOMIZE="CU";//like a dialog  
	public static final String TEXT = "TX"; //like sql execute,multi line text.
	 
}
