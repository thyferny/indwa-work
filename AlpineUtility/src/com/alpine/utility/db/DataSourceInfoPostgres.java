/**
 * ClassName  DataSourceInfoPostgres.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;


public class DataSourceInfoPostgres extends AbstractDataSourceInfoPGGP {
	private static final long serialVersionUID = -6503792197142859893L;
	public static final String dBType = "PostgreSQL";
	static{
		systemSchema.add("pg_toast_temp_\\d+");
	}

	@Override
	public String getDBDriver() {
		return dBDriver;
	}
	@Override
	public String getDBType() {
		return dBType;
	}

}
