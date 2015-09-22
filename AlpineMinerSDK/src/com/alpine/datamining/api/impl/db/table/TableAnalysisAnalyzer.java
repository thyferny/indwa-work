/**
 * ClassName TableAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;
//this is barchart!
public class TableAnalysisAnalyzer extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(TableAnalysisAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
 
		DataTable dataTable = new DataTable();
		fillDataTableMetaInfo(source, dataTable);
		PreparedStatement ps = null;
		DatabaseConnection databaseConnection=null;
		ResultSet rs = null;
		try {
			BarChartAnalysisConfig config = (BarChartAnalysisConfig) source.getAnalyticConfig();
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
			
			databaseConnection=((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
			
			String valueDomain = StringHandler.doubleQ(config.getValueDomain());
			String scopeDomain = StringHandler.doubleQ(config.getScopeDomain());
			String categoryType = StringHandler.doubleQ(config.getCategoryType());
			
			boolean containScopeAndCategory = false;
			
			if(StringUtil.isEmpty(config.getCategoryType())==false
					&&StringUtil.isEmpty(config.getScopeDomain())==false){
				containScopeAndCategory = true;
			}else{
				if(StringUtil.isEmpty(config.getCategoryType())==false){
					scopeDomain = categoryType;
				}
			}
			
			StringBuilder where_sb=new StringBuilder(" where ");
			where_sb.append(valueDomain).append(" IS NOT NULL AND ");
			where_sb.append(scopeDomain).append(" IS NOT NULL ");
			if(containScopeAndCategory){
				where_sb.append(" AND ").append(categoryType).append("IS NOT NULL");
			}
			StringBuilder sql_sb=new StringBuilder(" select ");
			
			if(containScopeAndCategory){
				if(!categoryType.equals(scopeDomain))
				{
					sql_sb.append(categoryType).append(",").append(scopeDomain).append(",sum(");

				}else
				{
					sql_sb.append(categoryType).append(",").append(scopeDomain).append(" as ");
					sql_sb.append(StringHandler.doubleQ(config.getScopeDomain()+System.currentTimeMillis())).append(",sum(");
				}

				sql_sb.append(valueDomain).append(")");
				if(config.getValueDomain().equals("sum")){
					sql_sb.append(" as ").append(StringHandler.doubleQ("alpine_sum"));
				}
				sql_sb.append(" from ").append(tableName).append(where_sb).append(" group by ");
				sql_sb.append(categoryType).append(",").append(scopeDomain).append(" order by ").append(categoryType);
				sql_sb.append(",").append(scopeDomain);
			}else{
				sql_sb.append(scopeDomain).append(",sum(");
				sql_sb.append(valueDomain).append(")");
				if(config.getValueDomain().equals("sum")){
					sql_sb.append(" as ").append(StringHandler.doubleQ("alpine_sum"));
				}
				sql_sb.append(" from ").append(tableName).append(where_sb).append(" group by ");
				sql_sb.append(scopeDomain).append(" order by ").append(scopeDomain);
			}
			
			databaseConnection.getConnection().setAutoCommit(false);
            logger.debug("TableAnalysisAnalyzer.doAnalysis():sql="+sql_sb);
			ps = databaseConnection.createPreparedStatement(sql_sb.toString(), false);
			int fetchSize = Integer
			.parseInt(AlpineMinerConfig.TABLE_ANALYSIS_THRESHOLD);
			ps.setFetchSize(fetchSize);
			rs = ps.executeQuery();
			List<DataRow> list = new ArrayList<DataRow>();
			long number = 0;
			while(rs.next()){
				number++;
				if(number>Integer.parseInt(AlpineMinerConfig.TABLE_ANALYSIS_THRESHOLD)){
					throw new AnalysisError(this, AnalysisErrorName.Exceed_MAX_Bar_Numbers,config.getLocale(),
							AlpineMinerConfig.TABLE_ANALYSIS_THRESHOLD);//
				}
				DataRow dr = new DataRow();
				if(containScopeAndCategory){
					dr.setData(new String[]{rs.getString(3),rs.getString(2),rs.getString(1)});
				}else{
					dr.setData(new String[]{rs.getString(2),rs.getString(1)});
				}	
				list.add(dr);
			}
			dataTable.setRows(list);
			List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
			TableColumnMetaInfo dc1 = new TableColumnMetaInfo(valueDomain,"");
		 
			TableColumnMetaInfo dc2 = new TableColumnMetaInfo(scopeDomain,"");
		 
			columns.add(dc1);
			columns.add(dc2);
			if(containScopeAndCategory){
				TableColumnMetaInfo dc3 = new TableColumnMetaInfo(categoryType,"");
				columns.add(dc3);
			}
			
			dataTable.setColumns(columns);
			AnalyzerOutPutTableObject tableOutput = new AnalyzerOutPutTableObject();
			tableOutput.setDataTable(dataTable);
			tableOutput.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return tableOutput;
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			}else{
				throw new AnalysisException(e);
			}
		 
		}finally{
			try {
				if(rs!=null) rs.close();
				if(ps!=null) ps.close();
				databaseConnection.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				logger.error(e );
				throw new AnalysisException(e);
			}
		}
	}


	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.BAR_CHART_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.BAR_CHART_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
