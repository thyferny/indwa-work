
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.List;



public interface Mapping extends Cloneable, Serializable {

	
	public Object clone();
	
	
	public int getIndex(String nominalValue);

	
	public int mapString(String nominalValue);

	
	public String mapIndex(int index);
	
	
	public void setMapping(String nominalValue, int index);
	
	
	public List<String> getValues();
	
	
	public int size();
	
}

