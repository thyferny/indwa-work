package com.alpine.miner.ifc;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;


/**The implementation must have cache and refresh function...
 * */
public interface HadoopConnectionManagerIfc {
	public List<HadoopConnection> getAllHadoopConnection(String username,
			ResourceType type) throws Exception ;
	//return the filepath
	public   String saveHadoopConnection(Properties props, String userName) throws Exception ;
	
	public   boolean updateHadoopConnectionResource(File file,	Properties props)    throws Exception ;
    
	 	
	public   HadoopConnection readHadoopConnection(String connName,String username ) throws Exception;
	public   HadoopConnection readHadoopConnection(String connName,String username, ResourceType type) throws Exception;
	
	 	
	public boolean refreshHadoopConnection(HadoopConnection connection,String username,boolean isRecursive);
	
	public boolean refreshHadoopFiles(HadoopConnection connection,String username,HadoopFile file,boolean isRecursive);
 
}
