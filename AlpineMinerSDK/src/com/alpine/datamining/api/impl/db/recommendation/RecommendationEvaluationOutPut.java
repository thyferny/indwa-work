/**
 * ClassName  RecommendationEvaluationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: March 12, 2011
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.recommendation;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
/**
 * @author Eason
 *
 */
public class RecommendationEvaluationOutPut extends AbstractAnalyzerOutPut{
	private static final long serialVersionUID = -3284373730968458231L;
	private Double[] result;

	public RecommendationEvaluationOutPut(Double[] result) {
		this.result=result;
	}
	public Double[] getResult() {
		return result;
	}

	public void setResult(Double[] result) {
		this.result = result;
	}
}
