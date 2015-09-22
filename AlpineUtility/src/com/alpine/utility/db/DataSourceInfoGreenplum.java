/**
 * ClassName  DataSourceInfoGreenplum.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;



public class DataSourceInfoGreenplum extends AbstractDataSourceInfoPGGP {

	private static final long serialVersionUID = 2974068867213916335L;
	public static final String dBType = "Greenplum";

	@Override
	public String getDBDriver() {
		return dBDriver;
	}
	@Override
	public String getDBType() {
		return dBType;
	}

}
