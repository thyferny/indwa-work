package com.alpine.datamining.operator.fpgrowth;

import com.alpine.datamining.operator.Parameter;

public class AssociationRuleGeneratorParameter implements Parameter {
	private double minConfidence = 0.8;

	public double getMinConfidence() {
		return minConfidence;
	}

	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}
}
