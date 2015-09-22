/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SessionListenerImpl.java
 */
package com.alpine.miner.impls.web.resource;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.alpine.miner.workflow.runner.FlowRunningHelper;

/**
 * @author Gary
 * Sep 3, 2012
 */
public class SessionListenerImpl implements HttpSessionListener {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
		Logger.getLogger(SessionListenerImpl.class).warn("Create Session.");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		String userName = LoginManager.removeUser(sessionEvent.getSession());
		FlowRunningHelper.getInstance().releaseUserMemory(userName);
	}

}
