/**
 * ClassName Mapping.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.List;


/** 
 * store the values of the nominal column;
 * 
 * @author Eason 
 *
 */
public interface Mapping extends Cloneable, Serializable {

	/** 
	 * Clone 
	 */
	public Object clone();
	
	/**
	 * @param nominalValue
	 * @return the internal numeric representation 
	 */
	public int getIndex(String nominalValue);

	/**
	 * @param nominalValue
	 * @return  the internal numeric representation 
	 */
	public int mapString(String nominalValue);

	/**
	 * @param index
	 * @return the nominal value for an internal numeric representation.
	 */
	public String mapIndex(int index);
	
	/**
	 * Sets the given mapping.
	 * @param nominalValue
	 * @param index
	 */
	public void setMapping(String nominalValue, int index);
	
	/**
	 * @return a list of all nominal values
	 */
	public List<String> getValues();
	
	/**
	 * @return the number of different nominal values.
	 */
	public int size();
	
}

