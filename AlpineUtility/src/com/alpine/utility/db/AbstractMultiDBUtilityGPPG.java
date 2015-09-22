/**
 * ClassName  AbstractMultiDBUtilityGPPG.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.resources.AlpineUtilityConfig;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

public class AbstractMultiDBUtilityGPPG implements IMultiDBUtility {
    private static final Logger itsLogger = Logger.getLogger(AbstractMultiDBUtilityGPPG.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = -1306510255471725154L;




	@Override
	public String floatArrayHead() {
		return "array[";
	}
	@Override
	public String floatArrayTail() {
		return "]::float[]";
	}
	@Override
	public String stringArrayHead() {
		return "array[";
	}
	@Override
	public String stringArrayTail() {
		return "]";
	}
	@Override
	public long castArrayDoubleToLong(Object obj) {
		return new Long(((Double)obj).intValue());
	}
	@Override
	public long castArrayIntegerToLong(Object obj) {
		return new Long((Integer)obj);
	}
	
	@Override
	public float castArrayToFloat(Object obj) {
		return ((Double)obj).floatValue();
	}
	@Override
	public int castArrayToInt(Object obj) {
		return ((Double)obj).intValue();
	}
	@Override
	public double castArrayToDouble(Object obj) {
		return (Double)obj;
	}
	@Override
	public String intArrayTail() {
		return "]::int[]";
	}
	@Override
	public String intArrayHead() {
		return "array[";
	}
	
	@Override
	public boolean isTableNameTooLong(String tableName) {
		return false;
	}
	
	@Override
	public long getSampleDistinctCount(Statement st,String tableName, String columnName,String whereCondition) throws SQLException{
		StringBuilder sql = new StringBuilder();
		sql.append(" select count ( distinct ").append(StringHandler.doubleQ(columnName)).append(" ) from (select ").append(StringHandler.doubleQ(columnName));
		sql.append(" from ").append(StringHandler.doubleQ(tableName)).append(" ").append((StringUtil.isEmpty(whereCondition)?"":whereCondition)).append(" limit ");
		sql.append(Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_MAX_DISTINCT))*10).append(" ) foo");
		itsLogger.debug(getClass().getName()+".getSampleDistinctCount(): sql = "+sql);
		ResultSet rs = st.executeQuery(sql.toString());
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
	public long getSampleAllCount(Statement st,String tableName, String columnName, long countAllThreshold) throws SQLException{
		StringBuilder sql = new StringBuilder();
		sql.append(" select count ( ").append(StringHandler.doubleQ(columnName)).append(" ) from (select ").append(StringHandler.doubleQ(columnName));
		sql.append(" from ").append(StringHandler.doubleQ(tableName)).append(" limit ");
		sql.append(countAllThreshold).append(" ) foo");
		itsLogger.debug(getClass().getName()+".getSampleAllCount(): sql = "+sql);
		ResultSet rs = st.executeQuery(sql.toString());
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
		+" from "+StringHandler.doubleQ(tableName)+" "+(StringUtil.isEmpty(whereCondition)?"":whereCondition)+" limit "+AlpineUtilityConfig.SAMPLE_DISTINCT_COUNT+" ) foo";
		itsLogger.debug(getClass().getName()+".getSampleDistinctRatio(): sql = "+sql);
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
