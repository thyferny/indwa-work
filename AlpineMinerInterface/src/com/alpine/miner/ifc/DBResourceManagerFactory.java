/**
 * ClassName DBResourceManagerFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-22
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.ifc;

public class DBResourceManagerFactory {
	public static final DBResourceManagerFactory INSTANCE=new DBResourceManagerFactory();
	private  DBResourceManagerIfc manager = null;
	private DBResourceManagerFactory(){
		
	}
	public  boolean registerDBResourceManager(DBResourceManagerIfc manager){
		this.manager=manager;
		return true;
	}
	public DBResourceManagerIfc getManager() {
		return manager;
	}

}
