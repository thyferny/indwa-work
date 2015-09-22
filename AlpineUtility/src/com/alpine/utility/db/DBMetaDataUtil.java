/**
 * ClassName DbMetaData.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;


public class DBMetaDataUtil {
    private static final Logger itsLogger = Logger.getLogger(DBMetaDataUtil.class);

    public static final String TABLE_NAME = "TABLE_NAME";
	public static final String COLUMN_NAME = "COLUMN_NAME";
	public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
	public static final String COLUMN_SIZE = "COLUMN_SIZE";
	public static final String TYPE_NAME = "TYPE_NAME";
	public static final String DATA_TYPE = "DATA_TYPE";
	public static final String TABLE_TYPE_VIEW = "VIEW";
	public static final String TABLE_TYPE_TABLE = "TABLE";
	private DbConnection dbConn = null;
	
	public static final List<String> connDeadList=new ArrayList<String>();

	private ArrayList<String> schemaList = null;
	// private String[] tableTypes = {TABLE_TYPE_TABLE,TABLE_TYPE_VIEW};
	private DataTypeConverter conv = null;
	private Connection connection;
	private boolean judgeConnection=false;
	private Locale locale=Locale.getDefault();

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public DBMetaDataUtil(DbConnection conn) {
		this.dbConn = conn;
		initialize();
	}

	private void initialize() {
		conv = DataTypeConverterUtil.createDataTypeConverter(dbConn.getDbType());
	}

	public Connection getConnection() throws  Exception {

		if (connection == null || connection.isClosed() == true) {
			 try{
				 connection = AlpineUtil.createConnection(dbConn ,locale) ;
			}catch(Exception e){
				if(false==DBMetaDataUtil.connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
					DBMetaDataUtil.connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
				}
				throw e;
			}
			//now database connection is OK
			if(DBMetaDataUtil.connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
				DBMetaDataUtil.connDeadList.remove(dbConn.getUrl()+dbConn.getDbuser());
			}
		}
		
		return connection;
	}
	
	public DBMetaDataUtil(Connection conn){
		this.connection = conn;
		initialize();
	}

	public ArrayList<String> getSchemaList() throws Exception {
		if (schemaList == null) {
			Connection dbCon = null;
			ResultSet rsSchema = null;
			try {
				if(judgeConnectionDead()){
					return new ArrayList<String>();
				}
				schemaList = new ArrayList<String>();
				dbCon = getConnection();
				if(dbCon!=null){
					DatabaseMetaData md = dbCon.getMetaData();
					
					rsSchema = md.getSchemas();
					while (rsSchema.next()) {
						String schemaName = rsSchema.getString("TABLE_SCHEM");
						schemaList.add(schemaName);
					}
				}
			} catch (Exception e) {
				if(!connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
					connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
				}
				throw e;
			} finally {
				if (rsSchema != null) {
					rsSchema.close();
				}

			}
		}
		return schemaList;
	}

	private boolean judgeConnectionDead() {
		if(!isJudgeConnection()){
			return false;
		}
		if(connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
			return true;
		}else{
			return false;
		}
	}

	public ArrayList<String> getAllTableList(String schemaName, String filterPattern)
			throws Exception {
		return getTableNameList(schemaName, TABLE_TYPE_TABLE, filterPattern);
	}

	/**
	 * @param schemaName
	 * @param string
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> getTableNameList(String schemaName, String type, String filterPattern)
			throws Exception {
		ArrayList<String> tableList = new ArrayList<String>();
		Connection dbCon = null;
		ResultSet rsTable = null;
		try {
			if(judgeConnectionDead()){
				return new ArrayList<String>();
			}
			dbCon = getConnection();
			if(dbCon!=null){
				DatabaseMetaData md = dbCon.getMetaData();
				//here avoid the partition table...
	 
				rsTable = md
						.getTables(null, schemaName, "%", new String[] { type });
				while (rsTable.next()) {
					String tableName = rsTable.getString(TABLE_NAME);
					if(!StringUtil.isEmpty(filterPattern) && tableName.matches(filterPattern)){
						continue;
					}
					tableList.add(tableName);
				}
			}
		} catch (Exception e) {
			if(!connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
				connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
			}
			throw e;
		} finally {
			if(rsTable!=null){
				rsTable.close();
			}
		}
		return tableList;
	}
	
	//generate Greenplum SQL for getting table lists. Avoid partition subtables
	private String getGPTableSQL(String schemaName) {		
		String sql=
			"SELECT n.nspname AS TABLE_SCHEM,c.relname AS TABLE_NAME FROM pg_catalog.pg_namespace n JOIN pg_catalog.pg_class c "+
			"ON c.relnamespace = n.oid LEFT JOIN pg_catalog.pg_description d ON (c.oid = d.objoid AND d.objsubid = 0) LEFT JOIN "+
			" pg_catalog.pg_class dc ON (d.classoid=dc.oid AND dc.relname='pg_class') LEFT JOIN pg_catalog.pg_namespace dn ON "+
			" (dn.oid=dc.relnamespace AND dn.nspname='pg_catalog') LEFT JOIN pg_catalog.pg_partition_rule pt ON c.oid = pt.parchildrelid "+
			" WHERE c.relnamespace = n.oid AND n.nspname LIKE '"+schemaName+"' AND c.relname LIKE '%' AND ( false OR ( c.relkind = 'r'"+
			" AND n.nspname NOT LIKE 'pg'||E'\\_'||'%' AND n.nspname <> 'information_schema' ) ) AND pt.parchildrelid IS NULL ORDER BY"+
			" TABLE_SCHEM,TABLE_NAME ";
		return sql;
	}
	
	// generate Postgres SQL for getting table lists. Avoid partition subtables 
	private String getPGTableSQL(String schemaName) {
		String sql=
			"select n.nspname AS TABLE_SCHEM, c.relname AS TABLE_NAME FROM pg_catalog.pg_class c " +                                                                
			"LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "	+
			"LEFT JOIN  (select relid, relname from pg_catalog.pg_stat_user_tables a join pg_catalog.pg_inherits b on a.relid=b.inhrelid)  j on j.relname  = c.relname " +                       
			"WHERE c.relkind IN ('r','') AND n.nspname NOT IN ('pg_catalog', 'pg_toast')  AND j.relid is null " +                             
			"AND n.nspname like '" + schemaName + "' ORDER BY 1, 2";//pg_catalog.pg_table_is_visible(c.oid) and
		//The clause "pg_catalog.pg_table_is_visible(c.oid)" will filter all table which is not in current search path.So remove it.
		//JIRA MINER-969

		
		return sql;
	}
	
	public ArrayList<String> getPGTableList(String schemaName )
	throws Exception {
		ArrayList<String> tableList = new ArrayList<String>();
		 
		ResultSet rsTable = null;
		Statement st =null;
		try {
			if(judgeConnectionDead()){
				return new ArrayList<String>();
			}
			Connection dbCon = getConnection();
			  st = dbCon.createStatement();
			  String sql = null;
			  if (AlpineUtil.isGreenplum(dbCon)) {
				  sql = getGPTableSQL(schemaName);
			  } else {
				  sql = getPGTableSQL(schemaName);
			  }
			itsLogger.debug("DBMetaDataUtil.getPGTableList():sql="+sql);
			rsTable = st.executeQuery(sql);
			while (rsTable.next()) {
				String tableName = rsTable.getString(TABLE_NAME);
				tableList.add(tableName);
			}
		} catch (Exception e) {
			if(!connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
				connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
			}
			throw e;
		} finally {
			if(rsTable!=null){
				rsTable.close();
			}
			if(st!=null){
				st.close();
			}
		
		}
		return tableList;
	}
	
 
	public ArrayList<String> getAllViewList(String schemaName, String filterPattern) throws Exception {
		return getTableNameList(schemaName, TABLE_TYPE_VIEW, filterPattern);
	}

	public ArrayList<String[]> getAllColumnList(String schemaName,
			String tableName) throws Exception {
		ArrayList<String[]> columnList = new ArrayList<String[]>();
		Connection dbCon = null;
		ResultSet rsCol = null;
		try {
			if(judgeConnectionDead()){
				return new ArrayList<String[]>();
			}
			dbCon = getConnection();
			DatabaseMetaData md = dbCon.getMetaData();
			rsCol = md.getColumns(null, schemaName, tableName, "%");
			while (rsCol.next()) {
				int dataType = rsCol.getInt(DATA_TYPE);
				String typeName = rsCol.getString(TYPE_NAME);
				int colSize = rsCol.getInt(COLUMN_SIZE);
				int decimal = rsCol.getInt(DECIMAL_DIGITS);
				String dataTypeClause = conv.getClause(typeName, dataType,
						colSize, decimal);
				columnList.add(new String[] { rsCol.getString(COLUMN_NAME),
						dataTypeClause });
			}
		} catch (Exception e) {
			if(!connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
				connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
			}
			throw e;
		} finally {
			if(rsCol!=null){
				rsCol.close();
			}
		}
		return columnList;
	}

	public ArrayList<String[]> getColumnList(String schemaName, String tableName)
			throws Exception {
		return getAllColumnList(schemaName,tableName);
	}


 

	public void disconnect() {
		try {
			if (connection != null && connection.isClosed() == false) {
				connection.close();
				connection = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param schemaName
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	public boolean isTableExist(String schemaName, String tableName) throws  Exception {
		boolean result=false;
		try {
			if(judgeConnectionDead()){
				return false;
			}
			String[] tableTypes = {"TABLE","VIEW" };
			 
			DatabaseMetaData md =  getConnection()
					.getMetaData();
			ResultSet rsTable = md.getTables(null, schemaName, "%",
					tableTypes);
			while (rsTable.next()) {
				if (tableName.equals(rsTable.getString(TABLE_NAME))) {
					result=true;
					break;
				 
					
				}
			}
			rsTable.close();
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e) ;
		 throw e;
		}
		return result;
	}

	/**
	 * @param schemaName
	 * @param tableName
	 * @return
	 * @throws Exception 
	 */
	public List<TableColumnMetaInfo> getColumnMetaInfoList(String schemaName,
			String tableName) throws Exception {
		ArrayList<TableColumnMetaInfo> columnList = new ArrayList<TableColumnMetaInfo>();
		if(StringUtil.isEmpty(tableName)){
			return columnList;
		}
		Connection dbCon = null;
		ResultSet rsCol = null;
		try {
			if(judgeConnectionDead()){
				return new ArrayList<TableColumnMetaInfo>();
			}
			dbCon = getConnection();
			DatabaseMetaData md = dbCon.getMetaData();
			rsCol = md.getColumns(null, schemaName, tableName, "%");
			while (rsCol.next()) {
				int dataType = rsCol.getInt(DATA_TYPE);
				String typeName = rsCol.getString(TYPE_NAME);
				int colSize = rsCol.getInt(COLUMN_SIZE);
				int decimal = rsCol.getInt(DECIMAL_DIGITS);
				String dataTypeClause = conv.getClause(typeName, dataType,
						colSize, decimal);
				columnList.add(new TableColumnMetaInfo( rsCol.getString(COLUMN_NAME),
						dataTypeClause ));
			}
		} catch (Exception e) {
			if(!connDeadList.contains(dbConn.getUrl()+dbConn.getDbuser())){
				connDeadList.add(dbConn.getUrl()+dbConn.getDbuser());
			}
			throw e;
		} finally {
			if(rsCol!=null){
				rsCol.close();
			}

		}
		return columnList;
	 
	}

	public boolean isJudgeConnection() {
		return judgeConnection;
	}

	public void setJudgeConnection(boolean judgeConnection) {
		this.judgeConnection = judgeConnection;
	}

	
}
