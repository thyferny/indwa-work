/**
 * Classname DBMetadataMangerImpl.java
 *
 * Version information:1.00
 *
 * Data:2012-01-25
 *
 * COPYRIGHT (C) 2012 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
/**
 * @author John Zhao
 *
 */
public class DBMetadataMangerImpl implements DBMetadataManger {
    private static final Logger itsLogger = Logger.getLogger(DBMetadataMangerImpl.class);

    private HashMap<DbConnection, DataBaseMetaInfo> metaMap = null;
	private HashMap<DbConnection, String> typeMap = null;

	protected DBMetadataMangerImpl() {
		metaMap = new HashMap<DbConnection, DataBaseMetaInfo>();
		typeMap = new HashMap<DbConnection, String> ();
	}

	@Override
	public DataBaseMetaInfo getDataBaseMetaInfo(DbConnection connectionInfo)
			throws Exception {
		if (metaMap.containsKey(connectionInfo) == false) {
			initDBConn(connectionInfo);
			refreshDBTypeCache(connectionInfo);
		}
		return metaMap.get(connectionInfo);
	}

	@Override
	public DBSchemaMetaInfo getDBSchemaMetaInfo(DbConnection connectionInfo,
			String schemaName) throws Exception {
		if (metaMap.containsKey(connectionInfo) == false) {
			initDBConn(connectionInfo);
		}
		DBSchemaMetaInfo dbSchemaMetaInfo = null;
		DataBaseMetaInfo dbMetaInfo = metaMap.get(connectionInfo);
		if (dbMetaInfo != null && dbMetaInfo.getSchemaInfos() != null) {
			for (Iterator<DBSchemaMetaInfo> iterator = dbMetaInfo
					.getSchemaInfos().iterator(); iterator.hasNext();) {
				DBSchemaMetaInfo temp = (DBSchemaMetaInfo) iterator.next();
				if (temp.getSchemaName().equals(schemaName)) {
					dbSchemaMetaInfo = temp;
					break;
				}

			}
		}
		// first time to load this schema
		if (dbSchemaMetaInfo == null) {
			DBMetaDataUtil dmd = new DBMetaDataUtil(connectionInfo);
//			dmd.setJudgeConnection(true) ;
			dmd.setLocale(Locale.getDefault());
			try {
				dbSchemaMetaInfo = initSchema(connectionInfo, dmd, schemaName);
			} catch (Exception e) {

			} finally {
				dmd.disconnect();
			}

		}
		return dbSchemaMetaInfo;
	}

	@Override
	public List<String> getSchemaList(DbConnection connectionInfo)
			throws Exception {
		List<String> schemaList = null;
		if (metaMap.containsKey(connectionInfo) == false) {
			initDBConn(connectionInfo);
		}

		DataBaseMetaInfo dbMetaInfo = metaMap.get(connectionInfo);
		if (dbMetaInfo != null) {
			schemaList = dbMetaInfo.getSchemaNameList();
		}else{
			schemaList = 	new ArrayList<String>();
		}

		return schemaList;
	}

	private void initDBConn(DbConnection connectionInfo) throws Exception {

		DataBaseMetaInfo dbMetaInfo = new DataBaseMetaInfo(connectionInfo
				.clone());
		metaMap.put(connectionInfo.clone(), dbMetaInfo);
		dbMetaInfo.refresh(false,true);

	}

