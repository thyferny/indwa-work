/**
 * ClassName NaiveBayesParameter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.bayes;

import com.alpine.datamining.operator.Parameter;

public class NaiveBayesParameter implements Parameter {
	private boolean caculateDeviance = false;
	public boolean isCaculateDeviance() {
		return caculateDeviance;
	}
	public void setCaculateDeviance(boolean caculateDeviance) {
		this.caculateDeviance = caculateDeviance;
	}
}
