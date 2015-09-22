/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowDraftManager.java
 */
package com.alpine.miner.impls.editworkflow.flow;

import java.util.HashMap;
import java.util.Map;

import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;

/**
 * @author Gary
 * Oct 23, 2012
 */
public class FlowDraftManager {

	private static final FlowDraftManager INSTANCE = new FlowDraftManager();
	
	/**
	 * key is login name
	 * value is flow draft
	 */
	private Map<String, DraftInfo> flowDraftPool = new HashMap<String, DraftInfo>();
	
	public static FlowDraftManager getInstance(){
		return INSTANCE;
	}
	
	public void pushDraft(String loginName, FlowInfo flow, OperatorWorkFlow flowContent){
		DraftInfo draftInfo = flowDraftPool.get(loginName);
		if(draftInfo == null){
			draftInfo = new DraftInfo(flow, flowContent); 
			flowDraftPool.put(loginName, draftInfo);
		}else{
			draftInfo.updateWorkFlow(flow, flowContent);
		}
	}
	
	public void clearDraft(String loginName){
		flowDraftPool.remove(loginName);
	}
	
	public DraftInfo getDraft(String loginName){
		return flowDraftPool.get(loginName);
	}
	
	public boolean hasDraftFlow(String loginName){
		return flowDraftPool.containsKey(loginName);
	}
	
	public static final class DraftInfo{
		private FlowInfo flowInfo;
		private OperatorWorkFlow workFlow;
		
		private DraftInfo(FlowInfo flowInfo, OperatorWorkFlow workFlow) {
			this.updateWorkFlow(flowInfo, workFlow);
		}
		
		void updateWorkFlow(FlowInfo flowInfo, OperatorWorkFlow workFlow){
			this.flowInfo = flowInfo;
			this.workFlow = workFlow;
		}

		public FlowInfo getFlowInfo() {
			return flowInfo;
		}

		public OperatorWorkFlow getWorkFlow() {
			return workFlow;
		}
	}
}
