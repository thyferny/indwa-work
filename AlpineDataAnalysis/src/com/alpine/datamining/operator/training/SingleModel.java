/**
 * ClassName SingleModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.training;

import com.alpine.datamining.db.DataSet;


public abstract class SingleModel extends Prediction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5671621077841405960L;

//	private double oobEstimatedError = 0;
//	
//	public double getOobEstimatedError() {
//		return oobEstimatedError;
//	}
//	public void setOobEstimatedError(double oobEstimatedError) {
//		this.oobEstimatedError = oobEstimatedError;
//	}
	protected SingleModel(DataSet dataSet) {
		super(dataSet);
	}
	
}
