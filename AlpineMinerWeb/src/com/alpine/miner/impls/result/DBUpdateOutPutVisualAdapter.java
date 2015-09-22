/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * DBUpdateOutPutVisualAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.db.DBTable;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public abstract class DBUpdateOutPutVisualAdapter extends AbstractOutPutVisualAdapter {
    private static Logger itsLogger = Logger.getLogger(DBUpdateOutPutVisualAdapter.class);

    protected void fillDataTable(DataTable dataTable,
			AnalyzerOutPutDataBaseUpdate outPut) {
 
 
		Connection conn  = outPut.getConnection();
		String tableName = outPut.getTableName();
		if (outPut.getSchemaName() != null
				&& outPut.getSchemaName().trim().length() > 0) {
			// tableName=outPut.getSchemaName()+"."+tableName;
			tableName = StringHandler.doubleQ(outPut.getSchemaName()) + "."
					+ StringHandler.doubleQ(tableName);
		}

		fillDataTable(dataTable, outPut, tableName, conn);
	}

	protected void fillDataTable(DataTable dataTable,
			AnalyticOutPut outPut, String tableName,
			Connection conn) {
		String dbType=outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
		PreparedStatement ps = null;
		ResultSet rs = null;
	 
		if (tableName==null||tableName.equals(""))
			return;
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		int fetchSize = super.getTableMaxRows();
		String sql= dataSourceInfo.createSelectSql(tableName,   String.valueOf(fetchSize));
		
		itsLogger.debug(
				getClass().getName() + ".generateOutPut(): sql = " + sql);
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			ps.setFetchSize(fetchSize);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			List<TableColumnMetaInfo> columns = VisualUtils.buildColumns(rsmd);
			dataTable.setColumns(columns);
			
			fillDataTables(dataTable,  rs, fetchSize, rsmd,outPut);
			DBUtil.reSetColumnType(dbType, dataTable) ;
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		} finally {
			try {
			
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.setAutoCommit(true);
				// never close the connection here, the system will close it 

			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
			}

		}
	}
	
	protected void fillDataTables(DataTable dataTable,  
			ResultSet rs, int fetchSize, ResultSetMetaData rsmd, AnalyticOutPut outPut)
			throws SQLException {
		String dbType = ((DBTable) (( AnalyzerOutPutDataBaseUpdate)outPut).getDataset().getDBTable())
		.getDatabaseConnection().getProperties().getName();
		int count = rsmd.getColumnCount();
		List<DataRow> rows = new ArrayList<DataRow>();

		while (rs.next() && fetchSize > 0) {
			fetchSize--;
			String[] items = buildRowItems(dbType, rs, rsmd, count);
			DataRow row = new DataRow();
			row.setData(items);
			rows.add(row);

		}

		dataTable.setRows(rows);
		 
	}

	private String[] buildRowItems(String dbType, ResultSet rs,
			ResultSetMetaData rsmd, int count) throws SQLException {
		String[] items = new String[count];
		for (int i = 0; i < count; i++) {

			if (DataTypeConverter.isDoubleType(rsmd
					.getColumnType(i + 1))) {
				items[i] = AlpineUtil.dealNullValue(rs, i + 1);
			} else if (dbType.equals(DataSourceInfoOracle.dBType)
					&& DataTypeConverter.isArrayArrayColumnType(
							rsmd.getColumnTypeName(i + 1), dbType)) {
				items[i] = AlpineUtil.dealArrayArray(rs, i + 1);
			} else if (dbType.equals(DataSourceInfoOracle.dBType)
					&& DataTypeConverter.isArrayColumnType(
							rsmd.getColumnTypeName(i + 1), dbType)) {
				items[i] = AlpineUtil.dealArray(rs, i + 1);
			} else {
				items[i] = rs.getString(i + 1);
			}
		}
		return items;
	}
}
