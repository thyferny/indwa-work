/**
 * ClassName OperatorStatus.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-2
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator;

import java.util.HashMap;

public class OperatorStatus {
	private HashMap<String,Boolean> statusMap=null;
	
	public void addStatus(String parameter,Boolean status){
		if(statusMap==null){
			statusMap=new HashMap<String,Boolean>();
			statusMap.put(parameter, status);
		}
	}
	
	public boolean getStatus(String parameter){
		return statusMap.get(parameter);	
	}
	
}
