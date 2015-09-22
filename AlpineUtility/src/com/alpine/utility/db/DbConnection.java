/**
 * ClassName DbConnection.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.Properties;

import com.alpine.utility.file.StringUtil;

public class DbConnection {
	/**
 * 
 */
	public static final String PASSWORD_TEXT = "passwordText";
	/**
 * 
 */
	public static final String USER_NAME_TEXT = "userNameText";
	/**
 * 
 */
	public static final String DB_NAME_TEXT = "dbNameText";
	/**
 * 
 */
	public static final String PORT_TEXT = "portText";
	/**
 * 
 */
	public static final String HOST_TEXT = "hostText";
	
	public static final String USE_SSL = "useSSL";
	/**
 * 
 */
	public static final String ENGINE_COMBO = "engineCombo";
	/**
 * 
 */
	public static final String CONN_NAME_TEXT = "connNameText";
	public static final String JDBC_DRIVER_COMBO = "jdbcDriverCombo";
	public static final String ORACLE_JDBC = "jdbc";

	private String connName = null; // name of the connection
	// private String connUid = null; // connection uid
	private String hostname = null;
	private int port = -1;
	private String dbname = null;
	private String dbuser = null;
	private String password = null;
	private String dbType = null;
	private String useSSL = "false";//default value
	public String getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
		 
	}

	private String jdbcDriverFileName = null;

	public String getJdbcDriverFileName() {
		return jdbcDriverFileName;
	}

	public void setJdbcDriverFileName(String jdbcDriverFileName) {
		this.jdbcDriverFileName = jdbcDriverFileName;
	}

	private String url;

	// private transient List<String> systemSchema;
	//
	// public List<String> getSystemSchema() {
	// return systemSchema;
	// }

	public DbConnection(String dbType, String hostname, int port,
			String dbname, String dbuser, String password,
			String jdbcDriverFileName,String useSSL) {
		this.hostname = hostname;
		this.dbType = dbType;
		this.port = port;
		this.dbname = dbname;
		this.dbuser = dbuser;
		this.password = password;
		this.jdbcDriverFileName = jdbcDriverFileName;
		this.useSSL=useSSL;
		composeUrls();
	}

	@Deprecated
	// will remove it when eclipse is ready to use jdbc driver function
	public DbConnection(String dbType, String hostname, int port,
			String dbname, String dbuser, String password,String useSSL) {
		this.hostname = hostname;
		this.dbType = dbType;
		this.port = port;
		this.dbname = dbname;
		this.dbuser = dbuser;
		this.password = password;
		this.useSSL=useSSL;
		composeUrls();
	}

	/**
	 * @param props
	 */
	public DbConnection(Properties props) {

		reload(props);

	}

	public void composeUrls() {
		IDataSourceInfo connInfo = DataSourceInfoFactory
				.createConnectionInfo(dbType);
		url = connInfo.getBaseSimpleUrl(AlpineUtil.getHostIP(hostname), port,
				dbname);
 

	}

	public String getUrl() {
		return url;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbuser(String dbuser) {
		this.dbuser = dbuser;
	}

	public String getDbuser() {
		return dbuser;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbType() {
		return dbType;
	}

	public void setConnName(String connName) {
		this.connName = connName;
	}

	public String getConnName() {
		return connName;
	}

	
//	MINER-2037 	If two different db connection have same property,one of connections can not be reflash by double click
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DbConnection == false || obj == null) {
			return false;
		}
		DbConnection that = (DbConnection) obj;
		return (StringUtil.safeEquals(this.getConnName(), that.getConnName())
				&& StringUtil.safeEquals(this.getDbType(), that.getDbType())
				&& StringUtil
						.safeEquals(this.getHostname(), that.getHostname())
				&& StringUtil.safeEquals(this.getDbname(), that.getDbname())
				&& StringUtil.safeEquals(this.getDbuser(), that.getDbuser())
				&& StringUtil
						.safeEquals(this.getPassword(), that.getPassword())
				&& StringUtil
						.safeEquals(this.getUseSSL() , that.getUseSSL() )
				&& StringUtil.safeEquals(this.getJdbcDriverFileName(), that
						.getJdbcDriverFileName()) && (this.getPort() == that
				.getPort())
				);

	}
//
	@Override
	public int hashCode() {
		int hash = 1;

		if (this.getConnName() != null) {
			hash = hash + this.getConnName().hashCode();
		}
		
		if (this.getDbType() != null) {
			hash = hash + this.getDbType().hashCode();
		}
		if (this.getHostname() != null) {
			hash = hash + this.getHostname().hashCode();
		}
		if (this.getDbname() != null) {
			hash = hash + this.getDbname().hashCode();
		}
		if (this.getDbuser() != null) {
			hash = hash + this.getDbuser().hashCode();
		}
		if (this.getPassword() != null) {
			hash = hash + this.getPassword().hashCode();
		}
		if (this.getUseSSL()  != null) {
			hash = hash + this.getUseSSL() .hashCode();
		}
		if (this.getJdbcDriverFileName() != null) {
			hash = hash + this.getJdbcDriverFileName().hashCode();
		}

 		return hash + port;
//
 	}

	@Override
	public DbConnection clone() {
		DbConnection conn = new DbConnection(getDbType(), getHostname(),
				getPort(), getDbname(), getDbuser(), getPassword(),
				getJdbcDriverFileName(),useSSL);
		conn.setConnName(getConnName());
	 
		return conn;
	}

	public void reload(Properties props) {
		this.hostname = props.get(HOST_TEXT).toString();
		this.dbType = props.get(ENGINE_COMBO).toString();
		this.port = (Integer.parseInt(props.get(PORT_TEXT).toString()));
		this.dbname = props.get(DB_NAME_TEXT).toString();
		this.dbuser =  props.get(USER_NAME_TEXT).toString();
		
		if(props.get(USE_SSL)!=null){
			this.useSSL =  props.get(USE_SSL).toString();
		}
		this.password = AlpineUtil.stringToObject(
				props.getProperty(PASSWORD_TEXT)).toString();
		this.connName = props.get(CONN_NAME_TEXT).toString();
		if (props.get(JDBC_DRIVER_COMBO) != null) {
			this.jdbcDriverFileName = props.get(JDBC_DRIVER_COMBO).toString();
		}
		composeUrls();
		
	}

}
