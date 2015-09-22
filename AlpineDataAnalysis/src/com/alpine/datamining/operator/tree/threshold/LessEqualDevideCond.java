/**
 * ClassName LessEqualDevideCond
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
 * A split condition for numerical values (less equals).
 * 
 */
public class LessEqualDevideCond extends AbstractDevideCond {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -4550957191601520532L;
	private double value;
    
    public LessEqualDevideCond(Column column, double value) {
        super(column.getName());
        this.value = value;
    }
    
//    public boolean test(Data data) {
//        return data.getValue(data.getcolumns().get(getcolumnName())) <= value;
//    }

	public String getRelation() {
		return "<=";
	}

	public String getValueString() {
		return String.valueOf(value);

	}
	public String getReadableValueString()
	{
		return String.valueOf(this.value);
	}
}
