/**
 * ClassName  DataBaseMetaInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * @author John Zhao
 * 
 */
public class DataBaseMetaInfo {

    private static final Logger itsLogger = Logger.getLogger(DataBaseMetaInfo.class);
    DbConnection dbConn = null;
	List<DBSchemaMetaInfo> schemaInfos = new ArrayList<DBSchemaMetaInfo> ();
	public List<DBSchemaMetaInfo> getSchemaInfos() {
		return schemaInfos;
	}

	public List<String> getSchemaNameList() {
		return schemaNameList;
	}

	List<String> schemaNameList = new ArrayList<String>();

	public DataBaseMetaInfo(DbConnection dbConn) {
		this.dbConn = dbConn;

	}

	// if recursive=false, will only refresh the schema name
	// if recursive=true, will refresh the schemainfo with it's own tables
	public void refresh(boolean recursive,boolean forceReconn) throws Exception {
		DBMetaDataUtil dmd = new DBMetaDataUtil(dbConn);
		dmd.setJudgeConnection(!forceReconn) ;
		dmd.setLocale(Locale.getDefault());
		try {
			ArrayList<String> newSchemas = dmd.getSchemaList();
			if (schemaInfos == null) { // first time ...
				
				schemaInfos = new ArrayList<DBSchemaMetaInfo>();
				for (Iterator iterator = newSchemas.iterator(); iterator
						.hasNext();) {
					// avoid the system schema
					String schema = (String) iterator.next();
					boolean contain = containSchema(dbConn.getDbType(), dmd
							.getConnection(), schema);

					if (contain == false) {

						DBSchemaMetaInfo schemaInfo = new DBSchemaMetaInfo(
								dbConn.getConnName(), schema);

						schemaInfos.add(schemaInfo);
					}
				}

			} else {
				List<String> oldSchemaNameList = new ArrayList<String>();

				if (schemaInfos != null && schemaInfos.size() > 0) {
					for (Iterator iterator = schemaInfos.iterator(); iterator
							.hasNext();) {
						DBSchemaMetaInfo schemaInfo = (DBSchemaMetaInfo) iterator
								.next();
						oldSchemaNameList.add(schemaInfo.getSchemaName());

					}

				}
				for (Iterator iterator = newSchemas.iterator(); iterator
						.hasNext();) {
					String newSchema = (String) iterator.next();

					boolean contain = containSchema(dbConn.getDbType(), dmd
							.getConnection(), newSchema);
					if (contain == false) {
						if (oldSchemaNameList.contains(newSchema) == false) {
							DBSchemaMetaInfo schemaInfo = new DBSchemaMetaInfo(
									dbConn.getConnName(), newSchema);

							schemaInfos.add(schemaInfo);
						}
					}
				}
				List<DBSchemaMetaInfo> shouldRemove = new ArrayList<DBSchemaMetaInfo>();
				for (Iterator iterator = schemaInfos.iterator(); iterator
						.hasNext();) {
					DBSchemaMetaInfo schemaInfo = (DBSchemaMetaInfo) iterator
							.next();
					String oldSchema = (String) schemaInfo.getSchemaName();
					if (newSchemas.contains(oldSchema) == false) {
						shouldRemove.add(schemaInfo);
					}

				}
				schemaInfos.removeAll(shouldRemove);
			}
			if (recursive == true) {
				for (Iterator iterator = schemaInfos.iterator(); iterator
						.hasNext();) {
					DBSchemaMetaInfo dbSchemaMetaInfo = (DBSchemaMetaInfo) iterator
							.next();
					dbSchemaMetaInfo.refresh(dbConn, dmd,forceReconn);

				}
			}

		} catch (Exception e) {
			schemaInfos=new ArrayList<DBSchemaMetaInfo>();
			itsLogger.error(e.getMessage(),e);
			throw e;

		} finally {
			dmd.disconnect();
		}

		reBuildSchemaNameList();
	}

	private boolean containSchema(String dbType, Connection connection,
			String schema) {
		IDataSourceInfo connInfo = DataSourceInfoFactory.createConnectionInfo(
				dbType, connection);
		List<String> systemSchemas = connInfo.getSystemSchema();
		for (int i = 0; i < systemSchemas.size(); i++) {
			String systemSchema = systemSchemas.get(i);
			if (schema.matches(systemSchema)) {
				return true;
			}
		}
		return false;
	}

	private void reBuildSchemaNameList() {

		schemaNameList = new ArrayList<String>();
		if (schemaInfos != null)
			//
			for (Iterator<DBSchemaMetaInfo> iterator = schemaInfos.iterator(); iterator
					.hasNext();) {
				DBSchemaMetaInfo schemaInfo = iterator.next();
				if (schemaInfo != null) {
					schemaNameList.add(schemaInfo.getSchemaName());
				}
			}

	}

	public void addSchemaInfo(DBSchemaMetaInfo schemaInfo) {
		if(schemaInfos==null){
			schemaInfos = new ArrayList<DBSchemaMetaInfo> ();
		}	
		schemaInfos.add(schemaInfo) ;	
	}
	
	public void setSchemaInfos(List<DBSchemaMetaInfo> schemaInfos){
		this.schemaInfos=schemaInfos;
	}
}
