
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;


public class DepthStop implements Stop {

    private long maxDepth;
    
    public DepthStop(long maxDepth2) {
        this.maxDepth = maxDepth2;
    }
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return depth >= this.maxDepth;
    }    
}
