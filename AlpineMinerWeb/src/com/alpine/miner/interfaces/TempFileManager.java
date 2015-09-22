/**
 * ClassName :TempFileManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.interfaces;

import com.alpine.miner.impls.web.resource.TempFileManagerImpl;

/**
 * @author zhaoyong
 *
 */
public interface TempFileManager {
	
	public static TempFileManager INSTANCE= new TempFileManagerImpl();
	public static final String TYPE_MODEL = "temp_model";
	public static final String TYPE_REPORT = "temp_report";
	public static final String TYPE_FLOW = "temp_flow"; //include the flow history...
	
 
	
	public String getTempFolder4Flow ( );
	public String getTempFolder4Model ( );
	public String getTempFolder4Report ( );
	
	//force clear all temp file
	//public void forceClearAll();
	
	//scan all the file find the exceed the live time to delete 
	public void scanAndClear();
	//for test use...
	void init(String tempFolder, long liveTime, long scanFrequency) throws Exception;
	
 	

}
