/**
 * ClassName Model.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;

/**
 * 
 * @author Eason
 */
public interface Model extends ConsumerProducer{


	public DataSet apply(DataSet testSet) throws OperatorException;
	
}
