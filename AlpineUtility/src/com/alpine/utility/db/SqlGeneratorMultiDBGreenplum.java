/**
 * ClassName  SqlGeneratorMultiDBGreenplum.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import com.alpine.utility.file.StringUtil;

public class SqlGeneratorMultiDBGreenplum extends
		AbstractSqlGeneratorMultiDBGPPG {

	private static final long serialVersionUID = -6279093250682991035L;

	@Override
	public String setCreateTableEndingSql(String distributeColumns) {
		if(StringUtil.isEmpty(distributeColumns)){
			return " DISTRIBUTED RANDOMLY ";
		}else{
			return " DISTRIBUTED BY ("+distributeColumns+") "; 
		}
	}
	@Override
	public String insertTable(String resTable, String decTable) {
		return "";
	}

	@Override
	public String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel){
		StringBuffer sb = new StringBuffer(" WITH (");
		sb.append("APPENDONLY=");
		if(!isAppendOnly){
			sb.append("FALSE"); 
		}else{
			sb.append("TRUE");
			sb.append(",ORIENTATION=");
			if(isColumnarStorage){
				sb.append("COLUMN");
			}else{
				sb.append("ROW");
			}
			if(isCompression){
				sb.append(",COMPRESSTYPE=");
				sb.append("ZLIB");
				sb.append(",COMPRESSLEVEL=");
				sb.append(compressionLevel);
			}
		}
		sb.append(" )");
		return sb.toString();
	}
}
