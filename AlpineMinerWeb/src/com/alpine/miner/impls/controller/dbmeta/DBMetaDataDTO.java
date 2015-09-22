package com.alpine.miner.impls.controller.dbmeta;

import java.util.List;

public class DBMetaDataDTO {
	public DBMetaDataDTO(String name, String type, String connectionName,
			String schemaName) {
		this.name = name;
		this.type = type;
		this.connectionName = connectionName;
		this.schemaName = schemaName;
	}

	public static final String TYPE_DB="db";
	public static final String TYPE_SCHEMA="sc";
	public static final String TYPE_VIEWCONTAINER="vc";
	public static final String TYPE_TABLECONTAINER="tc";
	public static final String TYPE_TABLE="tb";
	public static final String TYPE_VIEW="vi";
	//private static String TYPE_TABLECOLUMN="cl";
	String type;//db/schema/viewcontainer/tablecontainer/table
	String name;//
	String connectionName;//this is for schema
	String schemaName ;//this is for table
	List<DBMetaDataDTO> children = null;
	
	
	public List<DBMetaDataDTO> getChildren() {
		return children;
	}

	public void setChildren(List<DBMetaDataDTO> children) {
		this.children = children;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getConnectionName() {
		return connectionName;
	}
	
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
 
}
