/**
 * ClassName  StepedAnalyticRunner.java
 *
 * Version information: 1.00
 *
 * Data: 2010-7-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.ModelWrapperConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.CopytoHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.datamining.api.impl.hadoop.kmeans.HadoopKmeansAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;

/**
 * @author John Zhao
 *
 */
public class StepedAnalyticRunner extends AnalyticRunner {


	List<AnalyticNode> executedNodeList=new ArrayList<AnalyticNode> ();
	private String processID;
	
	/**
	 * @param process
	 * @param listeners
	 * @param locale 
	 * @throws Exception 
	 */
	public StepedAnalyticRunner(AnalyticProcess process,
			List<AnalyticProcessListener> listeners, boolean isVisual, Locale locale, AnalyticContext context,String executeUser) throws Exception {
		super(process, listeners, isVisual, locale,context,executeUser);
 
	}

 
	
	/**
	 * @param analyticNode
	 * @param listener 
	 * @return
	 * @throws Exception
	 */
	protected void doAnalysis(AnalyticProcess process, AnalyticNode analyticNode,
			List<AnalyticProcessListener> listeners) throws Exception {

		if(executedNodeList.contains(analyticNode)==true|| stop==true)
			return;

		List<AnalyticNode> parentNodes = analyticNode.getParentNodes();
		// make sure the parenet is OK
		for (Iterator iterator = parentNodes.iterator(); iterator.hasNext();) {
			AnalyticNode parentNode = (AnalyticNode) iterator.next();
			if (parentNode != null) {
				if (executedNodeList.contains(analyticNode)== false) {
					doAnalysis(process, parentNode, listeners);
				}
			}
		}
		//finished in last time
		
		notifyNodeStart(analyticNode, listeners);
		AnalyticOutPut outPut=null;
		if(analyticNode.isFinished()&&analyticNode.getOutput()!=null
				//&&analyticNode.getOutput().getDataAnalyzer() instanceof HadoopFileSelector ==false
				){
			notifyMessage(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Result_already_exists,locale),analyticNode.getName(),listeners);
			outPut=analyticNode.getOutput();
			 outPut.getDataAnalyzer().setContext(getContext()) ;
			outPuts.put(analyticNode.getName(), outPut);
			analyzerList.add( outPut.getDataAnalyzer());
			 AnalyticSource source = analyticNode.getSource();
			  if(outPut.getDataAnalyzer() instanceof AbstractHadoopAttributeAnalyzer
					  ||outPut.getDataAnalyzer() instanceof  HadoopFileSelector
					  ||outPut.getDataAnalyzer() instanceof CopytoHadoopAnalyzer
					  ||outPut.getDataAnalyzer() instanceof HadoopKmeansAnalyzer
					  ){
				 //only run scirpt and notsave result
				 
				//  executeNode(analyticNode, source,false);
				  setChildResultNameForHadoop(analyticNode,  source, outPut.getDataAnalyzer());
				 
			}
			  //else  if(outPut.getDataAnalyzer() instanceof CopytoHadoopAnalyzer){
			//	((CopytoHadoopAnalyzer)outPut.getDataAnalyzer() ).loadFileToPig((DataBaseAnalyticSource)analyticNode.getSource());
			//	setChildResultNameForHadoop(analyticNode,  source, outPut.getDataAnalyzer());
			//}
			
		}
		else{
			AnalyticSource source = analyticNode.getSource();
			//enhance the model wrapper. this is important
			if(source.getAnalyticConfig() instanceof ModelWrapperConfig){
				if(analyticNode.getParentNodes().size()>0){
					ModelWrapperConfig config=(ModelWrapperConfig)source.getAnalyticConfig();
					AnalyticNode parentNode = analyticNode.getParentNodes().get(0);
					EngineModel trainedModel = ((AnalyzerOutPutTrainModel)parentNode.getOutput()).getEngineModel();
					config.setTrainedModel(trainedModel);
				}
			}
 
		
			 
			outPut = executeNode(analyticNode, source,true);
		
		}
		analyticNode.setOutput(outPut);
		executedNodeList.add(analyticNode);
		notifyNodeFinished(analyticNode, listeners, outPut);
		

	}


	/**
	 * @param string
	 */
	private void notifyMessage(String msg,String nodeName,List<AnalyticProcessListener> listeners) {
		if(listeners!=null){
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				AnalyticProcessListener listener = (AnalyticProcessListener) iterator
						.next();
				listener.putMessage(msg,nodeName);
			 
			}
		}
		
	}

	@Override
	protected void releaseContext() {
		//keep the context for pig,so only kill the map/reduce job
		 getContext().cancleHadoopJob();
	}


	public void setProcessID(String processID) {
		this.processID = processID;
		
	}


	public String getProcessID() {
		return processID;
	}
	
}
