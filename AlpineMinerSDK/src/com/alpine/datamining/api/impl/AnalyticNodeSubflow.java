package com.alpine.datamining.api.impl;

import java.util.HashMap;
import java.util.List;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.impl.algoconf.SubFlowConfig;

public class AnalyticNodeSubflow extends AnalyticNodeImpl{
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 312056948153659869L;
	private List<AnalyticNode> subFlowNodes = null;
	private AnalyticNode exitNode ;
	private HashMap<String ,String> subflowVariableModel ;
	
	public HashMap<String, String> getSubflowVariableModel() {
		return subflowVariableModel;
	}

	public void setSubflowVariableModel(HashMap<String, String> subflowVariableModel) {
		this.subflowVariableModel = subflowVariableModel;
	}

	public AnalyticNode getExitNode() {
		return exitNode;
	}

	public void setExitNode(AnalyticNode exitNode) {
		this.exitNode = exitNode;
	}

	public void addSubFlowChildNodes(List<AnalyticNode> subFlowNodes) {
		this.subFlowNodes = subFlowNodes;
		
	}
	 
	public List<AnalyticNode> getSubFlowChildNodes() {
		return subFlowNodes;
	}
 
	public SubFlowConfig getSubFlowConfig(){ 
		return (SubFlowConfig)super.getSource().getAnalyticConfig();
	}

}
