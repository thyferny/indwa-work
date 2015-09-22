package com.alpine.miner.interfaces;

import java.util.Collection;

import com.alpine.miner.impls.flowHistory.FlowHistoryInfo;

public interface FlowHistoryService {

	Collection<FlowHistoryInfo> getFlowHistory();
	
	void pushNewFlowHistory(FlowHistoryInfo flowInfo);
	
	void removeFlowHistory(FlowHistoryInfo flowInfo);
	
	void clearFlowHistory();
}
