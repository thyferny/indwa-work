/**
 * ClassName  MultiDBUtilityFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

public class MultiDBUtilityFactory {

	public static IMultiDBUtility createConnectionInfo(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new MultiDBPostgreUtility();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new MultiDBOracleUtility();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new MultiDBGreenplumUtility();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new MultiDBDB2Utility();
		}
		else
		{
			return new MultiDBGreenplumUtility();
		}
	}
	
}
