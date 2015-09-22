/**
 * ClassName EvaluatorAdapter.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.operator.evaluator.DoubleListAndDoubleData;
import com.alpine.datamining.operator.evaluator.DoubleListData;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;

public interface EvaluatorAdapter {

	public DoubleListAndDoubleData generateROCData(EngineModel eModel,
			AnalyticSource source) throws Exception ;

	public DoubleListData generateLiftData(EngineModel eModel,
			AnalyticSource source) throws Exception;
	
	public GoodnessOfFit generateGoFData(EngineModel eModel,
			AnalyticSource source) throws Exception;
	
	public boolean isLocalMode();
	
	public void setLocalMode(boolean localMode);
 
	public void stop(); 
	
}
