/**
 * ClassName MinerEngineListener.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.Locale;

/**
 * @author John Zhao
 *this is for UI interaction to mornitoring the process running
 */
public interface AnalyticProcessListener {
	
 
	//for interactionMode
	// one node is oK now,means one node analysis has been finished
	public void finishAnalyzerNode(String nodeName,AnalyticOutPut outPut);
	public void startAnalyzerNode(String nodeName);
	//tell the listener that the process he is monitoring is finished now
	public void analyticFlowFinished(AnalyticResult result);
	/**
	 * 
	 */
	public void stopProcess(AnalyticResult result);
	//error will stop process
	public void processError(String errMessage);
	public void processError(Throwable error);
	/**
	 * @param message
	 * @param nodeName
	 */
	void putMessage(String message, String nodeName);
	public String getFilePath();
	public void setFilePath(String filePath);
	
	public void setLocale(Locale locale);
	public Locale getLocale();
	public String getFlowRunUUID() ;

}
