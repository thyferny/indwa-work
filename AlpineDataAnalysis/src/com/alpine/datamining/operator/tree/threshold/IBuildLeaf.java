
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;




public interface IBuildLeaf {

    public void changeToLeaf(Tree node, DataSet dataSet) throws OperatorException;
}
