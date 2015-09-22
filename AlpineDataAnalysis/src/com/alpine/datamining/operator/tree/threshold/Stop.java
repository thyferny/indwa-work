/**
 * ClassName Stop
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;


/**
 * This interface are used in order to determine
 * if a splitting procedure should be stopped.
 * 
 */
public interface Stop {

    public boolean shouldStop(DataSet dataSet, int depth) throws OperatorException;
    
}
