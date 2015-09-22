/**
 * ClassName IBuildLeaf
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;



/**
 * This class can be used to change an inner tree node into a leaf.
 * 
 */
public interface IBuildLeaf {

    public void changeToLeaf(Tree node, DataSet dataSet) throws OperatorException;
}
