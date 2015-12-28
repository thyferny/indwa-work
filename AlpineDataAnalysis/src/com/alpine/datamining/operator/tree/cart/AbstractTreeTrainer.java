
package com.alpine.datamining.operator.tree.cart;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;


public abstract class AbstractTreeTrainer extends Trainer {

    public AbstractTreeTrainer() {
        super();
    }

    public abstract Model train(DataSet eSet) throws OperatorException;


    protected abstract AbstractConstructTree getTB(DataSet dataSet) throws OperatorException;

    protected abstract Standard createStandard(boolean loadData) ;
}
