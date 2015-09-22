/**
 * ClassName SVMRegressionParameter.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

/**
 * 
 * @author Eason
 *
 */
public class SVMRegressionParameter extends SVMParameter {
	private double slambda = 0.2;

	public double getSlambda() {
		return slambda;
	}

	public void setSlambda(double slambda) {
		this.slambda = slambda;
	}
}
