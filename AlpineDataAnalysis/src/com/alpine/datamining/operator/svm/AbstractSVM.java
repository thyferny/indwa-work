
package com.alpine.datamining.operator.svm;

import com.alpine.datamining.operator.training.Trainer;

public abstract class AbstractSVM extends Trainer {
	public static String[] kernelTypeArray={"dot product","polynomial","gaussian"};
	protected SVMParameter para;
}
