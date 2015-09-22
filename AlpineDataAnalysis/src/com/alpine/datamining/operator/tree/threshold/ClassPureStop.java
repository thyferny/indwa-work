/**
 * ClassName ClassPureStop
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
 * This ClassPureStop terminates if only one single label is left. 
 * 
 */
public class ClassPureStop implements Stop {

    public ClassPureStop() {}
      
    public boolean shouldStop(DataSet dataSet, int depth) throws WrongUsedException {
        Column label = dataSet.getColumns().getLabel();
        dataSet.computeColumnStatistics(label);
        return dataSet.size() == dataSet.getStatistics(label, ColumnStats.COUNT, label.getMapping().mapIndex((int)dataSet.getStatistics(label, ColumnStats.MODE)));
    }    
}
