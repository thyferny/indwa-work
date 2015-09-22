package com.alpine.utility.db;

import java.io.Serializable;

public interface ISqlGeneratorMultiDB extends Serializable {
	public abstract String castToText(String columnName);
	public abstract String castToDouble(String columnName);
	public abstract String setSeed(String seed, Integer para, Integer index);
	public abstract String insertTable(String sql,String tableName);
	public abstract String setCreateTableEndingSql(String distributeColumns);
	public abstract String cascade();
	public abstract String to_date(String date);
	public abstract String dropTableIfExists(String tableName);
	public abstract String dropViewIfExists(String tableName);
	public abstract String generateTempTableString();
	public abstract String rownumberOverByNull();
	public abstract String countTable(String tableName,String number);
	public abstract String textArray();
	public abstract String getTableSetType(String type);
	public abstract String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel);
	public abstract String getCastDataType(String sql,String dataType);
}
