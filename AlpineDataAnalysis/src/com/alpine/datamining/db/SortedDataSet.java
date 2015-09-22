/**
 * ClassName SortedDataSet.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 *  This data set uses a mapping of indices to access the data provided by the 
 *  parent data set.
 *  @author Eason
 */
public class SortedDataSet extends AbstractDataSet {


	private static final long serialVersionUID = 8699050843148816770L;

	public static final int INCREASING = 0;
	public static final int DECREASING = 1;
	private static class SortingIndex implements Comparable<SortingIndex> {
		
		private Object key;
		private int index;
		
		public SortingIndex(Object key, int index) {
			this.key   = key;
			this.index = index;
		}

		public int hashCode() {
			if (key instanceof Double) {
				return ((Double)key).hashCode();
			} else if (key instanceof String) {
				return ((String)key).hashCode();
			} else {
				return 42;
			}
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof SortingIndex))
				return false;
			SortingIndex o = (SortingIndex)other;
			if (key instanceof Double) {
				return ((Double)key).equals(o.key);
			} else if (key instanceof String) {
				return ((String)key).equals(o.key);
			}
			return true;
		}
		
		public int compareTo(SortingIndex o) {
			if (key instanceof Double) {
				return ((Double)key).compareTo((Double)o.key);
			} else if (key instanceof String) {
				return ((String)key).compareTo((String)o.key);
			}
			return 0;
		}
		
		public int getIndex() { return index; }
		
		public String toString() { return key + " --> " + index; }
	}
	
	
	/** 
	 * The parent data set. 
	 */
	private DataSet parent;
	
    /**
	 *  The used mapping. 
	 */
    private int[] mapping;
    
    public SortedDataSet(DataSet parent, Column sortingColumn, int sortingDirection) {
    	this.parent = (DataSet)parent.clone();
		List<SortingIndex> sortingIndex = new ArrayList<SortingIndex>((int)parent.size());
		
		int counter = 0;
		Iterator<Data> i = parent.iterator();
		while (i.hasNext()) {
			Data data = i.next();
			if (sortingColumn.isNominal()) {
				sortingIndex.add(new SortingIndex(data.getNominalValue(sortingColumn), counter));
			} else {
				sortingIndex.add(new SortingIndex(Double.valueOf(data.getNumericalValue(sortingColumn)), counter));
			}
			counter++;
		}
		
		Collections.sort(sortingIndex);
		
		int[] mapping = new int[(int)parent.size()];
		counter = 0;
		Iterator<SortingIndex> k = sortingIndex.iterator();
		while (k.hasNext()) {
			int index = k.next().getIndex();
			if (sortingDirection == INCREASING) {
				mapping[counter] = index;
			} else {
				mapping[(int)(parent.size() - 1 - counter)] = index;
			}
			counter++;
		}
		
		this.mapping = mapping;
    }
    
    /**
     *  Constructs an data set based on the given sort mapping. 
     */
    public SortedDataSet(DataSet parent, int[] mapping) {
    	this.parent = (DataSet)parent.clone();
        this.mapping = mapping; 
    }

    /** 
     * Clone constructor. 
     */
    public SortedDataSet(SortedDataSet dataSet) {
    	this.parent = (DataSet)dataSet.parent.clone();
        this.mapping = new int[dataSet.mapping.length];
        System.arraycopy(dataSet.mapping, 0, this.mapping, 0, dataSet.mapping.length);
    }

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof SortedDataSet))
            return false;
        
        SortedDataSet other = (SortedDataSet)o;    
        if (this.mapping.length != other.mapping.length)
            return false;
        for (int i = 0; i < this.mapping.length; i++) 
            if (this.mapping[i] != other.mapping[i])
                return false;
        return true;
    }

    public int hashCode() {
        return super.hashCode() ^ this.mapping.hashCode();
    }
    
    /** 
     * Returns a SortedDataIterator. 
     */
    public Iterator<Data> iterator() {
        return new SortedDataIterator(this);
    }

    /** 
     * Returns the i-th data in the mapping. 
     */
    public Data getRow(int index) {
        if ((index < 0) || (index >= this.mapping.length)) {
            throw new RuntimeException("Given index '" + index + "' does not fit the mapped DataSet!");
        } else {
            return this.parent.getRow(this.mapping[index]);
        }
    }

    /** 
     * Counts the number of data. 
     */
    public long size() {
        return mapping.length;
    }

	public Columns getColumns() {
		return this.parent.getColumns();
	}

	public Table getDBTable() {
		return this.parent.getDBTable();
	}
}
