package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.datamining.workflow.StepedAnalyticRunner;
import com.alpine.datamining.workflow.util.StepRunHelper;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.reader.AbstractReaderParameters;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class WebWorkFlowStepRunner extends WebWorkFlowRunner {
    private static Logger itsLogger = Logger.getLogger(WebWorkFlowStepRunner.class);
    private StepRunHelper stepRunHelper;
	private List<UIOperatorModel> operatorModelList=null;
	private boolean inStepRun;
	private StepedAnalyticRunner stepRunner;
	public WebWorkFlowStepRunner(String filePath,  RowInfo row,String uuid,  
			String flowFullName, Locale locale, FlowInfo flowInfo){
		super(  filePath,    row,  uuid,  
				  flowFullName,   locale,   flowInfo) ;
		this.stepRunHelper=new StepRunHelper(flowInfo.getKey());
	}
	
	public void setNodeFinished(String nodeName,boolean finish){
		stepRunHelper.clearStepRunResult(nodeName);
 
	}
	
	public void clearStepRunResult(String operatorName){
		stepRunHelper.clearStepRunResult(operatorName);
	}
	

	//filePath is real path ,operatorUUID is the id for the operator user want to run to
	public void stepRunworkFlow(String filePath,String operatorUUID, AnalyticContext context) throws Exception{
		listener = new WebRunAnalyticProcessListener(user,flowName,filePath,true,locale);
		listener.setRow(row);
		listener.setUUID(uuid);
		listener.setFlowFileVersion(flow_file_Version) ;
		listener.setFlowFullName(flowFullName);
		List<AnalyticProcessListener> listeners =new ArrayList<AnalyticProcessListener>();
		listeners.add(listener);
		
		UIOperatorModel runOperatorModel=null;
		
//		if(operatorModelList==null){
			operatorModelList=getAllOperatorModel(filePath);
//		}
					
		for(UIOperatorModel oModel:operatorModelList){
			if(!oModel.getUUID().equals(operatorUUID)){
				continue;
			}else{
				runOperatorModel=oModel;
				break;
			}
		}
		if(runOperatorModel==null){
			return;
		}
//		refreshNodeName(nodeMap);
//		validateOperatorAddorDel(runOperatorModel);
		
		List<UIOperatorModel> parentListWithModel = getParentModelListWithMolde(runOperatorModel);
		
		startStepRunFlow(parentListWithModel,runOperatorModel,listeners,filePath,context);	
	}
	
	private List<UIOperatorModel> getParentModelListWithMolde(UIOperatorModel model) {
		List<UIOperatorModel> paretnList = new ArrayList<UIOperatorModel>();
		addModelList(model, paretnList);
		return paretnList;
	}


	private void addModelList(UIOperatorModel model,
			List<UIOperatorModel> paretnList) {
		paretnList.add(model);
		if (getParentList(model) != null) {
			for (Iterator<UIOperatorModel> iterator = getParentList(model).iterator(); iterator
					.hasNext();) {
				UIOperatorModel parent = iterator.next();
				addModelList(parent, paretnList);
			}
		}
	}
	
	private List<UIOperatorModel> getParentList(UIOperatorModel model){
		List<UIOperatorModel> getParentList=new ArrayList<UIOperatorModel>();
		List<UIConnectionModel>  sourceConnectionList=model.getSourceConnection();
		for(UIConnectionModel connModel:sourceConnectionList){
			getParentList.add(connModel.getSource());
		}	
		return getParentList;
	}
	 
	
	private List<UIOperatorModel> getAllOperatorModel(String filePath){
		List<UIOperatorModel> list=null;
		XMLWorkFlowReader reader=new XMLWorkFlowReader();
		AbstractReaderParameters para=new XMLFileReaderParameters(filePath,user,resourceType);
		try {
			OperatorWorkFlow ow=reader.doRead(para,locale);
			list=ow.getChildList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	private void startStepRunFlow(List<UIOperatorModel> parentListWithModel,
			UIOperatorModel runOperatorModel,
			List<AnalyticProcessListener> listeners, 
			String filePath, AnalyticContext context) throws Exception {
		if (inStepRun == false) {
			inStepRun=true;
		}
		
		//the random folder in order to avoid same sub flow name by manual or scheduler
		//and ensure can be clean the folder immediately when run complete.
		String tempDir=RUNTIME_DIR + File.separator + user + File.separator + UUID.randomUUID();
		String tmpFilePath=tempDir+File.separator+UUID.randomUUID().toString()+flowName+Resources.AFM;
		boolean addSuffixToOutput=Boolean.parseBoolean(ProfileReader.getInstance().getParameter(ProfileUtility.UI_ADD_PREFIX));
		copyFile(filePath,resourceType,user,tmpFilePath,locale,addSuffixToOutput);
		//copySubFlowOperatorsFlows(tempDir);
		listeners.get(0).setFilePath(tmpFilePath);
		final TableMappingModel tableMappingModel =(TableMappingModel)  ParameterUtility.getParameterValue(runOperatorModel.getOperator() ,
				OperatorParameter.NAME_tableMapping);
			
		//inStepRun=stepRunHelper.prepareStepRun(tmpFilePath, runOperatorModel.getUUID(), listeners,false,locale);
	 
		
		 boolean isSubflow = runOperatorModel.getOperator() instanceof SubFlowOperator; 
		if(isSubflow==false){//normal  
		    stepRunHelper.prepareStepRun(tmpFilePath, runOperatorModel.getId(),  locale ,null);
		    // then run to the nodel...
	   }else{
		   //the step run node is a subflow, need special stuffs
		   Object value = ParameterUtility.getParameterByName(runOperatorModel.getOperator(), 
					OperatorParameter.NAME_subflowPath).getValue();
		   if(value!=null){
		   String subFlowName = value.toString() ;
				if(StringUtil.isEmpty(subFlowName)==false){
					//save temp sub file ...
				 
					String subflowPath = tempDir+subFlowName+Resources.AFM;
			   
					stepRunHelper.prepareStepRun4SubflowOperator(tmpFilePath, runOperatorModel.getId(),   locale,subflowPath, tableMappingModel);
				}
			}
	    
	   }
		stepRunner = AlpineAnalyticEngine.getInstance().stepRunToNode(
				stepRunHelper.getSubProcessprocess(), stepRunHelper.getRunNode(), listeners, false,locale,context,user);
		if(stepRunner!=null){
			processID= stepRunner.getProcessID();
		}
		itsLogger.debug("WebWorkFlowRunner.stepRunworkFlow Start process id="+processID);
	}
	
	public void dispose() { 
		if(stepRunner!=null){
			try {
				AlpineAnalyticEngine.getInstance().stopAnalysisProcess(stepRunner.getProcessID());
				stepRunner.getContext().dispose();
				 clearStepRunResult(null) ;

			} catch (AnalysisException e) {
				//nothing to do here
			}

		}
		
	}

	public AnalyticContext getContext() { 
		if(stepRunner!=null){
			return stepRunner.getContext();
		}
		else{
			return null;
		}
	}
}
