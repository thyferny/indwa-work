/**
 * ClassName AnalyzerOutPutARIMARPredict
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;

/**
 *
 */
public class AnalyzerOutPutARIMARPredict extends AbstractAnalyzerOutPut   {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962518604118016881L;
	private ARIMARPredictResult ret;
		

	/**
	 * @param 
	 */
	public AnalyzerOutPutARIMARPredict(ARIMARPredictResult ret) {
		this.ret=ret;
	}


	public ARIMARPredictResult getRet() {
		return ret;
	}


	public void setRet(ARIMARPredictResult ret) {
		this.ret = ret;
	}
}
