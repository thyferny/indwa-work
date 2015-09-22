/**
 * ClassName  DataSourceInfoOracle.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.alpine.resources.CommonLanguagePack;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class DataSourceInfoOracle implements IDataSourceInfo {
	/**
	 * 
	 */
    private static final Logger itsLogger = Logger.getLogger(DataSourceInfoOracle.class);
    private static final long serialVersionUID = 7266215255190043184L;
	public static final String dBDriver = "oracle.jdbc.driver.OracleDriver";
	public static final String dBType = "Oracle";
	public static List<String > systemSchema= new ArrayList<String >();
	public static final String jdbcFileName="ojdbc6.jar";
	private Locale locale=Locale.getDefault();

	static{
		systemSchema.add("APEX_030200");
		systemSchema.add("APEX_PUBLIC_USER");
		systemSchema.add("APPQOSSYS");
		systemSchema.add("BI");
		systemSchema.add("CTXSYS");
		systemSchema.add("DBSNMP");
		systemSchema.add("DIP");
		systemSchema.add("EXFSYS");
		systemSchema.add("FLOWS_FILES");
		systemSchema.add("HR");
		systemSchema.add("IX");
		systemSchema.add("MDDATA");
		systemSchema.add("MDSYS");
		systemSchema.add("MGMT_VIEW");
		systemSchema.add("OE");
		systemSchema.add("OLAPSYS");
		systemSchema.add("ORACLE_OCM");
		systemSchema.add("ORDDATA");
		systemSchema.add("ORDPLUGINS");
		systemSchema.add("ORDSYS");
		systemSchema.add("OUTLN");
		systemSchema.add("OWBSYS");
		systemSchema.add("OWBSYS_AUDIT");
		systemSchema.add("PM");
		systemSchema.add("ROBI");
		systemSchema.add("RORO");
		systemSchema.add("SH");
		systemSchema.add("SI_INFORMTN_SCHEMA");
		systemSchema.add("SPATIAL_CSW_ADMIN_USR");
		systemSchema.add("SPATIAL_WFS_ADMIN_USR");
		systemSchema.add("SYS");
		systemSchema.add("SYSMAN");
		systemSchema.add("SYSTEM");
		systemSchema.add("WMSYS");
		systemSchema.add("XDB");
	}

	public String getBaseSimpleUrl(String hostname, int port, String dbname){
		return "jdbc:oracle:thin:@"+hostname+":"+port+":"+dbname;
	}
	public String getBaseFullUrl(String hostname, int port,String dbname, String dbuser, String password){
		return "jdbc:oracle:thin:@"+hostname+":"+port+":"+dbname+"?user="+dbuser+"&password="+password;
	}

	@Override
	public String getDBDriver() {
		return dBDriver;
	}
	@Override
	public String getDBType() {
		return dBType;
	}
	
	public List<String> getSystemSchema() {
		return systemSchema;
	}

	public static String getJdbcFullFilePath() {
		return AlpineUtil.getJarFileDir()+jdbcFileName;
	}
	@Override
	public boolean checkDBConnection(DbConnection dbc) throws Exception {
		if (OracleJdbcDriver.getInstance() == null) {
			AlpineConncetionException connException = new AlpineConncetionException(
					CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_LOAD_ERROR, locale));
			throw connException;
		}
		String PASSWORD = "password";
		String USER = "user";

		Driver driverd = OracleJdbcDriver.getInstance().getDriver();

		Properties props = new Properties();
		props.setProperty(USER, dbc.getDbuser());
		props.setProperty(PASSWORD, dbc.getPassword());
		Connection conn = null;
		try {
			DriverManager.setLoginTimeout(Integer.parseInt(ProfileReader
					.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
			conn = driverd.connect(dbc.getUrl(), props);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
					throw e;
				}
			}
		}
		return true;
	}
	@Override
	public Locale getLocale() {
		return locale;
	}
	@Override
	public void setLocale(Locale locale) {
		this.locale=locale;
	}
	
	public   String createSelectSql(String tablename, String limit){
		return "select  * from " + tablename +  " where rownum <= " + limit;
	}
	@Override
	public String[] deComposeUrl(String url) {
		// jdbc:oracle:thin:@192.168.1.1:1521:orcl
		String[] urlArray=new String[3];
		if(!StringUtil.isEmpty(url)){
			String[] urlStrArray = url.split("@");
			String urlStr = urlStrArray[1];
			urlArray=urlStr.split(":");
		}
		return urlArray;
	}
}
