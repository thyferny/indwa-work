/**
 * ClassName AbstractVisualizationGenerator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.visual;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.algoconf.CopyToDBConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.CopyToDBAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopBarChartAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 *
 */
public abstract class AbstractVisualizationType implements VisualizationType {
    private static final Logger abslogger =Logger.getLogger(AbstractVisualizationType.class);

    protected Locale locale = Locale.getDefault();

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationType#generateOutPutGenerator(com.alpine.datamining.api.AnalyticOutPut)
	 */
 
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationType#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return null;
	}
	
	public DataTable getResultTableSampleRow(AnalyticOutPut analyzerOutPut,
		String schemaName, String tableName) throws AnalysisException {
		if(analyzerOutPut.getAnalyticNode().getAnalyzerClass().equals(HadoopBarChartAnalyzer.class.getName())) {
			AnalyzerOutPutTableObject tableObject=(AnalyzerOutPutTableObject) analyzerOutPut;
			return tableObject.getDataTable();
		}
		Connection connection = null;
		String dbType=null;
		AnalyticSource analyticSource = analyzerOutPut.getDataAnalyzer().getAnalyticSource();
		if(analyticSource instanceof DataBaseAnalyticSource){
			connection = ((DataBaseAnalyticSource)analyticSource).getConnection();
			dbType=((DataBaseAnalyticSource)analyticSource).getDataSourceType();
		}else{//especial for CopytoDBOperator
			CopyToDBAnalyzer copyToDBAnalyzer=(CopyToDBAnalyzer)analyzerOutPut.getDataAnalyzer();
			connection = copyToDBAnalyzer.getConnection();
			CopyToDBConfig config = (CopyToDBConfig)analyticSource.getAnalyticConfig();
			dbType = config.getSystem();
		}


		DataTable dataTable = new DataTable();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		dataTable.setSchemaName(schemaName);
		dataTable.setTableName(tableName);
		
		String outputTableName=StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName);
		
		String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
		
		IDataSourceInfo dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(dbType);
		 
		String sql = dataSourceInfo.createSelectSql(outputTableName,maxRows);
		
		abslogger.debug("AbstractVisualizationType.getResultTableSampleRow():sql=" + sql);
		
		try {
			ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);;
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			List<DataRow> list = new ArrayList<DataRow>();
			while(rs.next()){
				DataRow dr = new DataRow();
				String[] items = new String[columnCount];
				for(int i=0; i<columnCount; i++){
					if (DataTypeConverter.isDoubleType(rsmd.getColumnType(i+1))) {
						items[i] = AlpineUtil.dealNullValue(rs,i+1);
					}else if(dbType.equals(DataSourceInfoOracle.dBType)&&DataTypeConverter.isArrayArrayColumnType(rsmd.getColumnTypeName(i+1), dbType)){
						items[i] = AlpineUtil.dealArrayArray(rs, i+1);
					}else if(dbType.equals(DataSourceInfoOracle.dBType)&&DataTypeConverter.isArrayColumnType(rsmd.getColumnTypeName(i+1), dbType)){
						items[i] = AlpineUtil.dealArray(rs, i+1);
					}else {
						items[i] = rs.getString(i+1);
					}
				}
				dr.setData(items);
				list.add(dr);
			}
			dataTable.setRows(list);
			
			List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
			TableColumnMetaInfo dc;
			for (int i=0; i<columnCount; i++) {
				dc = new TableColumnMetaInfo(rsmd.getColumnName(i+1),
						DataTypeConverterUtil.getColumnType(rsmd.getColumnType(i+1)));
				columns.add(dc);
			}
			dataTable.setColumns(columns);
		} catch (SQLException e) {	 
			abslogger.error(e.getMessage(), e);
			throw new AnalysisException(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				abslogger.error(e.getMessage(), e);
			}			
		}
		return dataTable;
	}
	

}
