/**
 * ClassName NominalColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.Iterator;



/**
 * This class holds all information on a single nominal column. In addition
 * to the generic column fields this class keeps information about the
 * nominal values and the value to index mappings. 
 
 * @author  Eason
 
 *          
 */
public class NominalColumn extends AbstractColumn {
	
	private static final long serialVersionUID = 960667636780727341L;

	/** 
	 * The maximum number of nominal values displayed in result strings for toString. 
	 */
	private static final int MAX_NUMBER_OF_SHOWN_NOMINAL_VALUES = 100;

	/**
	 * value mapping
	 */
	private Mapping mapping = new MappingImpl();
	
 

	/**
	 * Creates a simple column.
	 */
	public NominalColumn(String name, int valueType) {
		super(name, valueType);
		registerStats(new NominalColumnStats());

	}

	/**
	 * Clone constructor.
	 */
	private NominalColumn(NominalColumn a) {
		super(a);
		this.mapping = a.mapping;
	}
	
	/**
	 * Clones this column. 
	 */
	public Object clone() {
		return new NominalColumn(this);
	}

	public Mapping getMapping() {
		return this.mapping;
	}
	
	public void setMapping(Mapping newMapping) {
		this.mapping = new MappingImpl(newMapping);
	}
	
	public boolean isNominal() { 
		return true; 
	}
	
	public boolean isNumerical() { 
		return false; 
	}
	
	public boolean isCategory(){
		return true;
	}
	
	/**
	 * Returns a string representation and maps the value to a string .
	 */
	public String getAsString(double value, int digits, boolean quoteNominal) {
		if (Double.isNaN(value)) {
			return "?";
		} else {
            try {
                String result = getMapping().mapIndex((int) value); 
                if (quoteNominal) {
                	result = result.replaceAll("\"", "\\\\\"");
                	result = "\"" + result + "\"";
                }
                return result;
            } catch (Throwable e) {
                return "?";
            }
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append("/values=[");
		Iterator<String> i = this.mapping.getValues().iterator();
		int index = 0;
		while (i.hasNext()) {
			if (index >= MAX_NUMBER_OF_SHOWN_NOMINAL_VALUES) {
				result.append(", ... (" + (this.mapping.getValues().size() - MAX_NUMBER_OF_SHOWN_NOMINAL_VALUES) + " values) ...");
				break;
			}
			if (index != 0)
				result.append(", ");
			result.append(i.next());
			index++;
		}
		result.append("]");
		return result.toString();
	}
}
