/**
 * ClassName DataAnlyticOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.operator.fpgrowth.AssociationRules;

/**
 * @author John Zhao
 *
 */
public class AnalyzerOutPutAssociationRule extends AbstractAnalyzerOutPut   {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962518604118016881L;
	private AssociationRules rules;
		

	public AssociationRules getRules() {
		return rules;
	}

	/**
	 * @param rules
	 */
	public AnalyzerOutPutAssociationRule(AssociationRules rules) {
		this.rules=rules;
	}

	public String toString(){
		if(rules!=null){
			return rules.toResultString();
		}else{
			return null; 			
		}

	}
}
