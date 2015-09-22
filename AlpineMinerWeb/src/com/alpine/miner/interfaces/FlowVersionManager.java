/**
 * ClassName :FlowVersionManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-10-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.FlowVersionManagerImpl;

/**
 * @author zhaoyong
 *
 */
public interface FlowVersionManager {
	public static final FlowVersionManager INSTANCE = new FlowVersionManagerImpl();
	public List<FlowInfo> getFlowVersionInfos(FlowInfo info) ;

    public void copyHistoryToNewName(FlowInfo src, FlowInfo dest) throws Exception;

	public void copyFlowToHistory(FlowInfo flowInfo ) throws IOException 	 ;
	

	public FlowInfo relaodFlowInfo(FlowInfo flowInfo) throws IOException,
			FileNotFoundException ;
	/**
	 * @param flowName
	 * @param user
	 * @return
	 */
	public boolean hasHistoryVersion(String flowName, String user);
	
	
	/**
	 * @param flowName
	 * @param user
	 * @return
	 */
	//for history use...
	public String getLatestFlowVersion(String flowName, String user);
	
	
	public boolean hasHistoryVersion(FlowInfo flowInfo  );
	public String getLatestFlowVersion(FlowInfo flowInfo  );


	public FlowInfo relaodFlowInfo4Version(FlowInfo info, String version) throws Exception;

	//flowInfo is the history
	 

	/**
	 * @param flowInfo
	 * @throws IOException 
	 */
	public void deleteFlowhistory(FlowInfo flowInfo) throws IOException;


	FlowInfo replaceWithVersion(FlowInfo flowInfo, Locale locale)
			throws Exception;

}
