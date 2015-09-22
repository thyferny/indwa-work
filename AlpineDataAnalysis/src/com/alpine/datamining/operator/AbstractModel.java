/**
 * ClassName AbstractColumns.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBDataSet;

/**
 * * @author Eason
 */
public abstract class AbstractModel extends OutputObject implements Model {

    /**
	 * 
	 */
	private static final long serialVersionUID = -681238155782726884L;
	/** This header data set contains all important nominal mappings of all training columns.
	 */
    private DBDataSet headerDataSet;
    
    /** Created a new model which was built on the given data set.*/
    protected AbstractModel(DataSet dataSet) {
        if (dataSet != null)
            this.headerDataSet = new DBDataSet(dataSet);
    }
    
    /** Delivers the training header data set. */
    public DBDataSet getTrainingHeader() {
        return this.headerDataSet;
    }
    
	/** The default implementation returns the result of the super class.  */
	public String getName() {
		String result = super.getName();
		if (result.toLowerCase().endsWith("model")) {
			result = result.substring(0, result.length() - "model".length());
		}
		return result;
	}
}
