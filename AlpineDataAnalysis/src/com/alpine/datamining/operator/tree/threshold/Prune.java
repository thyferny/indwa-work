
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.exception.OperatorException;


public interface Prune {

    public void prune(Tree node) throws OperatorException;
    
}
