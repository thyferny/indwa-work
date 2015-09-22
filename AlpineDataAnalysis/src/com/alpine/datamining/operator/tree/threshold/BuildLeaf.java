/**
 * ClassName BuildLeaf
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.ColumnStats;
import com.alpine.datamining.exception.WrongUsedException;

/**
 * This class can be used to change an inner tree node into a leaf.
 * 
 */
public class BuildLeaf
	implements IBuildLeaf
{

    public void changeToLeaf(Tree node, DataSet dataSet) throws WrongUsedException {
        Column label = dataSet.getColumns().getLabel();
        dataSet.computeColumnStatistics(label);
        int labelValue = (int)dataSet.getStatistics(label, ColumnStats.MODE); 
        String labelName = label.getMapping().mapIndex(labelValue);
        node.setLabel(labelName);
        for (String value : label.getMapping().getValues()) {
            int count = (int)dataSet.getStatistics(label, ColumnStats.COUNT, value);
            node.addCount(value, count);
        }
    }
}
