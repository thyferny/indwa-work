
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Iterator;


public interface Columns extends Iterable<Column>, Cloneable, Serializable {

	
	public Object clone();

	
	public boolean equals(Object o);

	
	public int hashCode();

	
	public Iterator<Column> iterator();

	
	public Iterator<Column> allColumns();

	
	public Iterator<Column> specialColumns();

	
	public int size();

	
	public int allSize();

	
	public void add(Column column);

	
	public void addRegular(Column column);


	
	public boolean remove(Column column);

	
	public Column get(String name);

	
	public Column getSpecial(String name);

	
	public Column getLabel();

	
	public void setLabel(Column label);

	
	public Column getPredictedLabel();

	
	public void setPredictedLabel(Column predictedLabel);

	
	public Column getId();

	
	public void setId(Column id);

	
	public void setSpecialColumn(Column column, String specialName);
}
