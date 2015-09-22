/**
 * ClassName  SqlGeneratorMultiDBFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

public class SqlGeneratorMultiDBFactory {
	public static ISqlGeneratorMultiDB createConnectionInfo(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new SqlGeneratorMultiDBPostgre();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new SqlGeneratorMultiDBOracle();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new SqlGeneratorMultiDBGreenplum();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new SqlGeneratorMultiDBDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return new SqlGeneratorMultiDBNZ();
		}
		else
		{
			return new SqlGeneratorMultiDBGreenplum();
		}
	}
}
