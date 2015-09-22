/**
 * ClassName NoColumnStop
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
 * Stop if the data set does not have any regular columns.
 * 
 */
public class NoColumnStop implements Stop {

    public NoColumnStop() {}
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return (dataSet.getColumns().size() == 0);        
    }    
}
