/**
 * ClassName AlpineMinerEngine.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticResult;


/**
 * @author John Zhao
 *
 *server is stateful
 *enine is also stateful with a process id,but should not care about security...
 */
public interface AnalyticEngine {
	
	public static AnalyticEngine instance=AlpineAnalyticEngine.getInstance();
 
	/**
	  * step run, only run this node!will auto run the dependency node(parent)
	 * @throws Exception 
	  * */
 
 	
	public  void stopAnalysisProcess(String processID)throws AnalysisException;
	
//	public String runAnalysisProcessFile(String processFilePath, List<AnalyticProcessListener> list ,Locale locale) throws AnalysisException;
	
	public String runAnalysisProcessFile(String processFilePath, List<AnalyticProcessListener> list,boolean isVisual,Locale locale,String executeUser) throws AnalysisException;
	
//	public AnalyticOutPut runSingleNode(AnalyticNode node,List<AnalyticProcessListener> list) throws AnalysisException;

	/**
	 * @param stepRunProcess
	 * @param node
	 * @param createListeners
	 * @throws Exception 
	 */
	public  StepedAnalyticRunner stepRunToNode(AnalyticProcess process,
			AnalyticNode node, List<AnalyticProcessListener> listeners,
			boolean isVisual, Locale locale, AnalyticContext context,
			String executeUser) throws Exception;
	 
	 
}
