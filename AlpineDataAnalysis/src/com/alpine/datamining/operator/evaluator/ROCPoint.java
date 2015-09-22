/**
 * ClassName ROCPoint.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;


/**
 * @author Eason
 */
public class ROCPoint {

	private double falsePositives;
	private double truePositives;
	private double confidence;

	public ROCPoint(double falsePositives, double truePositives,
			double confidence) {
		this.falsePositives = falsePositives;
		this.truePositives = truePositives;
		this.confidence = confidence;
	}


	public double getFP() {
		return falsePositives;
	}


	public double getTP() {
		return truePositives;
	}

	public double getConfidence() {
		return confidence;
	}


}
