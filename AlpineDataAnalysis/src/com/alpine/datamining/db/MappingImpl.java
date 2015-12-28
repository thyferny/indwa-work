
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.utility.Tools;





public class MappingImpl implements Mapping  {


	
	private static final long serialVersionUID = 7165849119746756619L;

	
	private Map<String, Integer> symbolToIndexMap = new HashMap<String, Integer>();

	
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

	
	public int getIndex(String str) {
		Integer index = symbolToIndexMap.get(str);
		if (index == null)
			return -1;
		else
			return index.intValue();
	}

	
	public String mapIndex(int index) {
		if ((index < 0) || (index >= indexToSymbolMap.size()))
		{return null;}
			//throw new TypeException("Cannot map index of nominal column to nominal value: index " + index + " is out of bounds!");
		return indexToSymbolMap.get(index);
	}

	
	public void setMapping(String nominalValue, int index) {
		String oldValue = indexToSymbolMap.get(index);
		indexToSymbolMap.set(index, nominalValue);
		symbolToIndexMap.remove(oldValue);
		symbolToIndexMap.put(nominalValue, index);
	}
	
	
	public List<String> getValues() {
		return indexToSymbolMap;
	}

	
	public int size() {
		return indexToSymbolMap.size();
	}

	
	public String toString() {
		return indexToSymbolMap.toString() + Tools.getLineSeparator() + symbolToIndexMap.toString();
	}
}
