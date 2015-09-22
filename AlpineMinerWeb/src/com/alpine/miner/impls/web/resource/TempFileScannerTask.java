/**
 * ClassName :TempFileScannerTask.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import java.util.HashMap;
import java.util.Map;

import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.interfaces.TempFileManager;

/**
 * @author zhaoyong
 *
 */
public class TempFileScannerTask implements Task{
	private static Map<String, String> emptyParamMap = new HashMap<String, String>();	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4942338507248756482L;
	 

	@Override
	public String getGroup() {
		return "sys_temp_group";
	}

	@Override
	public String getName() {
		return "sys_temp_job";
	}

	@Override
	public Map<String, String> getParams() {
		return emptyParamMap;
	}

	@Override
	public boolean run() {
		TempFileManager.INSTANCE.scanAndClear();
		return true;
	}

}
