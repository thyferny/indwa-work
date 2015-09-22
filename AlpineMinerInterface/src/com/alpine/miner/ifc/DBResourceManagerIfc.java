/**
 * ClassName DBResourceManagerIfc.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-22
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.ifc;

import java.util.List;

import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.utility.db.TableColumnMetaInfo;

public interface DBResourceManagerIfc {

	/**
	 * @return
	 */
	public abstract List<DbConnectionInfo> getDBConnectionList(String userLogin);

	/**
	 * 
	 * @param info
	 * @throws OperationFailedException 
	 */
	public abstract void createDBConnection(DbConnectionInfo info)
			throws OperationFailedException;

	public abstract void updateDBConnection(DbConnectionInfo info)
			throws DataOutOfSyncException;

	public abstract void deleteDBConnection(DbConnectionInfo info) throws Exception;

	public abstract List<DbConnectionInfo> getDBConnectionListByPath(String path);

	//now only support public driver
	public abstract List<JDBCDriverInfo> getJDBCDriverInfos();

	public abstract void createJDBCDriverInfos(JDBCDriverInfo info);

	public abstract void initJDBCDriverInfo();

	public abstract DbConnectionInfo getDBConnection(String userName,
			String connName, ResourceType resourceType) throws Exception;

	public abstract String[] getSchemaList(String userName,
			String dbConnectionName, ResourceType dbType) throws Exception;

	public abstract String[] getTableList(String userName, String dbConnName,
			String schemaName, ResourceType dbType) throws Exception;

	public abstract List<TableColumnMetaInfo> loadColumnList(String userName,String connName,
			String schemaName, String tableName, ResourceType resourceType) throws Exception;

}