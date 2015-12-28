
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;



public interface Stop {

    public boolean shouldStop(DataSet dataSet, int depth) throws OperatorException;
    
}
