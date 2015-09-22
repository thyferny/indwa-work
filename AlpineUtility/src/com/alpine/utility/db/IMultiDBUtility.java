package com.alpine.utility.db;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;

public interface IMultiDBUtility extends Serializable {
	
	public abstract String floatArrayHead();
	public abstract String floatArrayTail();
	public abstract String intArrayHead();
	public abstract String intArrayTail();
	public abstract String stringArrayHead() ;
	public abstract String stringArrayTail();
	public abstract long castArrayDoubleToLong(Object obj);
	public abstract long castArrayIntegerToLong(Object obj);
	public abstract float castArrayToFloat(Object obj);
	public abstract int castArrayToInt(Object obj);
	public abstract double castArrayToDouble(Object obj);
	public abstract boolean isTableNameTooLong(String tableName);
	public abstract long getSampleDistinctCount(Statement st,String tableName, String columnName,String whereCondition) throws SQLException;
	public abstract long getSampleAllCount(Statement st,String tableName, String columnName, long countAllThreshold) throws SQLException;
	public abstract double getSampleDistinctRatio(Statement st,String tableName, String columnName,String whereCondition) throws SQLException;
	public abstract void dropTraingTempTable(Statement st,String tableName) throws SQLException;


}
