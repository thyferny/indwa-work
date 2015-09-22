/**
 * ClassName DevideCond
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import java.io.Serializable;

/**
 * A condition for a split in decision tree, rules etc. Subclasses should
 * also implement a toString method.
 *  
 */
public interface DevideCond extends Serializable {

	public String getColumnName();
	
	public String getRelation();
	
	public String getValueString();    
	public String getReadableValueString();

}
