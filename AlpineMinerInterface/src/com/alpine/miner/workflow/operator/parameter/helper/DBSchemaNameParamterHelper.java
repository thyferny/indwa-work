/**
 * ClassName DBSchemaNameParamterHelper.java
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
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

/**
 * @author zhaoyong
 * 
 */
public class DBSchemaNameParamterHelper extends SingleSelectParameterHelper {
    private static final Logger itsLogger=Logger.getLogger(DBSchemaNameParamterHelper.class);

    @Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType, Locale locale) {
		// find the dbconnection of this operator or his parent's
		List<String> schemaNames = new ArrayList<String>();

		String dbConnectionName = null;
		List<Object> inputList = parameter.getOperator().getOperatorInputList();
		if(inputList==null){
			return schemaNames;
		}
		for (Object obj : inputList) {
			if (obj instanceof OperatorInputTableInfo) {
				dbConnectionName = ((OperatorInputTableInfo) obj)
						.getConnectionName();
			}
		}

		if(parameter.getOperator() instanceof CopyToDBOperator){
			OperatorParameter connPara=parameter.getOperator().getOperatorParameter(OperatorParameter.NAME_dBConnectionName);
			if(connPara.getValue()!=null){
				dbConnectionName=(String)connPara.getValue();
			}
		}
		 
		if (StringUtil.isEmpty(dbConnectionName)) {
			return schemaNames;	
		}

		try {
			DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE.getManager();
 			
			DbConnectionInfo connection = dbManager.getDBConnection(userName, dbConnectionName,dbType);
			  
			List<String> schemaList = DBMetadataManger.INSTANCE.getSchemaList(connection.getConnection());
			
			schemaNames.addAll(schemaList);
			//can not apply DEFAULT_SCHEMA for copy to database, 
			if(schemaNames.size()>0&&!schemaNames.get(0).equals(VariableModel.DEFAULT_SCHEMA)){
				schemaNames.add(0, VariableModel.DEFAULT_SCHEMA);
				 
			}		
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);

			return schemaNames;
		}
		return schemaNames;
	}

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType) {
		return getAvaliableValues(parameter,userName,dbType,Locale.getDefault());
	}
	
}
