/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * HadoopOperatorInfo.java
 */
package com.alpine.miner.impls.web.resource.operator;

/**
 * @author Gary
 * Jul 9, 2012
 */
public class HadoopOperatorPrimaryInfo extends OperatorPrimaryInfo {

	private String outputHadoopFilePath;
	private boolean storeResult;

	public String getOutputHadoopFilePath() {
		return outputHadoopFilePath;
	}

	public void setOutputHadoopFilePath(String outputHadoopFilePath) {
		this.outputHadoopFilePath = outputHadoopFilePath;
	}

	public boolean isStoreResult() {
		return storeResult;
	}

	public void setStoreResult(boolean storeResult) {
		this.storeResult = storeResult;
	}
}
