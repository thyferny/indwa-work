/**
 * ClassName AnalyticNode.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticFlowStatus;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;


/**
 * @author John Zhao
 * 
 */
public class AnalyticNodeImpl implements AnalyticNode {

	/**
	 * 
	 */
	private static final Logger itsLogger = Logger.getLogger(AnalyticNodeImpl.class);
	private static final long serialVersionUID = 7883220645501579220L;
	private String analyzerClass;
	private AnalyticSource source;
	private boolean isRoot;
	private boolean isLeaf;
	private List<AnalyticNode> parentNodes=new ArrayList<AnalyticNode>();
	private List<AnalyticNode> childNodes=new ArrayList<AnalyticNode>();
	private boolean isFinished;
	AnalyticOutPut output=null; 
	private List<AnalyticSource> sources;
	private boolean isAdaboost=false;
	
	private String id;
	
	private String name;
	private String type;
	private AnalyticFlowStatus status;
	String groupID;

 
	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	@Override
	public void setStatus(AnalyticFlowStatus status) {
		this.status = status;
	}

	public AnalyticNodeImpl() {
		childNodes = new ArrayList<AnalyticNode>();
		parentNodes = new ArrayList<AnalyticNode>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#getChildNodes()
	 */
	public List<AnalyticNode> getChildNodes() {
		return childNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<AnalyticNode> childNodes) {
		this.childNodes = childNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#getAnalyzerClass()
	 */
	public String getAnalyzerClass() {
		return analyzerClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.AnalyticNode#setAnalyzerClass(java.lang.Class)
	 */
	public void setAnalyzerClass(String analyzerClass) {
		this.analyzerClass = analyzerClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#getSource()
	 */
	public AnalyticSource getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.AnalyticNode#setSource(com.alpine.datamining
	 * .api.AnalyticSource)
	 */
	public void setSource(AnalyticSource source) {
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#isRoot()
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#setRoot(boolean)
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#isLeaf()
	 */
	public boolean isLeaf() {
		return isLeaf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#setLeaf(boolean)
	 */
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#getParentNode()
	 */
	public List<AnalyticNode> getParentNodes() {
		return parentNodes;
	}

	public void setParentNodes(List<AnalyticNode> parentNodes) {
		this.parentNodes = parentNodes;
		if(itsLogger.isDebugEnabled()){
			if(null==parentNodes){
				itsLogger.debug("There are no parents");
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.AnalyticNode#addChildNodes(com.alpine.datamining
	 * .api.AnalyticNodeImpl)
	 */
	public void addChildNode(AnalyticNode childNode) {
		if(null==childNode){
			itsLogger.error("Child node is null");
			return;
		}
		this.childNodes.add(childNode);
		if(childNode.getParentNodes().contains(this)==false){
			childNode.addParentNode(this);
		}
	}
 

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#setFinished(boolean)
	 */
	@Override
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#getID()
	 */
	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.datamining.api.AnalyticNode#setID(java.lang.String)
	 */
	@Override
	public String setID(String id) {
		// TODO Auto-generated method stub
		return this.id = id;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String setName(String name) {
		// TODO Auto-generated method stub
		return this.name=name;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public String setType(String type) {
		// TODO Auto-generated method stub
		return this.type=type;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticNode#getStatus()
	 */
	@Override
	public AnalyticFlowStatus getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticNode#isActive()
	 */
	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return AnalyticFlowStatus.Active.equals(status);
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticNode#isWaiting()
	 */
	@Override
	public boolean isWaiting() {
		// TODO Auto-generated method stub
		return AnalyticFlowStatus.Waiting.equals(status);
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticNode#addParentNode(com.alpine.datamining.api.AnalyticNode)
	 */
	@Override
	public void addParentNode(AnalyticNode analyticNode) {
		this.parentNodes.add(analyticNode);
		if(null==parentNodes){
			itsLogger.debug("There are no parents");
		}
		
	}

	public AnalyticOutPut getOutput() {
		return output;
	}

	public void setOutput(AnalyticOutPut output) {
		this.output = output;
	}	

	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public List<AnalyticSource> getAllSources() {
		return sources;
	}

	@Override
	public void setAllSources(List<AnalyticSource> sources) {
		this.sources=sources;
	}
	
	@Override
	public void setAdaBoost(boolean adaBoost){
		isAdaboost=adaBoost;
	}
	@Override
	public boolean getAdaBoost(){
		return isAdaboost;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AnalyticNodeImpl [source=");
		builder.append(source);
		builder.append(", isRoot=");
		builder.append(isRoot);
		builder.append(", isLeaf=");
		builder.append(isLeaf);
		builder.append(", isFinished=");
		builder.append(isFinished);
		builder.append(", output=");
		builder.append(output);
		builder.append(", id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", type=");
		builder.append(type);
		builder.append(", status=");
		builder.append(status);
		builder.append(", groupID=");
		builder.append(groupID);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalyticNodeImpl other = (AnalyticNodeImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

	

}


