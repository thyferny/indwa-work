/**
 * ClassName SizeLimitStop
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
 * Terminates if the data set has less than minSize data.
 */
public class SizeLimitStop implements Stop {

    private int minSize;
    
    public SizeLimitStop(int minSize) {
        this.minSize = minSize;
    }
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return (dataSet.size() < this.minSize);
    }    
}
