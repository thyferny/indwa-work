/**
 * ClassName AnalyticFlow.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.api.AnalyticFlow;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.utility.db.Resources;


 

/**
 * @author John Zhao
 *
 */
public class AnalyticFlowImpl extends AnalyticNodeImpl implements AnalyticFlow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2506654062104893742L;

	//is a flat structure- all nodes
	private List<AnalyticNode> nodes;

	private String flowOwnerUser="";
	
	private String version=Resources.minerEdition;
	




	private String flowDescription="";



	public AnalyticFlowImpl(){
		super();
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticFlow#getAllNodes()
	 */
	@Override
	public List<AnalyticNode> getAllNodes() {
		// TODO Auto-generated method stub
		return nodes;
	}
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
 

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticFlow#getStartNodes()
	 */
	@Override
	public List<AnalyticNode> getStartNodes() {
		List<AnalyticNode> startNodes=new ArrayList<AnalyticNode>();
		for (AnalyticNode node : nodes) {
			if(node.getParentNodes()==null||node.getParentNodes().size()==0){
				startNodes.add(node);
			}
		}
		
		// TODO Auto-generated method stub
		return startNodes;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticFlow#setAllNodes(java.util.List)
	 */
	@Override
	public void setAllNodes(List<AnalyticNode> nodes) {
		this.nodes=nodes;
		
	}


	public String getFlowOwnerUser(){
		return flowOwnerUser;
	}

	/**
	 * @return
	 */
	public String getFlowDescription(){
		return flowDescription;
	}

	public void setFlowOwnerUser(String flowOwnerUser) {
		this.flowOwnerUser = flowOwnerUser;
	}

	public void setFlowDescription(String flowDescription) {
		this.flowDescription = flowDescription;
	}
	
	 

}
