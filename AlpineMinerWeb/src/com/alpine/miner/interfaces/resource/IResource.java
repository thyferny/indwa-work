package com.alpine.miner.interfaces.resource;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.alpine.miner.workflow.operator.OperatorWorkFlow;

/**   
 * ClassName:IResourceTree   
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 */
public interface IResource {
	public List findFolders();
	public List findFiles();
	public Properties getDBConnectionProps()throws IOException;
	public void updateDBConnction(Properties props)throws IOException;
	
	public OperatorWorkFlow getWorkFlow();
	public int getOperatorIndex(String classname);
	public boolean isParameterView(String columnName);
}
