
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;



public class NoDataStop implements Stop {
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return dataSet.size() == 0;
    }    
}
