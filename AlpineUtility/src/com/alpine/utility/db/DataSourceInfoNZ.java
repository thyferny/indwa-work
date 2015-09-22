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

public class DataSourceInfoNZ implements IDataSourceInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8292343176525022800L;
    private static final Logger itsLogger = Logger.getLogger(DataSourceInfoNZ.class);

    public static final String dBDriver = "org.netezza.Driver";
	public static final int maxColumnLength=128;
	public static final String dBType = "Netezza";
	public static List<String > systemSchema= new ArrayList<String >();
	public static final String jdbcFileName="nzjdbc.jar";
	private Locale locale=Locale.getDefault();

	@Override
	public boolean checkDBConnection(DbConnection dbc) throws Exception {
		if (NZJdbcDriver.getInstance() == null) {
			AlpineConncetionException connException = new AlpineConncetionException(
					CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_LOAD_ERROR, locale));
			throw connException;
		}
		String PASSWORD = "password";
		String USER = "user";

		Driver driverd = NZJdbcDriver.getInstance().getDriver();

		Properties props = new Properties();
		props.setProperty(USER, dbc.getDbuser());
		props.setProperty(PASSWORD, dbc.getPassword());
		TestConnTimer testConnTimer =null;
		try {
			DriverManager.setLoginTimeout(Integer.parseInt(ProfileReader
					.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
			boolean result=false;
			testConnTimer = new TestConnTimer(driverd,props,dbc);
			testConnTimer.start();
					
			long start = System.currentTimeMillis();
					
			int timeoutTime = Integer.parseInt(ProfileReader
					.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT))*1000;	
			boolean timeout=false;
	        while (start+timeoutTime>System.currentTimeMillis()) {
	            try {
	                Thread.sleep(100);
	                if(testConnTimer.getResult()==2||
	                		testConnTimer.getResult()==0){
	                	timeout=false;
	                	break;
	                }else{
	                	timeout=true;
	                }
	            } catch (InterruptedException e) {
					itsLogger.error(e.getMessage(),e);
	            }
	        }
	        
	        if(testConnTimer.getResult()==0){
	        	result=true;
	        }else{
	        	result=false;
	        }
	        if(timeout){
	        	throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.Connection_TIME_OUT, locale));
	        }else if(testConnTimer.getResult()==2){
	        	throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.Network_Interface_PORT_error, locale));
	        }
	        return result;		
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			 testConnTimer.interrupt();
		}
	}

 

	@Override
	public String getBaseSimpleUrl(String hostname, int port, String dbname) {
		return "jdbc:netezza://"+hostname+":"+port+"/"+dbname;
	}

	@Override
	public String getDBDriver() {
		return dBDriver;
	}

	@Override
	public String getDBType() {
		return dBType;
	}

	@Override
	public List<String> getSystemSchema() {
		return systemSchema;
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

	
	class TestConnTimer extends Thread { 
	    int result = CONNERROR;
		private Driver driver;
		private Properties props;
		private DbConnection dbc;
		private Connection conn=null;
		
		public static final int SUCCESS=0;
		public static final int TIMEOUT=1;
		public static final int PORTERROR=2;
		public static final int CONNERROR=3;

		TestConnTimer(Driver driver, Properties props, DbConnection dbc) { 
			this.driver=driver;
			this.props=props;
			this.dbc=dbc;
	    }

	    public void run() {    	
	    	try {
				conn = driver.connect(dbc.getUrl(), props);
				result=SUCCESS;
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				if(e instanceof NullPointerException){
					result=PORTERROR;
				}else{
					result=CONNERROR;
				}			
				return;
			}finally{
				if(conn!=null){
					try {
						conn.close();
					} catch (SQLException e) {
						itsLogger.error(e.getMessage(),e);
						result=CONNERROR;
						return;
					}
				}
			}
	    }
	    
	    public int getResult(){
	    	return result;
	    }
	}
	
	public   String createSelectSql(String tablename, String limit){
		return  "select * from " + tablename +  " limit " + limit;	
	}

	@Override
	public String[] deComposeUrl(String url) {
		// jdbc:netezza://192.168.1.232:5480/miner_demodb
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