	/**
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public List<DBTableMetaInfo> getTableInfoList(DbConnection connectionInfo, 
			String shecmaName) throws Exception {
		List<DBTableMetaInfo> result = new ArrayList<DBTableMetaInfo>();

		DBSchemaMetaInfo dbSchemaMetaInfo = getDBSchemaMetaInfo(connectionInfo,
				shecmaName);

		if (dbSchemaMetaInfo != null) {
			if (dbSchemaMetaInfo.getTableContainer() != null
					&& dbSchemaMetaInfo.getTableContainer().getTables() != null) {
				result.addAll(dbSchemaMetaInfo.getTableContainer().getTables());
			}
			if (dbSchemaMetaInfo.getViewContainer() != null
					&& dbSchemaMetaInfo.getViewContainer().getTables() != null) {
				result.addAll(dbSchemaMetaInfo.getViewContainer().getTables());
			}

		}

		return result;
	}

	@Override
	public List<String> getTableAndViewNameList(DbConnection connectionInfo,
			String schemaName) throws Exception {

		List<String> tableList = new ArrayList<String>();
		DBSchemaMetaInfo schema = getDBSchemaMetaInfo(connectionInfo,
				schemaName);
		
		if(schema.getTableContainer()==null){
			schema.refresh(connectionInfo,true);
		}
		tableList = Arrays.asList(schema.getTableNameArray());
		return tableList;
	}
 

	@Override
	public void refreshDBConnection(DbConnection dbc, boolean recursive,boolean forceReconn)
			throws Exception {
		if (metaMap.containsKey(dbc) == false) {
			initDBConn(dbc);
			refreshDBTypeCache(dbc);
		} else {
			DataBaseMetaInfo info = metaMap.get(dbc);
			info.refresh(recursive,forceReconn);
			refreshDBTypeCache(dbc);
		}

	}

	@Override
	public void refreshSchema(DbConnection dbc, String schemaName,boolean forceReconn)
			throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
		dmd.setLocale(Locale.getDefault());

		try {
			DBSchemaMetaInfo schemaInfo = getDBSchemaMetaInfo(dbc, schemaName);
			if (schemaInfo == null) {// schema does not exist in cache,create a
				// new one and refresh it
				schemaInfo = initSchema(dbc, dmd, schemaName);
			} else {
				schemaInfo.refresh(dbc, dmd,forceReconn);
			}

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	private DBSchemaMetaInfo initSchema(DbConnection dbc, DBMetaDataUtil dmd,
			String schemaName) throws Exception {
		// make sure the dbmetainfo exists
		if (metaMap.containsKey(dbc) == false) {
			initDBConn(dbc);
		}

		DBSchemaMetaInfo schemaInfo = new DBSchemaMetaInfo(dbc.getConnName(),
				schemaName);
		DataBaseMetaInfo dbMeta = metaMap.get(dbc);
		dbMeta.addSchemaInfo(schemaInfo);
		schemaInfo.refresh(dbc, dmd,true);
		return schemaInfo;
	}

	public void refreshTable(DbConnection dbc, DBTableMetaInfo table,boolean forceReconn)
			throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
		dmd.setJudgeConnection(!forceReconn);
		dmd.setLocale(Locale.getDefault());

		try {

			try {
				List<TableColumnMetaInfo> columns = createColumnList(table
						.getSchemaName(), table.getTableName(), dmd);

				table.setColumns(columns);

			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				throw e;
			} finally {
				dmd.disconnect();
			}

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	private List<TableColumnMetaInfo> createColumnList(String schemaName,
			String tableName, DBMetaDataUtil dmd) throws Exception {
		List<TableColumnMetaInfo> colInfos = new ArrayList<TableColumnMetaInfo>();
		ArrayList<String[]> columns = dmd.getAllColumnList(schemaName,
				tableName);
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			String[] cols = (String[]) iterator.next();
			TableColumnMetaInfo colInfo = new TableColumnMetaInfo(cols[0],
					cols[1]);
			colInfos.add(colInfo);

		}

		return colInfos;
	}

	@Override
	public void refreshSchema(DbConnection dbc, DBSchemaMetaInfo schemaInfo,boolean forceReconn)
			throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
		dmd.setLocale(Locale.getDefault());

		try {
			if (schemaInfo != null) {// schema does not exist in cache,create a
				// new one and refresh it

				schemaInfo.refresh(dbc, dmd,forceReconn);
			}

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	@Override
	public void refreshTable(DbConnection connectionInfo, String schemaName,
			String tableName,boolean forceReconn) throws Exception {
		DBSchemaMetaInfo schemaInfo = getDBSchemaMetaInfo(connectionInfo,
				schemaName);
		DBTableMetaInfo tableInfo = getDBTableMetaInfo(schemaInfo, tableName);
		if(tableInfo!=null){
			refreshTable(connectionInfo, tableInfo,forceReconn);
		}

	}

	private DBTableMetaInfo getDBTableMetaInfo(DBSchemaMetaInfo schema,
			String tableName) {
		DBTableContainer tableContainer = schema.getTableContainer();

		DBTableMetaInfo tableInfo = tableContainer
				.getDBTableMetaInfo(tableName);
		if (tableInfo == null) {
			tableContainer = schema.getViewContainer();

			tableInfo = tableContainer.getDBTableMetaInfo(tableName);
		}
		return tableInfo;
	}
	
	@Override
	public List<TableColumnMetaInfo> getTableColumnInfoList(
			DbConnection connectionInfo, String schemaName, String tableName)
			throws Exception {
		DBSchemaMetaInfo dbSchemaMetaInfo = getDBSchemaMetaInfo(connectionInfo,
				schemaName);

		if(dbSchemaMetaInfo.getTableContainer()==null){
			refreshSchema(connectionInfo,schemaName,false) ;
		}
		DBTableMetaInfo tableInfo = dbSchemaMetaInfo
				.getDBTableMetaInfo(tableName);
		
		if (tableInfo != null) {
			if (tableInfo.getColumns() == null) {
				refreshTable(connectionInfo, tableInfo,true);
			}
			return tableInfo.getColumns();
		}

		return new ArrayList<TableColumnMetaInfo>();
	}
	
	@Override
	//this is for the clear temp table
	public void removeTableFromCache(DbConnection connectionInfo,String schemaName,String tableName){
 
		DataBaseMetaInfo dbMetaInfo = metaMap.get(connectionInfo);
		if(dbMetaInfo!=null){
			 List<DBSchemaMetaInfo> schemaList = dbMetaInfo.getSchemaInfos();
			 for (Iterator iterator = schemaList.iterator(); iterator.hasNext();) {
				DBSchemaMetaInfo dbSchemaMetaInfo = (DBSchemaMetaInfo) iterator
						.next();
				if(dbSchemaMetaInfo.getSchemaName().equals(schemaName)){
					dbSchemaMetaInfo.removeTable(tableName);
					break;
				}
				
			}
			
		}
	}

	@Override
	public void refreshSchemaTables(DbConnection connectionInfo,
			String schemaName, boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(connectionInfo);
		dmd.setLocale(Locale.getDefault());

		try {
			DBSchemaMetaInfo schemaInfo = getDBSchemaMetaInfo(connectionInfo, schemaName);
			if (schemaInfo == null) {// schema does not exist in cache,create a
				// new one and refresh it
				schemaInfo = initSchemaTables(connectionInfo, dmd, schemaName);
			} else {
				schemaInfo.refreshTables(connectionInfo, dmd,forceReconn);
			}

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}	
	}

	private DBSchemaMetaInfo initSchemaTables(DbConnection dbc,
			DBMetaDataUtil dmd, String schemaName) throws Exception {
		// make sure the dbmetainfo exists
		if (metaMap.containsKey(dbc) == false) {
			initDBConn(dbc);
		}

		DBSchemaMetaInfo schemaInfo = new DBSchemaMetaInfo(dbc.getConnName(),
				schemaName);
		DataBaseMetaInfo dbMeta = metaMap.get(dbc);
		dbMeta.addSchemaInfo(schemaInfo);
		schemaInfo.refreshTables(dbc, dmd,true);
		return schemaInfo;
	}

	@Override
	public void refreshSchemaTables(DbConnection dbc,
			DBSchemaMetaInfo schemaInfo, boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
		dmd.setLocale(Locale.getDefault());

		try {
			if (schemaInfo != null) {// schema does not exist in cache,create a
				// new one and refresh it
				schemaInfo.refreshTables(dbc, dmd,forceReconn);
			}
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	@Override
	public void refreshSchemaViews(DbConnection connectionInfo,
			String schemaName, boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(connectionInfo);
		dmd.setLocale(Locale.getDefault());

		try {
			DBSchemaMetaInfo schemaInfo = getDBSchemaMetaInfo(connectionInfo, schemaName);
			if (schemaInfo == null) {// schema does not exist in cache,create a
				// new one and refresh it
				schemaInfo = initSchemaViews(connectionInfo, dmd, schemaName);
			} else {
				schemaInfo.refreshViews(connectionInfo, dmd,forceReconn);
			}

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	private DBSchemaMetaInfo initSchemaViews(DbConnection dbc,
			DBMetaDataUtil dmd, String schemaName) throws Exception {
		// make sure the dbmetainfo exists
		if (metaMap.containsKey(dbc) == false) {
			initDBConn(dbc);
		}

		DBSchemaMetaInfo schemaInfo = new DBSchemaMetaInfo(dbc.getConnName(),
				schemaName);
		DataBaseMetaInfo dbMeta = metaMap.get(dbc);
		dbMeta.addSchemaInfo(schemaInfo);
		schemaInfo.refreshViews(dbc, dmd,true);
		return schemaInfo;
	}

	@Override
	public void refreshSchemaViews(DbConnection dbc,
			DBSchemaMetaInfo schemaInfo, boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
		dmd.setLocale(Locale.getDefault());

		try {
			if (schemaInfo != null) {// schema does not exist in cache,create a
				// new one and refresh it
				schemaInfo.refreshViews(dbc, dmd,forceReconn);
			}
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
	}

	public boolean isGreenPlumDataBase(DbConnection dbConn) throws Exception{
		if(DataSourceInfoGreenplum.dBType.equals(typeMap.get(dbConn))){
			return true;
		}else if (typeMap.get(dbConn)==null){
			refreshDBTypeCache(dbConn);
			return DataSourceInfoGreenplum.dBType.equals(typeMap.get(dbConn));
		}else{
			return false;
		} 
		
	}

	private void refreshDBTypeCache(DbConnection dbConn) throws Exception {
		if(dbConn.getDbType().equals(DataSourceInfoPostgres.dBType)&& AlpineUtil.isGreenplum(
				AlpineUtil.createConnection( dbConn ,Locale.getDefault()) ) 
				) {
			typeMap.put(dbConn, DataSourceInfoGreenplum.dBType);
		}
		else{	
			typeMap.put(dbConn, dbConn.getDbType());
		}
	}
}
