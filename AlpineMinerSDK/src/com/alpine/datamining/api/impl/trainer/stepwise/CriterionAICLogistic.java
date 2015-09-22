/**
* ClassName CriterionAICLogistic.java
*
* Version information: 1.00
*
* Data: 25 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.trainer.stepwise;

import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;

/**
 * @author Shawn
 *
 */
public class CriterionAICLogistic extends CriterionAIC{

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.trainer.stepwise.CriterionAIC#getCriterion(com.alpine.datamining.operator.Model, long)
	 */
	@Override
	public double getCriterion(Model analyModel, long rowNumber) {
		double AIC = 0;
		double LL = ((LogisticRegressionModelDB) analyModel).getModelDeviance();
		AIC = LL
		+ 2
		* (((LogisticRegressionModelDB) analyModel)
				.getColumnNames().length + 1);
	
		return AIC;
		
	}
	


}
