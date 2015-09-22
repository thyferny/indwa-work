/**
 * ClassName  EvaluatorResultObjectAdapter.java
 *
 * Version information: 1.00
 *
 * Data: Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;

import com.alpine.datamining.operator.OutputObject;

/**
 * @author Eason
 *
 */
public class EvaluatorResultObjectAdapter extends OutputObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2534723590675588914L;
	private String sourceName;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
	

}
