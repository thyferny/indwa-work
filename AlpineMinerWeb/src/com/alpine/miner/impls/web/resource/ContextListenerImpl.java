/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ContextListenerImpl.java
 */
package com.alpine.miner.impls.web.resource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.alpine.miner.impls.taskmanager.TaskManagerStore;

/**
 * @author Gary
 * Dec 13, 2012
 */
public class ContextListenerImpl implements ServletContextListener {

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		TaskManagerStore.SCHEDULER.getInstance().termination();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		//nothing to do yet.
	}

}
