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
public interface Task extends Serializable {

	public String getName();
	
	public String getGroup();
	 
	public Map<String,String>  getParams();
	/**
	 * 
	 */
	public boolean  run(); 
}
