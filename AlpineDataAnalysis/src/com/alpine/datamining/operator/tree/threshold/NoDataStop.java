/**
 * ClassName NoDataStop
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;


/**
 * Splitting should be terminated if the data set is empty.
 * 
 */
public class NoDataStop implements Stop {
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return dataSet.size() == 0;
    }    
}
