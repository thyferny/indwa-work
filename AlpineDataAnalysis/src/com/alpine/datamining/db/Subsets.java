/**
 * ClassName Partition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



/**
 * Implements a partition. A partition is used to divide an data set into
 * different parts of arbitrary sizes without actually make a copy of the data.
 * @author Eason
 */
public class Subsets implements Cloneable, Serializable {


	private static final long serialVersionUID = 2160163403700370848L;

	/** 
	 * Mask for the selected partitions. 
	 */
	private boolean[] mask;

	/** 
	 * Size of the individual partitions. 
	 */
	private int[] partitionSizes;

	/** 
	 * Maps every data to its partition index. 
	 */
	private int[] elements;

	/** 
	 * Indicates the position of the last element for each partition. 
	 */
	private int[] lastElementIndex;
	
	/**
	 * Maps every data index to the true index of the data row in the data
	 * table.
	 */
	private int[] tableIndexMap = null;


	/** 
	 * Creates a partition from the given one. Partition numbering starts at 0. 
	 */
	public Subsets(int[] elements, int numberOfPartitions) {
		init(elements, numberOfPartitions);
	}

	/** 
	 * Clone constructor. 
	 */
	private Subsets(Subsets p) {
		this.partitionSizes = new int[p.partitionSizes.length];
		System.arraycopy(p.partitionSizes, 0, this.partitionSizes, 0, p.partitionSizes.length);
		
		this.mask = new boolean[p.mask.length];
		System.arraycopy(p.mask, 0, this.mask, 0, p.mask.length);
		
		this.elements = new int[p.elements.length];
		System.arraycopy(p.elements, 0, this.elements, 0, p.elements.length);

		this.lastElementIndex = new int[p.lastElementIndex.length];
		System.arraycopy(p.lastElementIndex, 0, this.lastElementIndex, 0, p.lastElementIndex.length);
		
		recalculateTableIndices();
	}

	/**
	 * Private initialization method used by constructors.
	 * @param newElements
	 * @param noOfPartitions
	 */
	private void init(int[] newElements, int noOfPartitions) {
		partitionSizes = new int[noOfPartitions];
		lastElementIndex = new int[noOfPartitions];
		elements = newElements;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] >= 0) {
				partitionSizes[elements[i]]++;
				lastElementIndex[elements[i]] = i;
			}
		}

		// select all partitions
		mask = new boolean[noOfPartitions];
		for (int i = 0; i < mask.length; i++)
			mask[i] = true;
		
		recalculateTableIndices();
	}

    public boolean equals(Object o) {
        if (!(o instanceof Subsets))
            return false;
        
        Subsets other = (Subsets)o;
        
        for (int i = 0; i < mask.length; i++)
            if (this.mask[i] != other.mask[i])
                return false;

        for (int i = 0; i < elements.length; i++)
            if (this.elements[i] != other.elements[i])
                return false;
                
        return true;
    }

    public int hashCode() {
        int hc = 17;
        int hashMultiplier = 59;
        
        hc = hc * hashMultiplier + this.mask.length;
        for (int i = 1; i < mask.length; i <<= 1) {
           hc = hc * hashMultiplier + Boolean.valueOf(this.mask[i]).hashCode();
        }
        
        hc = hc * hashMultiplier + this.elements.length;
        for (int i = 1; i < elements.length; i <<= 1) {
           hc = hc * hashMultiplier + Integer.valueOf(this.elements[i]).hashCode();
        }
        
        return hc; 
    }
    
    /** 
     * Returns true if the last possible index stored in lastElementIndex for all currently selected partitions is
     *  not yet reached. 
     */
    public boolean hasNext(int index) {
    	for (int p = 0; p < mask.length; p++) {
    		if (mask[p]) {
    			if (index <= lastElementIndex[p]) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
	/** 
	 * Clears the selection, i.e. deselects all subsets. 
	 */
	public void clearSelection() {
		this.mask = new boolean[mask.length];
		recalculateTableIndices();
	}



	/** 
	 * Marks the given subset as selected. 
	 */
	public void selectSubset(int i) {
		this.mask[i] = true;
		recalculateTableIndices();
	}


	/**
	 *  Returns the number of subsets. 
	 */
	public int getNumberOfSubsets() {
		return partitionSizes.length;
	}

	/** 
	 * Returns the number of selected elements. 
	 */
	public int getSelectionSize() {
		int s = 0;
		for (int i = 0; i < partitionSizes.length; i++)
			if (mask[i])
				s += partitionSizes[i];
		return s;
	}

	/** 
	 * Returns the total number of data. 
	 */
	public int getTotalSize() {
		return elements.length;
	}

	/**
	 * Returns true if the data with the given index is selected 
	 */
	public boolean isSelected(int index) {
		return mask[elements[index]];
	}

	/**
	 * Recalculates the data table indices of the currently selected
	 * data.
	 */
	private void recalculateTableIndices() {
		List<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < elements.length; i++) {
			if (mask[elements[i]]) {
				indices.add(i);
			}
		}
		tableIndexMap = new int[indices.size()];
		Iterator<Integer> i = indices.iterator();
		int counter = 0;
		while (i.hasNext()) {
			tableIndexMap[counter++] = i.next();
		}
	}

	/**
	 * Returns the actual data table index of the i-th data of the
	 * currently selected subset.
	 */
	public int mapIndex(int index) {
		return tableIndexMap[index];
	}

	public String toString() {
		StringBuffer str = new StringBuffer("(");
		for (int i = 0; i < partitionSizes.length; i++)
			str.append((i != 0 ? "/" : "") + partitionSizes[i]);
		str.append(")");
		return str.toString();
	}

	public Object clone() {
		return new Subsets(this);
	}
}
