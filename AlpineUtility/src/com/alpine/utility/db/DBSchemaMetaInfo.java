/**
 * Classname DBTableMetalInfo.java
 *
 * Version information:1.00
 *
 * Data:2010-6-20
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;


/**
 * @author Administrator
 *
 */
public class DBSchemaMetaInfo {
    private static final Logger itsLogger = Logger.getLogger(DBSchemaMetaInfo.class);

    String schemaName;
	String connName;
	public String getConnName() {
		return connName;
	}
	public void setConnName(String connName) {
		this.connName = connName;
	}
	DBTableContainer tableContainer=null; 
	DBViewContainer viewContainer=null;
	private String[] tableNameArray=new String[0];
	private String[] tableTypeArray=new String[0];
	
	public DBSchemaMetaInfo(String connName ,String schemaName) { 
		this.connName=connName;
		this.schemaName=schemaName;
	}
	public DBTableContainer getTableContainer() {
		return tableContainer;
	}
	public void setTableContainer(DBTableContainer tableContainer) {
		this.tableContainer = tableContainer;
	}
	public DBViewContainer getViewContainer() {
		return viewContainer;
	}
	public void setViewContainer(DBViewContainer viewContainer) {
		this.viewContainer = viewContainer;
	}
 
 
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	/**
	 * @param forceInit
	 * @param tableName
	 * @return
	 */
	public DBTableMetaInfo getDBTableMetaInfo( 
			String tableName) {
		
		if(tableContainer.getDBTableMetaInfo(tableName)!=null){
			return tableContainer.getDBTableMetaInfo(tableName);
		}
		else{
			return viewContainer.getDBTableMetaInfo(tableName);
		}
		 
	}
	/**
	 * @return
	 */
	public String[] getTableNameArray() {
		return tableNameArray;
	}
	/**
	 * 
	 */
	public void reBuildTableNameArray() {
		
		List<DBTableMetaInfo> result= new ArrayList <DBTableMetaInfo>();
  
			if (getTableContainer() != null
					&& getTableContainer().getTables() != null) {
				result.addAll(getTableContainer().getTables());
			}
			if (getViewContainer() != null
					&& getViewContainer().getTables() != null) {
				result.addAll(getViewContainer().getTables());
			}

	 
		
	 
		tableNameArray=new String[result.size()];
		tableTypeArray =new String[result.size()];
		int i=0;
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			DBTableMetaInfo dbTableMetaInfo = (DBTableMetaInfo) iterator.next();
			tableNameArray[i]=dbTableMetaInfo.getTableName();
			tableTypeArray[i]=dbTableMetaInfo.getTableType();
			i++;
		}
	
		                          
	}
	public String[] getTableTypeArray() {
		return tableTypeArray;
	}
	
	public void refresh(DbConnection dbc, DBMetaDataUtil dmd ,boolean forceReconn) throws Exception {
		String schema = this.getSchemaName();
		dmd.setJudgeConnection(!forceReconn);
		
		generateTableContainer(dbc, dmd, schema);

		generateViewContainer(dbc, dmd, schema);

		this.reBuildTableNameArray();
	 
		
	}
	private void generateViewContainer(DbConnection dbc, DBMetaDataUtil dmd,
			String schema) throws Exception {
		ArrayList<String> newViews = new ArrayList<String>();
		String filterPattern = null;
		newViews = dmd.getAllViewList(schema, filterPattern);

		if (this.getViewContainer() == null) {
			DBViewContainer viewContainer = creatDBViewContainer(schema,
					newViews, dmd, dbc.getConnName());
			this.setViewContainer(viewContainer);
		} else {
			// add or remove ...
			DBViewContainer viewContainer = this.getViewContainer();
			List<String> oldNameList = viewContainer.getViewNameList();
			List<String> shouldRemove = new ArrayList<String>();
			List<String> shouldAdd = new ArrayList<String>();
			for (Iterator iterator = newViews.iterator(); iterator.hasNext();) {
				String newViewName = (String) iterator.next();
				if (oldNameList.contains(newViewName) == false) {
					shouldAdd.add(newViewName);
				}
			}

			for (Iterator iterator = oldNameList.iterator(); iterator.hasNext();) {
				String oldTableName = (String) iterator.next();
				if (newViews.contains(oldTableName) == false) {
					shouldRemove.add(oldTableName);
				}
			}
			viewContainer.removeAll(shouldRemove);
			viewContainer.addAll(shouldAdd, schema, dbc.getConnName());

		}
	}
	private void generateTableContainer(DbConnection dbc, DBMetaDataUtil dmd,
			String schema) throws Exception {
		ArrayList<String> newTables = new ArrayList<String>();
		if (dbc.getDbType().equals(DataSourceInfoPostgres.dBType)
				|| dbc.getDbType().equals(DataSourceInfoGreenplum.dBType)) {
			newTables = dmd.getPGTableList(schema);
		} else {
			String filterPattern = null;
			if(dbc.getDbType().equals(DataSourceInfoOracle.dBType)){
				filterPattern = "^BIN\\$........................\\$0$";
			}
			newTables = dmd.getAllTableList(schema, filterPattern);
		}

		if (this.getTableContainer() == null) {
			DBTableContainer tableContainer = creatDBTableContainer(schema,
					newTables,   dbc.getConnName());
			this.setTableContainer(tableContainer);
		} else {
			// add or remove ...
			DBTableContainer tableContainer = this.getTableContainer();
			List<String> oldNameList = tableContainer.getTableNameList();
			List<String> shouldRemove = new ArrayList<String>();
			List<String> shouldAdd = new ArrayList<String>();
			for (Iterator iterator = newTables.iterator(); iterator.hasNext();) {
				String newTableName = (String) iterator.next();
				if (oldNameList.contains(newTableName) == false) {
					shouldAdd.add(newTableName);
				}
			}

			for (Iterator iterator = oldNameList.iterator(); iterator.hasNext();) {
				String oldTableName = (String) iterator.next();
				if (newTables.contains(oldTableName) == false) {
					shouldRemove.add(oldTableName);
				}
			}
			tableContainer.removeAll(shouldRemove);
			tableContainer.addAll(shouldAdd, schema, dbc.getConnName());

		}
	}
	 

	private DBTableContainer creatDBTableContainer(String schema,
			ArrayList<String> tables , String connName)
			throws Exception {
		DBTableContainer dbvc = new DBTableContainer();
		dbvc.setSchemaMetaInfo(this);
		List<DBTableMetaInfo> tableInfos = new ArrayList<DBTableMetaInfo>();
		for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
			String tableName = (String) iterator.next();
			DBTableMetaInfo table = new DBTableMetaInfo();
			table.setSchemaName(schema);
			table.setTableName(tableName);
			table.setTableType(DBMetaDataUtil.TABLE_TYPE_TABLE);
			table.setConnectionName(connName);

			tableInfos.add(table);

		}

		dbvc.setTables(tableInfos);
		return dbvc;
	}

	private DBViewContainer creatDBViewContainer(String schema,
			ArrayList<String> views, DBMetaDataUtil dmd, String connName)
			throws Exception {
		DBViewContainer dbvc = new DBViewContainer();
		dbvc.setSchemaMetaInfo(this);
		List<DBTableMetaInfo> tables = new ArrayList<DBTableMetaInfo>();
		for (Iterator iterator = views.iterator(); iterator.hasNext();) {
			String view = (String) iterator.next();
			DBTableMetaInfo table = new DBViewMetaInfo();
			table.setSchemaName(schema);
			table.setTableName(view);
			table.setTableType(DBMetaDataUtil.TABLE_TYPE_VIEW);
			table.setConnectionName(connName);

			tables.add(table);

		}

		dbvc.setTables(tables);
		return dbvc;
	}
	
	public void refresh(DbConnection dbc,boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbc);
	
		dmd.setLocale(Locale.getDefault());
		try {
		 
				this.refresh(dbc, dmd,forceReconn);
			

		} catch (Exception e) {

			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			dmd.disconnect();
		}
		
	}
	public void removeTable(String tableName) {
		//
		if(getTableContainer()!=null){
			getTableContainer().removeTable(tableName);
		}
		if(getViewContainer()!=null){
			getViewContainer().removeTable(tableName);
		}
		reBuildTableNameArray();
		
	}
	public void refreshTables(DbConnection dbc, DBMetaDataUtil dmd, boolean forceReconn) throws Exception {
		String schema = this.getSchemaName();
		dmd.setJudgeConnection(!forceReconn);
		
		generateTableContainer(dbc, dmd, schema);

		this.reBuildTableNameArray();
	}
	
	public void refreshViews(DbConnection dbc, DBMetaDataUtil dmd, boolean forceReconn) throws Exception {
		String schema = this.getSchemaName();
		dmd.setJudgeConnection(!forceReconn);
		
		generateViewContainer(dbc, dmd, schema);

		this.reBuildTableNameArray();
	}
	
}
