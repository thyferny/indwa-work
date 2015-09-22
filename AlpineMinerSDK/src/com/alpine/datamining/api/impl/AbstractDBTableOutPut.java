/**
 * ClassName  AbstractDBTableOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-7-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.List;

import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author John Zhao
 *
 */
public class AbstractDBTableOutPut extends AbstractAnalyzerOutPut {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6336170517775203171L;
	private DataBaseInfo dbInfo;
	private boolean hadoopFile;


	public boolean isHadoopFile() {
		return hadoopFile;
	}

	public void setHadoopFileFlag(boolean hadoopFile) {
		this.hadoopFile = hadoopFile;
	}

	private String schemaName;
	private String tableName;
	
 
	List<TableColumnMetaInfo> columns;
	
	public List<TableColumnMetaInfo> getColumns() {
		return columns;
	}

	public void setColumns(List<TableColumnMetaInfo> columns) {
		this.columns = columns;
	}
 

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public DataBaseInfo getDbInfo() {
		return dbInfo;
	}

	public void setDbInfo(DataBaseInfo dbInfo) {
		this.dbInfo = dbInfo;
	}

	 
}
