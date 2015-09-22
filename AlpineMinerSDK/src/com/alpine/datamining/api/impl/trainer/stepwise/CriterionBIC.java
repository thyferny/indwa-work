/**
* ClassName CriterionBIC.java
*
* Version information: 1.00
*
* Data: 14 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.trainer.stepwise;

import com.alpine.datamining.operator.Model;

/**
 * @author Shawn
 *
 */
public  abstract class CriterionBIC extends CriterionIMP{
	public  static final String criterionType="BIC";

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.trainer.stepwise.CriterionIMP#getCriterion(com.alpine.datamining.operator.Model, long)
	 */
	@Override
	public abstract double getCriterion(Model analyModel, long rowNumber);
	


}
