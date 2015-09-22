package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticResult;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import com.alpine.utility.log.LogPoster;
/**
 * ClassName:WebRunAnalyticProcessListener
 * 
 * Author  john zhao
 * 
 * Version Ver 1.0
 * 
 * Date 2011-3-29
 * 
 * COPYRIGHT 2010-2011 Alpine Solutions. All Rights Reserved.
 */
public class WebRunAnalyticProcessListener extends AbstractAnalyticProcessListener{
	 
	private RowInfo resultInfo;
	private String editingFlowName;//include path and file name
 
	private boolean stepRun = false;
	 

	public WebRunAnalyticProcessListener(String runUser,String flownName,String editingFlowName, Locale locale){
		this(runUser, flownName, editingFlowName, false, locale);
	}
	
	public WebRunAnalyticProcessListener(String runUser,String flownName,String editingFlowName, boolean stepRun, Locale locale){
		super(runUser, flownName, locale) ;
		this.stepRun = stepRun;
		this.editingFlowName = editingFlowName;
	}
	
	@Override
	public void finishAnalyzerNode(String nodeName, AnalyticOutPut outPut) {
		OutPutVisualAdapterFactory visualFactory=OutPutVisualAdapterFactory.getInstance();
		if(isStop()){ 
			return;
		}
		String jsonString="";
		//if ==null, means some operator has no output (like table selection)
		 	
		try {
				if(stepRun==true){
					//get it from hashmap
					if(visualFactory.getTempModelString(getRunUser(), outPut)!=null){
						jsonString = visualFactory.getTempModelString(getRunUser(), outPut);
					}else{
						//first time to run
						jsonString = generateVisualString(outPut, visualFactory );
						visualFactory.addTempVModelString(getRunUser(), outPut, jsonString);
					}
					resultJSONList.add(jsonString);
					String message = VisualNLS.getMessage(VisualNLS.OPERATOR_FINISHED, locale);
					if(outPut.getExtraLogMessage()!=null){
						message = message+" (" + outPut.getExtraLogMessage() +")" ;
					}
					showLog(nodeName,Resources.OPERATOR_FINISHED,null,jsonString,message);
				}else{
					super.finishAnalyzerNode(nodeName, outPut);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "finishAnalyzerNode error:"+e.getMessage());
				processError(e);
				return;
				
			}
 
	}
 
	 
  
	@Override
	public void analyticFlowFinished(AnalyticResult result) {
		super.analyticFlowFinished(result) ;
	 
	}
 

	@Override
	public void stopProcess(AnalyticResult result) {
		super.stopProcess(result);
		
	}

	  
	
	public void setRow(RowInfo row) {
		this.resultInfo = row;
	}
	
	protected void showLog(String nodeName, String message,String errMessage,String output,String logmessage) {
		errMessage = formatErrorMsg(errMessage);
		String[] item = createLogItem(nodeName, message, errMessage, output,logmessage);
	 
		//avoid some one update the row at the same time...
		synchronized (resultInfo.getRowList()) {
			resultInfo.addRow(item);	
		}
		super.showLog(nodeName, message, errMessage, output,logmessage);
	 
 	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener#saveModelToEditFlow()
	 */
	@Override
	protected void saveModelToEditFlow() throws Exception {
		//save the flow make sure the model is saved
		OperatorWorkFlow workFlow = getWorkFlowData(this.getTempFileName(), locale);
		XMLWorkFlowSaver workflowSaver=new XMLWorkFlowSaver();
		workflowSaver.doSave(editingFlowName, workFlow,super.getRunUser(),false) ;
		

		//read the native flow, the orinal one... I think don't need following codes, them look like just read out the workflow content and store into itself.
//		OperatorWorkFlow workFlow =getWorkFlowData(persistence.generateResourceKey(flowInfo)+".afm",locale);
//		workflowSaver.doSave(persistence.generateResourceKey(flowInfo) + ".afm", workFlow,runUser,false) ;
 		
	}
	
	private OperatorWorkFlow getWorkFlowData(String fileName,
			Locale locale ) throws Exception {
		
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
		XMLFileReaderParameters params = null;
		 
			if (fileName != null && fileName.length() > 0) {
				File f = new File(fileName);
				if (f.exists() == true) {
					params = new XMLFileReaderParameters(fileName,
							super.getRunUser(), ResourceType.Personal);
					OperatorWorkFlow flow = reader.doRead(params,locale);
					return flow;
				}
				
			}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener#returnExecuteType()
	 */
	@Override
	protected String returnExecuteType() {
		return LogPoster.WORKFLOW_RUN_MANUAL;
	}
}
