/**
 * ClassName DBTableNameParamterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

/**
 * @author zhaoyong
 *
 */
public class DBTableNameParamterHelper extends SingleSelectParameterHelper {
    private static final Logger itsLogger=Logger.getLogger(DBTableNameParamterHelper.class);

    public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) {
 		return getAvaliableValues(parameter,userName,dbType,Locale.getDefault()); 
	}

	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType,Locale locale) {
		 
		List<String> tableNames=new ArrayList<String>(); 
		String schemaName=getSchemaNameInOperator(parameter.getOperator());//schemaName or outputSchema
		String dbConnName=getDBConnNameInOperator(parameter.getOperator());
		
		if(StringUtil.isEmpty(dbConnName)){
			return tableNames;
		}
		try {
			  DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE.getManager();
	 			
				 DbConnectionInfo connection = dbManager.getDBConnection(userName, dbConnName,dbType);
				  
				 tableNames = DBMetadataManger.INSTANCE.getTableAndViewNameList(connection.getConnection(), schemaName);

		} catch (Exception e) {
			 itsLogger.error(e.getMessage(),e) ;
			 return tableNames; 
		}
 		return tableNames; 
	}



}
