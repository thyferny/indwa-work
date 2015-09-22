/**
 * ClassName Standard
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;



public interface Standard {

    public double getNominalBenefit(DataSet dataSet, Column column) throws OperatorException;
    
    public double getNumericalBenefit(DataSet dataSet, Column column, double splitValue) throws OperatorException;
    
    
    public boolean supportsIncrementalCalculation();
    
    public void startIncrementalCalculation(DataSet dataSet, ICount calculator);
    
    public void swapData(Data data);
    
	public double getIncrementalScore();



}
