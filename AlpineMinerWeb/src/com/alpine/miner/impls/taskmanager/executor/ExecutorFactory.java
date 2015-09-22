/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor;

import org.apache.commons.logging.LogFactory;

import com.alpine.miner.impls.taskmanager.executor.scheduler.quartz.QuartzExecutorImpl;

/**
 * @author Gary
 *
 */
public class ExecutorFactory {
	
	private static Executor handler;
	
	static{
		try {
			handler = new QuartzExecutorImpl();
		} catch (InitializeException e) {
			LogFactory.getLog(ExecutorFactory.class).error(e);
			System.err.println(e.getMessage());
		}
	}

	private ExecutorFactory(){}
	
	public static Executor getExecutor(){
		return handler;
	}
}
