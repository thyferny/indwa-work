/**
 * ClassName AbstractSVM
 *
 * Version information: 1.00
 *
 * Data: Apr 18, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import com.alpine.datamining.operator.training.Trainer;

public abstract class AbstractSVM extends Trainer {
	public static String[] kernelTypeArray={"dot product","polynomial","gaussian"};
	protected SVMParameter para;
}
