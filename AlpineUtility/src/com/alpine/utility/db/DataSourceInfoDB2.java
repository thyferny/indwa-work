/**
 * ClassName  DataSourceInfoDB2.java
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
/**
 * @author Eason
 * 
 */
public class DataSourceInfoDB2 implements IDataSourceInfo {

    private static final Logger itsLogger = Logger.getLogger(DataSourceInfoDB2.class);
    private static final long serialVersionUID = 6219203606812982060L;

	public static final String dBDriver = "com.ibm.db2.jcc.DB2Driver";
	public static final String dBType = "DB2";
	public static List<String > systemSchema= new ArrayList<String >();

	public static final String jdbcFileName="db2jcc.jar";
	
	private Locale locale=Locale.getDefault();

	public String getBaseSimpleUrl(String hostname, int port, String dbname){
		return "jdbc:db2://"+hostname+":"+port+"/"+dbname;
	}
	public String getBaseFullUrl(String hostname, int port,String dbname, String dbuser, String password){
		return "jdbc:db2://"+hostname+":"+port+"/"+dbname+":user="+dbuser+";password="+password;
	}

	static{
		systemSchema.add("NULLID");
		systemSchema.add("SQLJ");
		systemSchema.add("SYSCAT");
		systemSchema.add("SYSFUN");
		systemSchema.add("SYSIBM");
		systemSchema.add("SYSIBMADM");
		systemSchema.add("SYSIBMINTERNAL");
		systemSchema.add("SYSIBMTS");
		systemSchema.add("SYSPROC");
		systemSchema.add("SYSPUBLIC");
		systemSchema.add("SYSSTAT");
		systemSchema.add("SYSTOOLS");
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
	@Override
	public boolean checkDBConnection(DbConnection dbc) throws Exception {
		if (Db2JdbcDriver.getInstance() == null) {
			AlpineConncetionException connException = new AlpineConncetionException(
					CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_LOAD_ERROR, locale));
			throw connException;
		}
		String PASSWORD = "password";
		String USER = "user";

		Driver driverd = Db2JdbcDriver.getInstance().getDriver();

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
	public static String getJdbcFullFilePath() {
		return AlpineUtil.getJarFileDir()+jdbcFileName;
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
		return "select * from " + tablename +  " fetch first " + limit+ " rows only ";
	}

	@Override
	public String[] deComposeUrl(String url) {
		// jdbc:db2://192.168.1.1:50001/AM_DEMO
		String[] urlArray=new String[3];
		if(!StringUtil.isEmpty(url)){
			String[] urlStrArray = url.split("//");
			String urlStr = urlStrArray[1];
			//192.168.1.1:50001/AM_DEMO
			String[] temp = urlStr.split(":",2);
			urlArray[0]=temp[0];
			String[] temp1 = temp[1].split("/",2);
			urlArray[1]=temp1[0];
			urlArray[2]=temp1[1];
		}
		return urlArray;
	}
}
