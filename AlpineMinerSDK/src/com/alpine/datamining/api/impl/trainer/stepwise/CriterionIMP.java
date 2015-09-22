/**
* ClassName CriterionIMP.java
*
* Version information: 1.00
*
* Data: 12 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.trainer.stepwise;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.operator.Model;

/**
 * @author Shawn
 *
 */
public abstract class CriterionIMP {
	public String criterionType;
	public abstract double getCriterion(Model analyModel,long rowNumber) throws AnalysisException;
	
	
	public String getCriterionType() {
		return criterionType;
	}
	public void setCriterionType(String criterionType) {
		this.criterionType = criterionType;
	};
}
