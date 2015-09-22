/**
* ClassName CriterionSBCLogistic.java
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
public class CriterionSBCLogistic extends CriterionSBC{

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.trainer.stepwise.CriterionIMP#getCriterion(com.alpine.datamining.operator.Model, long, java.lang.String)
	 */
	@Override
	public double getCriterion(Model analyModel, long rowNumber) {
		double SBC=0;
		double LL = ((LogisticRegressionModelDB) analyModel).getModelDeviance();
		double p=((LogisticRegressionModelDB)analyModel).getColumnNames().length;
		SBC=LL + Math.log(rowNumber)*(p+1);
	
		return SBC;
	}
}
