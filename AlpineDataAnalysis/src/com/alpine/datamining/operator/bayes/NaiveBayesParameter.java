
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
