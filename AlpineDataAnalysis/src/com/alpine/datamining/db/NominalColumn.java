
package com.alpine.datamining.db;

import java.util.Iterator;




public class NominalColumn extends AbstractColumn {
	
	private static final long serialVersionUID = 960667636780727341L;

	
	private static final int MAX_NUMBER_OF_SHOWN_NOMINAL_VALUES = 100;

	
	private Mapping mapping = new MappingImpl();
	
 

	
	public NominalColumn(String name, int valueType) {
		super(name, valueType);
		registerStats(new NominalColumnStats());

	}

	
	private NominalColumn(NominalColumn a) {
		super(a);
		this.mapping = a.mapping;
	}
	
	
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
