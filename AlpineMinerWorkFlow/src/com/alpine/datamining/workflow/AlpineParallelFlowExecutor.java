package com.alpine.datamining.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticFlowStatus;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;

public class AlpineParallelFlowExecutor {
	private Logger itsLogger = Logger.getLogger(AlpineParallelFlowExecutor.class);
	private AnalyticProcess process;
	
	private Set<AnalyticNode> nodes;
	private List<AnalyticProcessListener> listeners;
	private volatile boolean isAlreadyExecuted;
	private AnalyticRunner runner;
	private ThreadPoolExecutor threadPool;

	
	//Thread pool parameters
	private int poolSize = 5;
	private int maxPoolSize = 5;
    long keepAliveTime = 60;
    private  static final  TimeUnit TIME_UNIT=TimeUnit.SECONDS;
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
	//
    private Set<AnalyticNode> analyzedNodes;
    private Set<AnalyticNode> nodesToBeAnalyzed;
    private Set<AnalyticNode> nodesFailedToBeAnalyzed;
	private List<Future<AnalyticNode>> futures;
	private CountDownLatch doneSignal;
	private Object theLock = new Object();
	
    
    
    
	public AlpineParallelFlowExecutor(AnalyticProcess process,
			List<AnalyticNode> nodeList, List<AnalyticProcessListener> listeners,AnalyticRunner runner) {
		this.process=process;
		this.listeners=Collections.synchronizedList(listeners);
		this.runner = runner;
		this.isAlreadyExecuted=false;
		
		init(nodeList);
		
	}
	private void init(List<AnalyticNode> nodeList) {
		
		this.nodes= Collections.synchronizedSet(new HashSet<AnalyticNode>());
		for(AnalyticNode node:nodeList){
			nodes.add(node);
		}
		
		initNodeLists();
		initializeThreadPool();
		if(null==identifyTheRoots()||0==identifyTheRoots().size()){
			throw new IllegalArgumentException("There is no root");
		}
		
	}
	
	private void initNodeLists() {
		
		for(AnalyticNode node:nodes){
			node.setStatus(AnalyticFlowStatus.NotStarted);
		}
		
		
		
		nodesToBeAnalyzed = Collections.synchronizedSet(new HashSet<AnalyticNode>());
		for(AnalyticNode node:nodes){
			nodesToBeAnalyzed.add(node);
		}
		futures = Collections.synchronizedList(new ArrayList<Future<AnalyticNode>>());
		analyzedNodes = Collections.synchronizedSet(new HashSet<AnalyticNode>());
		nodesFailedToBeAnalyzed  = Collections.synchronizedSet(new HashSet<AnalyticNode>());
		
		
		
	}
	private void initializeThreadPool() {
		//		corePoolSize - the number of threads to keep in the pool, even if they are idle.
		//		maximumPoolSize - the maximum number of threads to allow in the pool.
		//		keepAliveTime - when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
		//		unit - the time unit for the keepAliveTime argument.
		//		workQueue - the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
		
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,keepAliveTime,TIME_UNIT , queue);
		
	}

	public boolean execute() throws AnalysisException, Exception {
		synchronized (theLock) {
			if (isAlreadyExecuted) {
				throw new IllegalArgumentException("Already executed");
			}

			doneSignal = new CountDownLatch(nodesToBeAnalyzed.size());

			isAlreadyExecuted = true;
		}
		analyzeTheNodes();
		doneSignal.await();
		return nodesFailedToBeAnalyzed.size() > 0;
	}
	
	public synchronized void notifyAnalyzingCompleted(AnalyticNode completedNode){
		
		updateJobLists(completedNode);
		boolean anyFailed = nodesFailedToBeAnalyzed.size()>0;
		
		if(anyFailed){
			shutDown();
			for(int i = 0;i<nodes.size();i++){
				doneSignal.countDown();
			}
		}
		doneSignal.countDown();
		try {
			analyzeTheNodes();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	private boolean isSubmissionCompeted() {
		return !nodesToBeAnalyzed.isEmpty();
		
	}
	private void updateJobLists(AnalyticNode completedNode) {
		nodesToBeAnalyzed.remove(completedNode);
		analyzedNodes.add(completedNode);
		if (completedNode.getStatus().equals(AnalyticFlowStatus.FinishedFailed)) {
			nodesFailedToBeAnalyzed.add(completedNode);
		}

	}
	
	private void analyzeTheNodes() throws InterruptedException, ExecutionException {
		sumbitReadyNodesToBeAnalyzed();

	}
	
	
	
	private boolean sumbitReadyNodesToBeAnalyzed() {

		boolean isAnySubmitted =false;
		List<AnalyticNode> nodesToBeRemoved =Collections.synchronizedList(new ArrayList<AnalyticNode>());
		for (AnalyticNode r : nodesToBeAnalyzed) {

			List<AnalyticNode> parentNodes = r.getParentNodes();
			if (null == parentNodes || analyzedNodes.containsAll(parentNodes)) {
				nodesToBeRemoved.add(r);
				r.setStatus(AnalyticFlowStatus.Waiting);
				AnalyticNodeThread t = new AnalyticNodeThread(process, r,listeners, runner,this);
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("Submitting the node to be analyzed on thread pool:"+t);
				}
				futures.add(threadPool.submit(t));
				isAnySubmitted=true;
			}

		}
		nodesToBeAnalyzed.removeAll(nodesToBeRemoved);
		return isAnySubmitted;
	}
	
	
	private boolean isCompleted(AnalyticNode child) {
		return child.getStatus().equals(AnalyticFlowStatus.FinishedFailed)||
			   child.getStatus().equals(AnalyticFlowStatus.FinishedSucceded);
	}
	private List<AnalyticNode> identifyTheRoots() {
		List<AnalyticNode> roots = new ArrayList<AnalyticNode>();
		for(AnalyticNode node:nodes){
			if(null!=node&&(null==node.getParentNodes()||0==node.getParentNodes().size())){
				roots.add(node);
			}
		}
		return roots;
		
	}
	public void shutDown(){
		
		threadPool.shutdown();
	}
	
	
}
