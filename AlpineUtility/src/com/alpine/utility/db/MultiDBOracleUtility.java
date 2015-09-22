/**
 * ClassName  MultiDBOracleUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.resources.AlpineUtilityConfig;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

public class MultiDBOracleUtility implements IMultiDBUtility {
    private static final Logger itsLogger = Logger.getLogger(MultiDBOracleUtility.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3256096275875271194L;


	@Override
	public String floatArrayHead() {
		return "Floatarray(";
	}
	@Override
	public String floatArrayTail() {
		return ")";
	}
	@Override
	public String stringArrayHead() {
		return "varchar2array(";
	}
	@Override
	public String stringArrayTail() {
		return ")";
	}
	@Override
	public long castArrayDoubleToLong(Object obj) {
		BigDecimal bd=(BigDecimal)obj;
 		return bd.longValue();
	}
	@Override
	public long castArrayIntegerToLong(Object obj) {
		BigDecimal bd=(BigDecimal)obj;
 		return bd.longValue();
	}
	@Override
	public float castArrayToFloat(Object obj) {
		BigDecimal bd=(BigDecimal)obj;
 		return bd.floatValue();
	}
	@Override
	public int castArrayToInt(Object obj) {
		BigDecimal bd=(BigDecimal)obj;
 		return bd.intValue();
	}
	@Override
	public double castArrayToDouble(Object obj) {
		BigDecimal bd=(BigDecimal)obj;
 		return bd.doubleValue();
	}
	@Override
	public String intArrayTail() {
		return ")";
	}
	@Override
	public String intArrayHead() {
		return "IntegerArray(";
	}

	@Override
	public boolean isTableNameTooLong(String tableName) {
		if(tableName.length()>30)return true;
		else
		return false;
	}
	@Override
	public long getSampleDistinctCount(Statement st,String tableName, String columnName,String whereCondition) throws SQLException{
		String sql = new String();
		sql += " select count ( distinct "+StringHandler.doubleQ(columnName)+" ) from (select "+StringHandler.doubleQ(columnName)
		+" from "+StringHandler.doubleQ(tableName)+" "+(StringUtil.isEmpty(whereCondition)?" where ":(whereCondition+" and "))+" rownum <= "
		+Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT))*10+" ) foo";
		itsLogger.debug(getClass().getName()+".getSampleDistinctCount(): sql = "+sql);
		ResultSet rs = st.executeQuery(sql);
		long count = 0;
		if (rs.next()){
			count = rs.getLong(1);
		}
		if (rs != null){
			rs.close();
		}
		return count;
	}
	@Override
	public double getSampleDistinctRatio(Statement st,String tableName, String columnName,String whereCondition) throws SQLException{
		String sql = new String();
		sql += " select count ( distinct "+StringHandler.doubleQ(columnName)+" ), count("+StringHandler.doubleQ(columnName)+" ) from (select "+StringHandler.doubleQ(columnName)
		+" from "+StringHandler.doubleQ(tableName)+" "+(StringUtil.isEmpty(whereCondition)?" where ":(whereCondition+" and "))+" rownum <= "+AlpineUtilityConfig.SAMPLE_DISTINCT_COUNT+" ) foo";
		itsLogger.debug(getClass().getName()+".getSampleDistinctCount(): sql = "+sql);
		ResultSet rs = st.executeQuery(sql);
		double ratio = 0.0;
		long distinctCount = 0;
		long count = 0;
		if (rs.next()){
			distinctCount = rs.getLong(1);
			count = rs.getLong(2);
			if(count > 0){
				ratio = distinctCount * 1.0/count;
			}else{
				ratio = 0.0;
			}
		}
		if (rs != null){
			rs.close();
		}
		return ratio;
	}
	

	@Override
	public long getSampleAllCount(Statement st,String tableName, String columnName, long countAllThreshold) throws SQLException{
		String sql = new String();
		sql += " select count ( "+StringHandler.doubleQ(columnName)+" ) from (select "+StringHandler.doubleQ(columnName)
		+" from "+StringHandler.doubleQ(tableName)+" where rownum <= "
		+countAllThreshold+" ) foo";
		itsLogger.debug(getClass().getName()+".getSampleAllCount(): sql = "+sql);
		ResultSet rs = st.executeQuery(sql);
		long count = 0;
		if (rs.next()){
			count = rs.getLong(1);
		}
		if (rs != null){
			rs.close();
		}
		return count;
	}
	public void dropTraingTempTable(Statement st,String tableName) throws SQLException{
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		itsLogger.debug(truncate.toString());
		st.execute(truncate.toString());
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		itsLogger.debug(dropSql.toString());
		st.execute(dropSql.toString());
	}

}
