/**
 * ClassName GreaterDevideCond
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;

/**
 * Returns true if the value of the desired column is greater then a given threshold.
 */
public class GreaterDevideCond extends AbstractDevideCond {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5048656184753767009L;
	private double value;
    
    public GreaterDevideCond(Column column, double value) {
        super(column.getName());
        this.value = value;
    }
    
//    public boolean test(Data data) {
//        return data.getValue(data.getcolumns().get(getcolumnName())) > value;
//    }
    
	public String getRelation() {
		return ">";
	}

	public String getValueString() {
		return String.valueOf(value);
	}
	public String getReadableValueString()
	{
		return String.valueOf(this.value);
	}
}
