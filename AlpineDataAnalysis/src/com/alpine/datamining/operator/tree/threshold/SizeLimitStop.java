
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;



public class SizeLimitStop implements Stop {

    private int minSize;
    
    public SizeLimitStop(int minSize) {
        this.minSize = minSize;
    }
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return (dataSet.size() < this.minSize);
    }    
}
