/**
 * ClassName  AnalyticThreadPool.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-17
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Engine always use this to run the thread, if more than nThreads threads
 * executed pool will wait...
 * 
 * @author John Zhao
 * 
 */
public class AnalyticThreadPool     {
	private static final Logger itsLogger = Logger.getLogger(AnalyticThreadPool.class);
	private final int nThreads;
	private final PoolWorker[] threads;
	private final LinkedList<AnalyticRunner> queue;
 
	
	public AnalyticThreadPool(int nThreads, int checkThreadPeriod) {
	 
		//nThreads is the system capacity, the max number of flows can be executed togather
		this.nThreads = nThreads;
//		this.checkThreadPeriod = checkThreadPeriod;
		queue = new LinkedList<AnalyticRunner>();
		threads = new PoolWorker[nThreads];
		for (int i = 0; i < this.nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
		// use a timer to check it, remove the dead one and create a new one...
//		timer = new Timer(true);
//		timer.schedule(new ThreadPoolChecker(this), 0, this.checkThreadPeriod);

	}

	public void execute(AnalyticRunner runner) {
		synchronized (queue) {
			//for stop use...
			queue.addLast(runner);
			queue.notify();
			// here need not run it directly,
			// when PoolWorker finish one, it will take another one to run...
		}
	}

	class PoolWorker extends Thread {
		AnalyticRunner runner;
		boolean isStoped = false;
		private String processID;

		public String getProcessID() {
			return this.processID;
		}

		public void run() {

			// run for ever...
			while (!isStoped) {
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) {
							
							runner = null;
						}
					}
					runner = (AnalyticRunner) queue.removeFirst();
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("Thread pool find new flow in the queue:" +runner.getProcessID());
					}
				}
				
				processID=runner.getProcessID();
				// If we don't catch RuntimeException,
				// the pool could leak threads
				try {
					// here is synchronized call...
					//will block this thread untill finished
					runner.run();
					runner = null;
 
 
					AnalyticThreadPool.this.stop(processID,true);
				 
				} catch ( Throwable e) {
					 
					itsLogger.error(
							"Thread pool exception:",e);
 
					AnalyticThreadPool.this.stop(processID,false);
				 
			 
				}
 
			}
		}
 
		public void setStopped(boolean isStoped) {
			this.isStoped = isStoped ;
			
		}

	}

	public synchronized void checkAllThreads() {

		for (int i = 0; i < threads.length; i++) {
			PoolWorker thread = threads[i];
			if (thread.isAlive() == false) {
				thread.interrupt();
				threads[i] = new PoolWorker();
				threads[i].start();
			}
		}
	}

	/**
	 * @param processID
	 * @param b 
	 */
	public void stop(String processID, boolean finish) { 
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("AnalyticThreadPool.stop process id=" + processID);
		}
			//here remove the runner	
//		AnalyticRunner runner = runnerMap.remove( processID);

		removeNoneStartedRunner(processID);
			for (int i = 0; i < threads.length; i++) {
				
				if (threads[i].getProcessID() != null
						&& threads[i].getProcessID().equals(processID)) {
		 //todo ...
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AnalyticThreadPool stoping threads " + i + " processID="
								+ threads[i].getProcessID()+" threadname = " +threads[i].getName() +" id= " +threads[i].getId());
					}
					// listner...
					if(threads[i].runner!=null&&finish==false){//error, so really stop, otherwise is the running finished
						
						threads[i].runner.stopProcess();
					}//otherwise it is finished.
					threads[i].setStopped(true);
					//have to 
 
					threads[i] = null;
					threads[i] = new PoolWorker();
					if(itsLogger.isDebugEnabled()){
 						itsLogger.debug("AnalyticThreadPool.  create a new thread  =" + threads[i].getName()+" "+threads[i].getId());
 					}
					threads[i].start();
					
					System.gc();
					return;
				}
			}
 

	}

	private void removeNoneStartedRunner(String processID) {
		AnalyticRunner notStartedRunner = null;
		for (Iterator iterator = queue.iterator(); iterator.hasNext();) {
			AnalyticRunner runner = (AnalyticRunner) iterator.next(); 
			if (processID.equals( runner.getProcessID())) {
				notStartedRunner= runner;
			} 
			
		}
		 if(notStartedRunner!=null){
			 queue.remove(notStartedRunner) ;
		 }
	}

 

	
}
