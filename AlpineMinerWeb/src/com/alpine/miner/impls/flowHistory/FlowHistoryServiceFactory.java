package com.alpine.miner.impls.flowHistory;

import com.alpine.miner.interfaces.FlowHistoryService;

public class FlowHistoryServiceFactory {

	private FlowHistoryServiceFactory(){}
	
	public static FlowHistoryService getService(String category){
		return new FlowHistoryServiceImpl(category);
	}
}
