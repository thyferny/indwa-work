/**
 * ClassName LinearRegressionDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cart;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;

/**
 * This is the abstract super class for all cart tree learners for database. 
 */
public abstract class AbstractTreeTrainer extends Trainer {

    public AbstractTreeTrainer() {
        super();
    }

    public abstract Model train(DataSet eSet) throws OperatorException;


    protected abstract AbstractConstructTree getTB(DataSet dataSet) throws OperatorException;

    protected abstract Standard createStandard(boolean loadData) ;
}
