/**
 * ClassName AnalyzerOutPutARIMARPredict
 *
 * Version information: 1.00
 *
 * Data: 2012-11-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output.hadoop;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.hadoop.predictor.HadoopARIMARPredictResult;

/**
 *
 */
public class HadoopAnalyzerOutPutARIMARPredict extends AbstractAnalyzerOutPut   {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962518604118016881L;
	private HadoopARIMARPredictResult ret;
		

	/**
	 * @param 
	 */
	public HadoopAnalyzerOutPutARIMARPredict(HadoopARIMARPredictResult ret) {
		this.ret=ret;
	}


	public HadoopARIMARPredictResult getRet() {
		return ret;
	}


	public void setRet(HadoopARIMARPredictResult ret) {
		this.ret = ret;
	}
}
