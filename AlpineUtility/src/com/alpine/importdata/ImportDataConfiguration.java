/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataConfiguration.java
 */
package com.alpine.importdata;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.db.DbConnection;

/**
 * @author Gary
 * Aug 13, 2012
 */
public class ImportDataConfiguration {

	private DbConnection connectionInfo;
	
	private String 	schemaName,
					tableName;
					
	private char delimiter,
				 quote,
				 escape;
	private int limitNum;
	
	private boolean includeHeader;
	private List<ColumnStructure> structure = new ArrayList<ColumnStructure>();
	
	public static class ColumnStructure{
		private String columnName;
		private DatabaseDataType columnType;
		private boolean isInclude,
						allowEmpty;
		
		public ColumnStructure(String columnName, DatabaseDataType columnType, boolean isInclude, boolean allowEmpty) {
			this.columnName = columnName;
			this.columnType = columnType;
			this.isInclude = isInclude;
			this.allowEmpty = allowEmpty;
		}
		
		public String getColumnName() {
			return columnName;
		}
		public DatabaseDataType getColumnType() {
			return columnType;
		}
		public boolean isInclude() {
			return isInclude;
		}
		public boolean isAllowEmpty() {
			return allowEmpty;
		}
	}
	
	public void addColumnStructure(ColumnStructure structure){
		this.structure.add(structure);
	}

	public ColumnStructure getColumnStructure(int index){
		return this.structure.get(index);
	}
	
	public List<ColumnStructure> getColumnInfo(){
		return this.structure;
	}

	public String getSchemaName() {
		return schemaName;
	}


	public String getTableName() {
		return tableName;
	}


	public char getDelimiter() {
		return delimiter;
	}


	public char getQuote() {
		return quote;
	}


	public char getEscape() {
		return escape;
	}


	public boolean isIncludeHeader() {
		return includeHeader;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}


	public void setQuote(char quote) {
		this.quote = quote;
	}


	public void setEscape(char escape) {
		this.escape = escape;
	}

	public void setIncludeHeader(boolean includeHeader) {
		this.includeHeader = includeHeader;
	}


	public int getLimitNum() {
		return limitNum;
	}


	public void setLimitNum(int limitNum) {
		this.limitNum = limitNum;
	}

	public DbConnection getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(DbConnection connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
}
