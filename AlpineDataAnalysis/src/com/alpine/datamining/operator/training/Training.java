/**
 * ClassName Training.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.training;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;


public interface Training {


	public Model train(DataSet dataSet) throws OperatorException;

	public String getName();

	public boolean shouldEstimatePerformance();

	public boolean shouldCalculateWeights();

}
