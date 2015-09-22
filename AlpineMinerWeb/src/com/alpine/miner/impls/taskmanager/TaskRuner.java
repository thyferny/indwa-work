/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Gary
 *
 */
public interface TaskRuner extends  Serializable{

	/**
	 * 
	 * @param params
	 */
	public boolean execute(Map<String,String> params);
}
