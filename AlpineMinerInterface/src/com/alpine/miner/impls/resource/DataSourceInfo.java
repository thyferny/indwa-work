/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FlowInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */
 
package com.alpine.miner.impls.resource;


/**
 * @author sam_zang
 *
 */
public abstract class DataSourceInfo extends ResourceInfo {
	public static final String DATASOURCE_TYOE_HADDOP = "HADOOP";
	public static final String DATASOURCE_TYOE_DATABASE = "DATABASE";
	
	private String datasourceType = null;

	public DataSourceInfo(String user, String name, ResourceType type) {
		super(user, name, type);
	}
	
	/**
	 * 
	 */
	public DataSourceInfo() {
		super();
	}
	
	/**
	 * @return the datasourceType
	 */
	public String getDatasourceType() {
		return datasourceType;
	}

	/**
	 * @param datasourceType the datasourceType to set
	 */
	public void setDatasourceType(String datasourceType) {
		this.datasourceType = datasourceType;
	}
}
