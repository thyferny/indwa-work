/**
 * ClassName KNNPredictConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

/**
 * 
 * @author Eason
 *
 */
public class KNNPredictConfig  extends HadoopPredictorConfig{

	private String k = "1";
	public KNNPredictConfig() {
	}
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}
}
