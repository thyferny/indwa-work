/**
 * ClassName MappingImpl.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.utility.Tools;




/**
 * This is an implementation of {@link MappingImpl} which can
 * be used for nominal  .
 * 
 * @author Eason
 */
public class MappingImpl implements Mapping  {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7165849119746756619L;

	/** The map between symbolic values and their indices. */
	private Map<String, Integer> symbolToIndexMap = new HashMap<String, Integer>();

	/** The map between indices of nominal values and the actual nominal value. */
	private List<String> indexToSymbolMap = new ArrayList<String>();
	
	public MappingImpl() {}
	
	MappingImpl(Mapping mapping) {
		this.symbolToIndexMap.clear();
		this.indexToSymbolMap.clear();
		for (int i = 0; i < mapping.size(); i++) {
			int index = i;
			String value = mapping.mapIndex(index);
			this.symbolToIndexMap.put(value, index);
			this.indexToSymbolMap.add(value);
		}
	}
	
	public Object clone() {
		return new MappingImpl(this);
	}

	/**
	 * Returns the index for the nominal column value <code>str</code>.
	 */
	public int mapString(String str) {
		if (str == null)
			return -1;
		// lookup string in hashtable
		int index = getIndex(str);
		// if string is not yet in the map, add it
		if (index < 0) {
			// new string -> insert
			indexToSymbolMap.add(str);
			index = indexToSymbolMap.size() - 1;
			symbolToIndexMap.put(str, index);
		}
		return index;
	}

	/**
	 * Returns the index of the given nominal value
	 */
	public int getIndex(String str) {
		Integer index = symbolToIndexMap.get(str);
		if (index == null)
			return -1;
		else
			return index.intValue();
	}

	/**
	 * Returns the column value, that is associated with this index.
	 */
	public String mapIndex(int index) {
		if ((index < 0) || (index >= indexToSymbolMap.size()))
		{return null;}
			//throw new TypeException("Cannot map index of nominal column to nominal value: index " + index + " is out of bounds!");
		return indexToSymbolMap.get(index);
	}

	/** Sets the given mapping. Please note that this will overwrite existing mappings and might
	 *  cause data changes in this way. */
	public void setMapping(String nominalValue, int index) {
		String oldValue = indexToSymbolMap.get(index);
		indexToSymbolMap.set(index, nominalValue);
		symbolToIndexMap.remove(oldValue);
		symbolToIndexMap.put(nominalValue, index);
	}
	
	/** Returns the values of the column as an enumeration of strings. */
	public List<String> getValues() {
		return indexToSymbolMap;
	}

	/** Returns the number of different nominal values. */
	public int size() {
		return indexToSymbolMap.size();
	}

	
	public String toString() {
		return indexToSymbolMap.toString() + Tools.getLineSeparator() + symbolToIndexMap.toString();
	}
}
