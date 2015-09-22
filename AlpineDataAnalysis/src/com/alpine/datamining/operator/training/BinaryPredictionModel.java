/**
 * ClassName BinaryPredictionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.training;

import com.alpine.datamining.db.DataSet;


public abstract class BinaryPredictionModel extends Prediction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4218999671408512315L;
	protected double threshold = 0.0d;
	
	protected BinaryPredictionModel(DataSet dataSet, double threshold) {
		super(dataSet);
        this.threshold = threshold;
	}
}
