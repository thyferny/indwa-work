/**
 * ClassName DBConnectionParamterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhaoyong
 *
 */
public class DBConnectionParamterHelper extends SingleSelectParameterHelper {
	
	List<String> canSelectNullValueParameters=Arrays.asList(new String[]{
			OperatorParameter.NAME_dBConnectionName+SQLExecuteOperator.class.getCanonicalName(),
	});
	 
	@Override
 
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) {
		 
		List<String> result=new ArrayList<String>(); 
		
		String operatorClassName=parameter.getOperator().getClass().getCanonicalName();
		//if user is null, will get all public connections
		DBResourceManagerIfc dbManager=DBResourceManagerFactory.INSTANCE.getManager();
		
		List<DbConnectionInfo> dbConnections = dbManager.getDBConnectionList(userName);
		for (Iterator<DbConnectionInfo> iterator = dbConnections.iterator(); iterator.hasNext();) {
			DbConnectionInfo dbConnectionInfo = iterator.next();
			if(dbType==null||dbType.equals(dbConnectionInfo.getResourceType())){
				result.add(dbConnectionInfo.getId()) ;
			}
		}
		if(canSelectNullValueParameters.contains(parameter.getName()+operatorClassName)){
			result.add(0,"");
		} 
		return result;
	}
 

}
