/**
 * ClassName :ModelInfo.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.resource;

/**
 *  ModelInfo is a pure property file
 * @author zhaoyong
 *
 */
public class JDBCDriverInfo extends ResourceInfo{
	public static final String Driver_NAME = "driver";//like ojdbc6.jar
  	// pure file name 
	String driverName;
	
	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public JDBCDriverInfo(String userName,ResourceType type,String id,String driverName){
		super(userName, id, type) ;
		this.driverName=driverName;
		 
	}
	
	public JDBCDriverInfo(){
		
	}
	  
}
