/**
 * ClassName AnalyticNode.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.io.Serializable;
import java.util.List;

/**
 * @author John Zhao
 *
 */
public interface AnalyticNode extends Serializable{
	
 


	public abstract List<AnalyticNode> getChildNodes();

	public abstract void setChildNodes(List<AnalyticNode> childNodes);

	//using string to keep it can easy plugin a new analyzer as a simple jar
	public abstract String getAnalyzerClass();
	
	public abstract void setAnalyzerClass(String analyzerClass);
//defualt source or only one source
	public abstract AnalyticSource getSource();

	public abstract void setSource(AnalyticSource source);
	
	public abstract List<AnalyticSource> getAllSources();

	public abstract void setAllSources( List<AnalyticSource> sources);
	
	public abstract boolean isRoot();

	public abstract void setRoot(boolean isRoot);

	public abstract boolean isLeaf();

	public abstract void setLeaf(boolean isLeaf);

	public abstract List<AnalyticNode> getParentNodes();

	public abstract void addChildNode(AnalyticNode childNode);
	
	public void setParentNodes(List<AnalyticNode> parentNodes) ;
	
	public void setFinished(boolean isFinished) ;
	public boolean isFinished( ) ;
	
	//i for XML sereialzation , 2 for engine use
	public String getID( );
	
	public String setID(String id);
	
	public String getName( );
	
	public String setName(String name);
	public String getType( );//analyzer or model
	
	public String setType(String type);
	
	public AnalyticFlowStatus getStatus();//
	
	public void setStatus(AnalyticFlowStatus status);
	
	/**
	 * @return
	 */
	public boolean isActive();
	/**
	 * @return
	 */
	public boolean isWaiting();

	/**
	 * @param analyticNodeImpl
	 */
	public abstract void addParentNode(AnalyticNode analyticNode);

	public AnalyticOutPut getOutput() ;

	public void setOutput(AnalyticOutPut output)  ;
	
//	public String getNodeType(); // model trainer or prodictor...
	//node name of the subflow
	public String getGroupID() ;

	public void setGroupID(String groupID) ;

	void setAdaBoost(boolean adaBoost);

	boolean getAdaBoost();

	 
}