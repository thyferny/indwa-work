/**
 * ClassName NumericColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;


/** 
 * Numeric Column, for example, integer, float,...
 * 
 * @author Eason
 *
 */
public class NumericColumn extends AbstractColumn {

	private static final long serialVersionUID = 1L;

	/**
	 * Indicates the default fraction digits number which is defined by the system
	 *  property  numbers.
	 */
	public static final int DEFAULT_NUMBER_OF_DIGITS = -1;
	
	/** 
	 * Indicates an fraction digits unlimited number. 
	 */
	public static final int UNLIMITED_NUMBER_OF_DIGITS = -2;

 
	/**
	 * whether category, true if integer dependent column or text column
	 */
	private boolean category;
	private Mapping mapping = new MappingImpl();

	/**
	 * Creates a simple column.
	 */
	public NumericColumn(String name, int valueType) {
		super(name, valueType);
		registerStats(new NumericColumnStats());

	}
	
	/**
	 * Clone constructor.
	 */
	private NumericColumn(NumericColumn a) {
		super(a);
		this.category = a.category;
		this.mapping = a.mapping;
	}

	/** Clones this column. */
	public Object clone() {
		return new NumericColumn(this);
	}
	
	public boolean isNominal() {
		return false;
	}
	
	public boolean isNumerical() { 
		return true; 
	}
	
	public boolean isCategory(){
		return category;
	}

	public void setCategory(boolean category){
		this.category = category; 
	}
	public Mapping getMapping() {
		if(isCategory()){
			return mapping;
		}else{
			throw new UnsupportedOperationException("The   numerical columns method does not support getMapping().");
		}
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}
	
	/**
	 * Returns a string representation of value.
	 */
	public String getAsString(double value, int numberOfDigits, boolean quoteNominal) {
		if (Double.isNaN(value)) {
			return "?";
		} else {
			
			switch (numberOfDigits) {
			case UNLIMITED_NUMBER_OF_DIGITS:
				return Double.toString(value);
			case DEFAULT_NUMBER_OF_DIGITS:
				return String.valueOf(value);
			default:
				return String.valueOf(value);
			}
			
		}
	}
}
