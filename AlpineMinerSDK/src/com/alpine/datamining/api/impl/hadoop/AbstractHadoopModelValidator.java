/**
 * ClassName AbstractHadoopModelValidator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-10
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import com.alpine.datamining.api.impl.hadoop.evaluator.EvaluatorAdapter;


/***
 * 
 * @author Peter
 * 
 */

public abstract class AbstractHadoopModelValidator extends AbstractHadoopMRJobAnalyzer {
	protected EvaluatorAdapter adpter;
	public void stop(){
		adpter.stop();
	}
}
