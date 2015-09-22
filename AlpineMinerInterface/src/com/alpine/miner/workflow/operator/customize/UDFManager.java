/**
 * ClassName :UDFManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-10-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.customize;

import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;

/**
 * @author zhaoyong
 *
 */
public interface UDFManager {
	public static final UDFManager INSTANCE = new UDFManagerImpl();
	public static final String XML = "xml";
	public String getRootDir() ;
	public void setRootDir(String rootDir) ;
	
	public String getOperatorRegistryRootDir() ;
	public void setOperatorRegistryRootDir(String operatorRegistryRootDir) ;
	
	public List<CustomizedOperatorModel> getAllCustomizedOperatorModels() throws Exception;	
	public boolean deleteCustomizedOperatorModels(String operatorName) throws   Exception;
	public boolean importUDFFile(String udfFiePath) throws Exception;
	
 
 
	
	public CustomizedOperatorModel getCustomizedOperatorModelByUDFName(String udfName) throws Exception;
	public CustomizedOperatorModel getCustomizedOperatorModelByOperatorName(String operatorName) throws Exception;

}
