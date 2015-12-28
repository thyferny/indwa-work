
package com.alpine.datamining.operator.svm;


public class SVMRegressionParameter extends SVMParameter {
	private double slambda = 0.2;

	public double getSlambda() {
		return slambda;
	}

	public void setSlambda(double slambda) {
		this.slambda = slambda;
	}
}
