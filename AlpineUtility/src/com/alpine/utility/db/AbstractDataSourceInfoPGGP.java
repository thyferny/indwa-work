/**
 * ClassName  AbstractDataSourceInfoPGGP.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public abstract class AbstractDataSourceInfoPGGP implements IDataSourceInfo {
	private static final long serialVersionUID = -6503792197142859893L;
	public static final String dBDriver = "org.postgresql.Driver";
	public static List<String > systemSchema= new ArrayList<String >();
	private Locale locale=Locale.getDefault();
    private static final Logger itsLogger = Logger.getLogger(AbstractDataSourceInfoPGGP.class);
    static{
		systemSchema.add("information_schema");
		systemSchema.add("gp_toolkit");
		systemSchema.add("pg_aoseg");
		systemSchema.add("pg_bitmapindex");
		systemSchema.add("pg_catalog");
	}
	
	public String getBaseSimpleUrl(String hostname, int port, String dbname){
		return "jdbc:postgresql://"+hostname+":"+port+"/"+dbname;
	}
 
	public List<String> getSystemSchema() {
		return systemSchema;
	}

	@Override
	public boolean checkDBConnection(DbConnection dbc) throws Exception{
		Class.forName(getDBDriver());
		Connection dbcon = null;
			try {
            	DriverManager.setLoginTimeout(
            			Integer.parseInt(
            					ProfileReader.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
            	
            	String url = dbc.getUrl()+"?user="+dbc.getDbuser()+"&password="+dbc.getPassword();
            	if(dbc.getUseSSL().equals("true")){
            		url = url + "&ssl=true";
            	}
            	dbcon  = DriverManager.getConnection(url);
			 
			
				return true;
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw e;
			} finally {
				if (dbcon != null) {
					try {
						dbcon.close();
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						throw e;
					}
				}
			}
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
		return  "select * from " + tablename +  " limit " + limit;	
	}
	
	@Override
	public String[] deComposeUrl(String url) {
		// jdbc:postgresql://192.168.1.1:5432/miner_demo
		String[] urlArray=new String[3];
		if(!StringUtil.isEmpty(url)){
			String[] urlStrArray = url.split("//");
			String urlStr = urlStrArray[1];
			//192.168.1.1:5432/miner_demo
			String[] temp = urlStr.split(":",2);
			urlArray[0]=temp[0];
			String[] temp1 = temp[1].split("/",2);
			urlArray[1]=temp1[0];
			urlArray[2]=temp1[1];
		}
		return urlArray;
	}
}
