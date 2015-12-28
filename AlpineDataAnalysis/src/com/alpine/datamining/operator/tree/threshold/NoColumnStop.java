
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;



public class NoColumnStop implements Stop {

    public NoColumnStop() {}
    
    public boolean shouldStop(DataSet dataSet, int depth) {
        return (dataSet.getColumns().size() == 0);        
    }    
}
