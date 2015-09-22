/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowItemsReplicationForm.java
 */
package com.alpine.miner.impls.controller.forms;

import java.util.List;

import com.alpine.miner.impls.editworkflow.flow.ConnectionItemInfo;
import com.alpine.miner.impls.editworkflow.flow.OperatorItemInfo;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.operator.OperatorPrimaryInfo;

/**
 * @author Gary
 * Aug 2, 2012
 */
public class FlowItemsReplicationForm {

	private FlowInfo flowInfo;
	private FlowInfo copiedFlowInfo;
	
	private OperatorItemInfo[] copyOperatorSet;
//	private ConnectionItemInfo[] copyConnectionSet;
	
	private List<OperatorPrimaryInfo> operatorPrimaryInfoSet;
	private List<ConnectionItemInfo> connectionInfoSet;

	private int offset;
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public List<OperatorPrimaryInfo> getOperatorPrimaryInfoSet() {
		return operatorPrimaryInfoSet;
	}
	public void setOperatorPrimaryInfoSet(
			List<OperatorPrimaryInfo> operatorPrimaryInfoSet) {
		this.operatorPrimaryInfoSet = operatorPrimaryInfoSet;
	}
	public List<ConnectionItemInfo> getConnectionInfoSet() {
		return connectionInfoSet;
	}
	public void setConnectionInfoSet(
			List<ConnectionItemInfo> connectionInfoSet) {
		this.connectionInfoSet = connectionInfoSet;
	}
	public FlowInfo getFlowInfo() {
		return flowInfo;
	}
	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}
	public FlowInfo getCopiedFlowInfo() {
		return copiedFlowInfo;
	}
	public void setCopiedFlowInfo(FlowInfo copiedFlowInfo) {
		this.copiedFlowInfo = copiedFlowInfo;
	}
	public OperatorItemInfo[] getCopyOperatorSet() {
		return copyOperatorSet;
	}
	public void setCopyOperatorSet(OperatorItemInfo[] copyOperatorSet) {
		this.copyOperatorSet = copyOperatorSet;
	}

}
