
package com.alpine.datamining.db;



public class NumericColumn extends AbstractColumn {

	private static final long serialVersionUID = 1L;

	
	public static final int DEFAULT_NUMBER_OF_DIGITS = -1;
	
	
	public static final int UNLIMITED_NUMBER_OF_DIGITS = -2;

 
	
	private boolean category;
	private Mapping mapping = new MappingImpl();

	
	public NumericColumn(String name, int valueType) {
		super(name, valueType);
		registerStats(new NumericColumnStats());

	}
	
	
	private NumericColumn(NumericColumn a) {
		super(a);
		this.category = a.category;
		this.mapping = a.mapping;
	}

	
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
