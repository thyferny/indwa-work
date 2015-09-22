package com.alpine.datamining.api.utility;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

public class DBDataUtil {
    private static final Logger itsLogger = Logger.getLogger(DBDataUtil.class);

    public static final String TABLE_TYPE_VIEW = "VIEW";
	public static final String TABLE_TYPE_TABLE = "TABLE";
	private DbConnection dbConn = null;
	private Locale locale=Locale.getDefault();

	private ArrayList<String> schemaList = null;
	// private String[] tableTypes = {TABLE_TYPE_TABLE,TABLE_TYPE_VIEW};
	private DataTypeConverter conv = null;
	private Connection connection;

	public DBDataUtil(DbConnection conn) {
		this.dbConn = conn;
		initialize();
	}

	private void initialize() {
		conv = DataTypeConverterUtil.createDataTypeConverter(dbConn.getDbType());
	}

	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private Connection getConnection() throws  Exception {

		if (connection == null || connection.isClosed() == true) {
			 
			connection = AlpineUtil.createConnection(dbConn ,locale) ;
		}
		return connection;
	}
	
	public DBDataUtil(Connection conn, String dbSystem){
		this.connection = conn;
		conv = DataTypeConverterUtil.createDataTypeConverter(dbSystem);
	}
	public ArrayList<String[]> getAllColumnList(String schemaName,
			String tableName) throws Exception {
		ArrayList<String[]> columnList = new ArrayList<String[]>();
		Connection dbCon = null;
		ResultSet rsCol = null;
		try {
			dbCon = getConnection();
			DatabaseMetaData md = dbCon.getMetaData();
			rsCol = md.getColumns(null, schemaName, tableName, "%");
			while (rsCol.next()) {
				int dataType = rsCol.getInt("DATA_TYPE");
				String typeName = rsCol.getString("TYPE_NAME");
				int colSize = rsCol.getInt("COLUMN_SIZE");
				int decimal = rsCol.getInt("DECIMAL_DIGITS");
				String dataTypeClause = conv.getClause(typeName, dataType,
						colSize, decimal);
				columnList.add(new String[] { rsCol.getString("COLUMN_NAME"),
						dataTypeClause });
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if(rsCol!=null){
				rsCol.close();
			}

		}
		return columnList;
	}

	public static String getDistribution(DatabaseConnection databaseConnection,String tableName,String dBType) throws OperatorException
	{
		if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new GpN2TUtil().getDistribution(databaseConnection, tableName);
		}else if(dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return null;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new OracleN2TUtil().getDistribution(databaseConnection, tableName);
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return null;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return null;
		}else{
			return new GpN2TUtil().getDistribution(databaseConnection, tableName);
		}
	}
	public static void alterColumnType(Statement st,String columnName,String tableName,String dBType) throws OperatorException, AnalysisError
	{
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)
		||dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			new GpN2TUtil().alterColumnType(st,columnName, tableName);
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			new OracleN2TUtil().alterColumnType(st,columnName, tableName);
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			new NZN2TUtil().alterColumnType(st,columnName, tableName);
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			new DB2N2TUtil().alterColumnType(st,columnName, tableName);
		}
		else
		{
			new GpN2TUtil().alterColumnType(st,columnName, tableName);
		}
	}
	
	public ArrayList<String[]> getColumnList(String schemaName, String tableName)
			throws Exception {
		return getAllColumnList(schemaName,tableName);
	}

	public ArrayList<String> getSchemaList() throws Exception {
		if (schemaList == null) {
			Connection dbCon = null;
			ResultSet rsSchema = null;
			try {
				dbCon = getConnection();
				DatabaseMetaData md = dbCon.getMetaData();
				schemaList = new ArrayList<String>();
				rsSchema = md.getSchemas();
				while (rsSchema.next()) {
					String schemaName = rsSchema.getString("TABLE_SCHEM");
					schemaList.add(schemaName);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (rsSchema != null) {
					rsSchema.close();
				}

			}
		}
		return schemaList;
	}

 
	public DataTable getTableDataList(String schemaName, String tableName,
			String rows,String dbType) throws Exception {
		DataTable dataTable = new DataTable();
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection dbCon = null;
		try {
			ArrayList<String[]> list = getColumnList(schemaName, tableName);
			String[] columns = new String[list.size()];
			List<TableColumnMetaInfo> columnList = new ArrayList<TableColumnMetaInfo>();
			for (int i = 0; i < list.size(); i++) {
				columns[i] = list.get(i)[0];
				TableColumnMetaInfo dc = new TableColumnMetaInfo(list.get(i)[0],list.get(i)[1]);
				columnList.add(dc);
			}
			dataTable.setColumns(columnList);
		 
			dataTable.setSchemaName(schemaName);
			dataTable.setTableName(tableName);
		
			dbCon = getConnection();

			String outputTableName=StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName);
			String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
			
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
						
			String sql = dataSourceInfo.createSelectSql(outputTableName,   maxRows);
			
			itsLogger.debug("DBDataUtil.getTableDataList():sql="+sql);
			dbCon.setAutoCommit(false);
			ps = dbCon.prepareStatement(sql);
			int fetchSize = Integer.parseInt(rows);
			ps.setFetchSize(fetchSize);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			List<DataRow> rowList = new ArrayList<DataRow>();
			while (rs.next() && fetchSize > 0) {
				fetchSize--;
				String[] items = new String[columns.length];
				DataRow dr = new DataRow();
				for(int i=0; i<columnCount; i++){
					if (DataTypeConverter.isDoubleType(rsmd.getColumnType(i+1))&&!DataTypeConverter.isMoneyType(rsmd.getColumnTypeName(i+1))) {
						items[i] = AlpineUtil.dealNullValue(rs,i+1);
					}else if(dbType.equals(DataSourceInfoOracle.dBType)&&DataTypeConverter.isArrayArrayColumnType(rsmd.getColumnTypeName(i+1), dbType)){
						items[i] = AlpineUtil.dealArrayArray(rs, i+1);
					}else if(dbType.equals(DataSourceInfoOracle.dBType)&&DataTypeConverter.isArrayColumnType(rsmd.getColumnTypeName(i+1), dbType)){
						items[i] = AlpineUtil.dealArray(rs, i+1);
					}else{
						items[i] = rs.getString(i+1);
					}
				}
				dr.setData(items);
				rowList.add(dr);
			}
			dataTable.setRows(rowList);
		} catch (Exception e) {
			itsLogger.error(
					getClass().getName() + "\n" + e.toString());
			throw e;
		} finally {
			if (rs != null)
				rs.close();
			if(ps != null)//must close statement firstly.
				ps.close();
			if (dbCon != null)
				dbCon.setAutoCommit(true);
		}

		return dataTable;
	}

	public DataTable getTableDataList(String schemaName, String tableName,
			String[] columnArray, String rows) throws Exception {
		DataTable te = new DataTable();
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection dbCon = null;
		try {
			dbCon = getConnection();

			// String columns = "";
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < columnArray.length; i++) {
				// columns+="\""+columnArray[i]+"\"";
				sb.append(StringHandler.doubleQ(columnArray[i]));
				if (i < columnArray.length - 1) {
					// columns+=",";
					sb.append(",");
				}
			}
			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(" WHERE ");
			for (int i = 0; i < columnArray.length; i++) {
				sbWhere.append(StringHandler.doubleQ(columnArray[i]) + " is not null");
				if (i < columnArray.length - 1) {
					// columns+=",";
					sbWhere.append(" AND ");
				}
			}

			String sql = " SELECT " + sb.toString() + " FROM " + schemaName
					+ "." + tableName + sbWhere.toString();
			int fetchSize = Integer
			.parseInt(rows);
			dbCon.setAutoCommit(false);
			ps = dbCon.prepareStatement(sql);
			ps.setFetchSize(fetchSize);
			rs = ps.executeQuery();
			List<DataRow> rowList = new ArrayList<DataRow>();
			while (rs.next()&&fetchSize > 0) {
				fetchSize--;
				String[] items = new String[columnArray.length];
				DataRow dr = new DataRow();
				int i = 0;
				for (String col : columnArray) {
					items[i] = rs.getString(col);
					i++;
				}
				dr.setData(items);
				rowList.add(dr);
			}
			te.setRows(rowList);
		} catch (Exception e) {
			itsLogger.error(
					getClass().getName() + "\n" + e.toString());
			throw e;
		} finally {
			if (rs != null)
				rs.close();
			if(ps != null)//must close statement firstly.
				ps.close();
			if (dbCon != null)
				dbCon.setAutoCommit(true);
		}

		return te;
	}

	/* 
	 * This is called by UnivariatePlot/Scatter Plot.  
	 * @xColumn is the reference column
	 * @columnArray are all the column need to be plotted + referenceColumn
	 * The code also assume the referenceColumn is stored on the first element of the columnArray
	 * 
	 */
	public DataTable getSampleTableDataList(String schemaName,
			String tableName, String[] columnArray, String rows, String xColumn)
			throws Exception {
		DataTable dataTable = new DataTable();
		schemaName=StringHandler.doubleQ(schemaName);
		tableName=StringHandler.doubleQ(tableName);
		
		
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection dbCon = null;
		try {
			dbCon = getConnection();
		 
 
			dataTable.setSchemaName(schemaName);
			dataTable.setTableName(tableName);
		
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < columnArray.length; i++) {
				sb.append(StringHandler.doubleQ(columnArray[i]));
				if (i < columnArray.length - 1) {
					sb.append(",");
				}
			}

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(" WHERE ");
			for (int i = 0; i < columnArray.length; i++) {
				sbWhere.append(StringHandler.doubleQ(columnArray[i])
						+ " is not null");
				if (i < columnArray.length - 1) {
					sbWhere.append(" AND ");
				}
			}

			// construct the SQL to make sure you get the right set of data if the 
			// dataset is huge
			StringBuffer sover = new StringBuffer();
			long count  =0;
			String countSql = " select count(*) from "+schemaName+"."+tableName+" "+sbWhere.toString();
			ps = dbCon.prepareStatement(countSql);
			rs = ps.executeQuery();
			rs.next();
			count = rs.getLong(1);
			rs.close();
			ps.close();
			
			sover.append("select "+sb.toString());
			sover.append(" from (select "+sb.toString()+",row_number() over (order by ");
			sover.append(StringHandler.doubleQ(xColumn)+") as myrow_number from "+schemaName+"."+tableName+" "+sbWhere+") foo where");
			sover.append(" mod(( myrow_number-1)*"+rows+","+count+")<"+rows);
			//sover.append(" mod(( myrow_number-1)*1.0,"+count+"*1.0/"+rows+")<1");
			String sql = sover.toString();
			itsLogger.debug(getClass().getName()+".getSampleTableDataList(): sql = "+sql);
			ps = dbCon.prepareStatement(sql);
			rs = ps.executeQuery();
			List<DataRow> rowList = new ArrayList<DataRow>();
			while (rs.next()) {
				String[] items = new String[columnArray.length];
				DataRow dr = new DataRow();
				for(int n=0;n<items.length;n++){
					items[n] = rs.getString(columnArray[n]);
				}
				dr.setData(items);
				rowList.add(dr);
			}
			dataTable.setRows(rowList);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		} finally {
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}

		}

		return dataTable;
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
			String[] tableTypes = {"TABLE","VIEW" };
			 
			DatabaseMetaData md =  getConnection()
					.getMetaData();
			ResultSet rsTable = md.getTables(null, schemaName, "%",
					tableTypes);
			while (rsTable.next()) {
				if (tableName.equals(rsTable.getString("TABLE_NAME"))) {
					result=true;
					break;
				 
					
				}
			}
			rsTable.close();
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
		 throw e;
		}
		return result;
	}
	
	/**
	 * @param columnValues
	 * @param sourceSchema
	 * @param sourceTable
	 * @param columnName
	 * @throws SQLException 
	 */
	public boolean loadDistinctValue(List<String> columnValues,
			String sourceSchema, String sourceTable, String columnName,int limit, String dataSourceType) throws  Exception { 
		Connection dbCon = getConnection();
		sourceSchema=StringHandler.doubleQ(sourceSchema);
		sourceTable=StringHandler.doubleQ(sourceTable);
		
		StringBuffer sb = new StringBuffer();
		ResultSet rs=null;
		boolean exceedLiminLines;
		PreparedStatement ps=null;
		try {
			Statement st = dbCon.createStatement();
			IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dataSourceType);
			long count = multiDBUtility.getSampleDistinctCount(st, sourceSchema+"."+sourceTable, StringHandler.doubleQ(columnName),null);
			exceedLiminLines = false;
			if(count> limit){
				exceedLiminLines=true;
			}

			sb = new StringBuffer();
			
			sb.append("select distinct ").append(StringHandler.doubleQ(columnName))		
			.append(" from ").append(sourceSchema).append(".").append(sourceTable);
			String sql = sb.toString();
			itsLogger.debug(getClass().getName()+".loadDistinctValue(): sql = "+sql);
			dbCon.setAutoCommit(false);
			ps = dbCon.prepareStatement(sql);
			int fetchSize = limit;
			ps.setFetchSize(fetchSize);
			  rs = ps.executeQuery();
			while (rs.next()) {
				if(exceedLiminLines==true&&fetchSize<=0)break;
				if(rs.getString(columnName)!=null
						&&rs.getString(columnName).trim().length()>0){

					columnValues.add(rs.getString(columnName));
				} 
			}
		} catch (Exception e) {
			throw  e;
		}finally{
			if (rs != null)
				rs.close();
			if(ps != null)//must close statement firstly.
				ps.close();
			if (dbCon != null)
				dbCon.setAutoCommit(true);
		}
		
		return exceedLiminLines;
	}
}
