
package com.alpine.datamining.operator.tree.cartregression;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.ColumnStats;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.tree.threshold.IBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.Tree;


public class BuildLeaf
	implements IBuildLeaf
{

    public void changeToLeaf(Tree node, DataSet dataSet) throws WrongUsedException {
        Column label = dataSet.getColumns().getLabel();
        dataSet.computeColumnStatistics(label);
        ((RegressionTree)node).setAvg(dataSet.getStatistics(label, ColumnStats.AVERAGE));
        ((RegressionTree)node).setDeviance(dataSet.getStatistics(label, ColumnStats.VARIANCE));
        ((RegressionTree)node).setCount((int)dataSet.getStatistics(label, ColumnStats.COUNT));
    }
}
