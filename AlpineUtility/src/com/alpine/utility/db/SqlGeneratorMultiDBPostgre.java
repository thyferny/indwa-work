/**
 * ClassName  SqlGeneratorMultiDBPostgre.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

public class SqlGeneratorMultiDBPostgre extends
		AbstractSqlGeneratorMultiDBGPPG {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6279093250682991035L;

	@Override
	public String setCreateTableEndingSql(String distributeColumns) {
		return " ";
	}

	@Override
	public String insertTable(String resTable, String decTable) {
		return "";
	}
	@Override
	public String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel){
		return "";
	}
}
