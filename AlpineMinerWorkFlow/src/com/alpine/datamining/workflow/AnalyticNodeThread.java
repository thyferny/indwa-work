package com.alpine.datamining.workflow;

import java.util.List;
import java.util.concurrent.Callable;

import com.alpine.datamining.api.AnalyticFlowStatus;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticSource;

public class AnalyticNodeThread implements Callable<AnalyticNode> {
	
	private AnalyticNode analyticNode;
	private AnalyticRunner runner;
	private List<AnalyticProcessListener> listeners;
	private AnalyticSource source;
	private AlpineParallelFlowExecutor parallelFlowExecutor;

	public AnalyticNodeThread(AnalyticProcess process,
			AnalyticNode analyticNode, List<AnalyticProcessListener> listeners,
			AnalyticRunner runner, AlpineParallelFlowExecutor alpineParallelFlowExecutor) {
		this.analyticNode = analyticNode;
		this.listeners = listeners;
		this.runner = runner;
		this.source = analyticNode.getSource();
		this.parallelFlowExecutor = alpineParallelFlowExecutor;
		
	}

	@Override
	public AnalyticNode call() throws Exception {
		//It seems there might be some issue on adaboos, it is setting it to adaboos
		analyticNode.setStatus(AnalyticFlowStatus.Active);
		runner.notifyNodeStart(analyticNode, listeners);
		try {
			AnalyticOutPut outPut = null;
			outPut = runner.executeNode(analyticNode, source, true);
			runner.notifyNodeFinished(analyticNode, listeners, outPut);
			analyticNode.setStatus(AnalyticFlowStatus.FinishedSucceded);
			parallelFlowExecutor.notifyAnalyzingCompleted(analyticNode);
		} catch (Throwable e) {
			analyticNode.setStatus(AnalyticFlowStatus.FinishedFailed);
		} 

		return analyticNode;
	}

}
