/**
 * ClassName DepthStop
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
 * Stop if a maximal depth is reached.
 * 
 */
public class DepthStop implements Stop {

    private long maxDepth;
    
    public DepthStop(long maxDepth2) {
        this.maxDepth = maxDepth2;
    }
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return depth >= this.maxDepth;
    }    
}
