/**
 * ClassName AnalyticFlow.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.List;

import com.alpine.utility.db.Resources;

 

/**
 * @author John Zhao
 *
 */
public interface AnalyticFlow extends  AnalyticNode{
 
	public static final String Version = Resources.minerEdition; 

	public List<AnalyticNode> getStartNodes();
	public List<AnalyticNode> getAllNodes();
	public void setAllNodes(List<AnalyticNode> nodes);
	/**
	 * @return
	 */
	public String getFlowOwnerUser();
	/**
	 * @return
	 */
	public String getFlowDescription();


	public void setFlowOwnerUser(String flowOwnerUser) ;

	public void setFlowDescription(String flowDescription) ;

	

	public String getVersion() ;

	public void setVersion(String version) ;
}
