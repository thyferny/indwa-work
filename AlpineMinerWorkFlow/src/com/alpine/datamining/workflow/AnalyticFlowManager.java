/**
 * ClassName PersistenceManager.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import com.alpine.datamining.api.AnalyticFlow;

/**
 * @author John Zhao
 *
 */
public interface AnalyticFlowManager {

	public boolean saveAnalyticFlow(AnalyticFlow flow);
	public AnalyticFlow findAnalyticFlow(String flowName);
}
