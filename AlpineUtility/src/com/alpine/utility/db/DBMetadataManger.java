/**
 * Classname DBMetadataManger.java
 *
 * Version information:1.00
 *
 * Data:2012-01-25
 *
 * COPYRIGHT (C) 2012 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.db;

import java.util.List;

public interface DBMetadataManger {
	public static final DBMetadataManger INSTANCE = new DBMetadataMangerImpl();
 
	public  List<String> getSchemaList(DbConnection connectionInfo) throws Exception ;
	//table type can be table and view
	public List<String> getTableAndViewNameList(DbConnection connectionInfo,String schemaName) throws Exception ;
		 

	//this is for Illuminator use only
	public void  refreshSchema(DbConnection connectionInfo,String schemaName,boolean forceReconn) throws Exception;
	
	public void  refreshSchemaTables(DbConnection connectionInfo,String schemaName,boolean forceReconn) throws Exception;
	public void  refreshSchemaViews(DbConnection connectionInfo,String schemaName,boolean forceReconn) throws Exception;
	
	public void  refreshTable(DbConnection connectionInfo,String schemaName,String tableName,boolean forceReconn)throws Exception;
	

	
	//this is used by AM and Illuminator at the same time
	public void refreshDBConnection(DbConnection dbc, boolean recursive,boolean forceReconn) throws Exception;
	
	
	//the following is for AM UI use only
	public void  refreshSchema(DbConnection connectionInfo,DBSchemaMetaInfo schemaInfo,boolean forceReconn)throws Exception;
	
	public void  refreshSchemaTables(DbConnection connectionInfo,DBSchemaMetaInfo schemaInfo,boolean forceReconn) throws Exception;
	public void  refreshSchemaViews(DbConnection connectionInfo,DBSchemaMetaInfo schemaInfo,boolean forceReconn) throws Exception;
	
	public void  refreshTable(DbConnection connectionInfo, DBTableMetaInfo table,boolean forceReconn) throws Exception;	
	

	
	public DataBaseMetaInfo getDataBaseMetaInfo(DbConnection dbc) throws Exception;	
	public DBSchemaMetaInfo getDBSchemaMetaInfo(DbConnection dbc, String schemaName) throws Exception;
	List<TableColumnMetaInfo> getTableColumnInfoList(
			DbConnection connectionInfo, String schemaName, String tableName)
			throws Exception;

	//this is for the clear temp table
	public void removeTableFromCache(DbConnection connectionInfo,String schemaName,String tableName);
	//cache here to avoid ping db each time 
	public boolean isGreenPlumDataBase(DbConnection connectionInfo) throws Exception;
 }
