/**
 * ClassName SortedDataIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;


/**
 * This SortedDataIterator reader 
 * @author Eason
 */
public class SortedDataIterator extends AbstractDataIterator {

	/** 
	 * The parent data set. 
	 */
	private DataSet parent;

    /** 
     * The current index in the mapping. 
     */
    private int currentIndex;
    
    /** 
     * Indicates if the current data was &quot;delivered&quot; by a call of {@link #next()}. 
     */
    private boolean nextInvoked = true;
    
    /** 
     * The data that will be returned by the next call of next(). 
     */
    private Data currentData = null;
    
    /** 
     * Constructs a new mapped data reader. 
     */
    public SortedDataIterator(DataSet parent) {
        this.parent = parent;
        this.currentIndex = 0;
    }

    public boolean hasNext() {
        if (this.nextInvoked) {
            this.nextInvoked = false;
        	if (this.currentIndex < parent.size()) {
            	this.currentData = this.parent.getRow(this.currentIndex);
            	this.currentIndex++;
        	} else {
        		return false;
        	}
        }
        return true;
    }

    public Data next() {
        if (hasNext()) {
            this.nextInvoked = true;
            return currentData;
        } else {
            return null;
        }
    }
}

