/**
 * ClassName Pruner.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.exception.OperatorException;

/**
 * The pruner for trees.
 * 
 */
public interface Prune {

    public void prune(Tree node) throws OperatorException;
    
}
