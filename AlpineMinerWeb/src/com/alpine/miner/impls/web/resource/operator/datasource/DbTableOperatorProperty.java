/**
 * 
 */
package com.alpine.miner.impls.web.resource.operator.datasource;

/**
 * ClassName: DbTableOperatorProperty.java
 * <p/>
 * Data: 2012-7-7
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class DbTableOperatorProperty {
	
	private static final String OPERATOR_CLASS = "DbTableOperator";

	private String 	connectionName,
					schemaName,
					entityName,
					operatorClass = OPERATOR_CLASS;//because gson convert json base on field. So sick.

	public DbTableOperatorProperty() {
		
	}
	
	public DbTableOperatorProperty(String connName, String schemaName, String entityName){
		this.connectionName = connName;
		this.schemaName = schemaName;
		this.entityName = entityName;
	}

	/**
	 * @return the connectionName
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @param connectionName the connectionName to set
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * @param entityName the entityName to set
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

}
