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

import com.alpine.utility.db.DbConnection;

/**
 * @author sam_zang
 *
 */
public class DbConnectionInfo extends DataSourceInfo {
	public DbConnectionInfo(String user, String name, ResourceType type) {
		super(user, name, type);
	}
	
	/**
	 * 
	 */
	public DbConnectionInfo() {
		super();
	}

	private DbConnection connection;

	/**
	 * @return the connection
	 */
	public DbConnection getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(DbConnection connection) {
		this.connection = connection;
	}


}
