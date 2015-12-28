
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
