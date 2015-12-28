
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Iterator;


public interface Column extends Cloneable, Serializable {
	
	public static final int REGULAR = 0;
	
	
	public static final int SPECIAL = 1;
	
	
	public static final int ALL = 2;
	
	
	public static final String CONFIDENCE_NAME = "C";
	
	
	public static final String ID_NAME = "ALPINE_ID_NAME";

	
	public static final String DEPENDENT_NAME = "ALPINE_DEPENDENT_NAME";

	
	public static final String PREDICTION_NAME = "P";
	

	
	public static final int UNDEFINED_COLUMN_INDEX = -1;

	
	public static final String MISSING_NOMINAL_VALUE = "?";

	
	public boolean equals(Object o);

	
	public int hashCode();

	
	public Object clone();

	
	public String getName();

	
	public void setName(String name);

	
	public int getTableIndex();

	
	
	public void setTableIndex(int index);

	
	public Mapping getMapping();

	
	public void setMapping(Mapping mapping);

	
	public double getValue(Row row);

	
	public void setValue(Row row, double value);

	
	public int getValueType();

	
	public String toString();

	
	public void setDefault(double value);

	
	public double getDefault();

	
	public boolean isNominal();

	
	public boolean isNumerical();

	
	public boolean isCategory();

	
	public String getAsString(double value, int digits, boolean quoteNominal);

	
	public Iterator<ColumnStats> getAllStats();

	
	public boolean isSpecial();

	
	public String getSpecialName();

	
	public void setSpecialName(String specialName);

	
	public void registerStats(ColumnStats columnStats);

}
