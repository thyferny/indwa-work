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

public class HadoopConnectionManagerFactory {
	public static final HadoopConnectionManagerFactory INSTANCE=new HadoopConnectionManagerFactory();
	private  HadoopConnectionManagerIfc manager = null;
	private HadoopConnectionManagerFactory(){
		
	}
	public  boolean registerHadoopResourceManager(HadoopConnectionManagerIfc manager){
		this.manager=manager;
		return true;
	}
	public HadoopConnectionManagerIfc getManager() {
		return manager;
	}

}
