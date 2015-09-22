/**
 * ClassName  DataSourceInfoFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.Connection;

public class DataSourceInfoFactory {
	private static DataSourceInfoPostgres dataSourcePG= new DataSourceInfoPostgres();
	private static DataSourceInfoGreenplum dataSourceGP= new DataSourceInfoGreenplum();
	private static DataSourceInfoOracle dataSourceOracle = new DataSourceInfoOracle();
	private static DataSourceInfoDB2 dataSourceDB2 = new DataSourceInfoDB2();
	private static DataSourceInfoNZ dataSourceNZ = new DataSourceInfoNZ();
	
	
	public static IDataSourceInfo createConnectionInfo(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return dataSourcePG;
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return dataSourceOracle;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return dataSourceGP;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return dataSourceDB2;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return  dataSourceNZ;
		}
		else {
			return dataSourceGP;
		}
	}
	public static IDataSourceInfo createConnectionInfo(String dBType, Connection conn){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			if(AlpineUtil.isGreenplum(conn)){
				return dataSourceGP;
			}else{
				return dataSourcePG;
			}
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return dataSourceOracle;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return dataSourceDB2;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return dataSourceNZ;
		}
		else{
			return dataSourceGP;
		}
	}
}
