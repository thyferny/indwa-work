/**
* ClassName CriterionAICLinear.java
*
* Version information: 1.00
*
* Data: 25 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.trainer.stepwise;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.resources.AlpineThreadLocal;

/**
 * @author Shawn
 *
 */
public  class CriterionAICLinear extends CriterionAIC{

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.trainer.stepwise.CriterionAIC#getCriterion(com.alpine.datamining.operator.Model, long)
	 */
	@Override
	public double getCriterion(Model analyModel, long rowNumber) throws AnalysisException {
		double AIC = 0;
		double s = ((LinearRegressionModelDB) analyModel).getS();
		if(s==Double.NaN  )
		{
			String e = SDKLanguagePack.getMessage(SDKLanguagePack.STEPWISE_LINEAR_SQUARE,AlpineThreadLocal.getLocale());
			
			throw new AnalysisException(e);
		}
		double Q = Math.pow(s, 2)
				* (rowNumber
						- ((LinearRegressionModelDB) analyModel)
								.getColumnNames().length - 1);
		AIC = rowNumber
				* Math.log(Q / rowNumber)
				+ 2
				* (((LinearRegressionModelDB) analyModel)
						.getColumnNames().length + 1);

		return AIC;
	}

}
