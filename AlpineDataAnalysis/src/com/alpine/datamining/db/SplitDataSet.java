/**
 * ClassName SplitDataSet.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.alpine.datamining.utility.Tools;


/**
 * An data set that can be split into subsets by using a  Subsets.
 * @author Eason
 */
public class SplitDataSet extends AbstractDataSet {

	private static final long serialVersionUID = -7308885713189119662L;

	
	/** 
	 * The partition. 
	 */
	private Subsets partition;
	
	/** 
	 * The parent data set. 
	 */
	private DataSet parent;
	
	
	/** 
	 * Constructs a SplitDataSet with the given partition. 
	 */
	public SplitDataSet(DataSet dataSet, Subsets partition) {
		this.parent = (DataSet)dataSet.clone();
		this.partition = partition;
	}
    
    
	/** 
	 * Clone constructor. 
	 */
	public SplitDataSet(SplitDataSet dataSet) {
    	this.parent = (DataSet)dataSet.parent.clone();
		this.partition = (Subsets) dataSet.partition.clone();
		setStatisticsCaculatedFlag(false);
	}

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof SplitDataSet))
            return false;
        return this.partition.equals(((SplitDataSet)o).partition);
    }

    public int hashCode() {
        return super.hashCode() ^ partition.hashCode();
    }
    
	/** 
	 *Selects exactly one subset.
	 */
	public void selectSingleSubset(int index) {
		partition.clearSelection();
		partition.selectSubset(index);
		setStatisticsCaculatedFlag(false);
	}

	/** 
	 * Returns the number of subsets. 
	 */
	public int getNumberOfSubsets() {
		return partition.getNumberOfSubsets();
	}

	/** 
	 * Returns an data reader that splits all data that are not selected. 
	 */
	public Iterator<Data> iterator() {
		return new IndexDataIterator(this);
	}

	public long size() {
		return partition.getSelectionSize();
	}

	public Data getRow(int index) {
		int actualIndex = partition.mapIndex(index);
		return this.parent.getRow(actualIndex);
	}

	public Table getDBTable() {
		return parent.getDBTable();
	}
	
	public Columns getColumns() {
		return this.parent.getColumns();
	}


	/**
	 * @param dataSet
	 * @param column
	 * @return SplitDataSet
	 */
	public static SplitDataSet splitByColumn(DataSet dataSet, Column column) {
		int[] elements = new int[(int)dataSet.size()];
		int i = 0;
		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		AtomicInteger currentIndex = new AtomicInteger(0);
		for (Data data : dataSet) {
			int value = (int) data.getValue(column);
			Integer indexObject = indexMap.get(value);
			if (indexObject == null) {
				indexMap.put(value, currentIndex.getAndIncrement());
			}
			int intValue = indexMap.get(value).intValue();
			elements[i++] = intValue;
		}
		
		int maxNumber = indexMap.size();
		indexMap.clear();
		Subsets partition = new Subsets(elements, maxNumber);
		return new SplitDataSet(dataSet, partition);
	}
	/**
	 * @param dataSet
	 * @param column
	 * @param values
	 * @return SplitDataSet split by nominal String List.
	 */
	public static SplitDataSet splitByColumn(DataSet dataSet, Column column, List<String> values) {
		int[] elements = new int[(int)dataSet.size()];
		int i = 0;
		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		AtomicInteger currentIndex = new AtomicInteger(0);
		for (Data data : dataSet) {
			int value = 0;
			String columnValue = data.getNominalValue(column);
			if (values.contains(columnValue))
			{
				value = 0;
			}
			else
			{
				value = 1;
			}
			Integer indexObject = indexMap.get(value);
			if (indexObject == null) {
				indexMap.put(value, currentIndex.getAndIncrement());
			}
			int intValue = indexMap.get(value).intValue();
			elements[i++] = intValue;
		}
		
		int maxNumber = indexMap.size();
		indexMap.clear();
		Subsets partition = new Subsets(elements, maxNumber);
		return new SplitDataSet(dataSet, partition);
	}
    
    /**
     * @param dataSet
     * @param column
     * @param value
     * @return an data set splitted into
     * two parts containing all data providing a greater (smaller) value
     * for the given column than the given value. The first partition contains
     * all data providing a smaller or the same value than the given one.
     */
    public static SplitDataSet splitByColumn(DataSet dataSet, Column column, double value) {
        int[] elements = new int[(int)dataSet.size()];
        Iterator<Data> reader = dataSet.iterator();
        int i = 0;
        while (reader.hasNext()) {
            Data data = reader.next();
            double currentValue = data.getValue(column);
            if (Tools.isLessEqual(currentValue, value))
                elements[i++] = 0;
            else
                elements[i++] = 1;
        }
        Subsets partition = new Subsets(elements, 2);
        return new SplitDataSet(dataSet, partition);
    }
}
