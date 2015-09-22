package com.alpine.datamining.api.impl.db.table;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableScatterConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatter;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class TableScatterAnalyzer extends AbstractDBAnalyzer{
	private static Logger logger= Logger.getLogger(TableScatterAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		TableScatterConfig config = (TableScatterConfig) source.getAnalyticConfig();
	 
		String schemaName =((DataBaseAnalyticSource)source).getTableInfo().getSchema(); 
		String tableName= ((DataBaseAnalyticSource)source).getTableInfo().getTableName(); 
		String dbSystem = ((DataBaseAnalyticSource)source).getDataBaseInfo().getSystem(); 
		Connection conn = ((DataBaseAnalyticSource)source).getConnection();
		DBDataUtil dbd = new DBDataUtil(conn, dbSystem);
		dbd.setLocale(config.getLocale());
			
		DataTable dt = null;
		try {
			String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
			
			if(config.getCategoryColumn() == null || config.getCategoryColumn().equals("")){
				dt = dbd.getSampleTableDataList(schemaName,
						tableName,new String[]{config.getColumnY(),config.getColumnX()},maxRows, 
						config.getColumnX());
			}else{
				dt = dbd.getSampleTableDataList(schemaName,
						tableName,new String[]{config.getColumnY(),config.getColumnX(),config.getCategoryColumn()},maxRows, 
						config.getColumnX());
			}
			
			AnalyzerOutPutScatter output = new AnalyzerOutPutScatter();
			output.setDataTable(dt);
			output.setDependentColumn(config.getColumnY());
			output.setReferenceColumn(config.getColumnX());
			output.setReferenceColumnType(config.getReferenceType());
			output.setCategoryColumn(config.getCategoryColumn());
			return output;
		} catch (Exception e) {
			logger.error(e );
			throw new AnalysisException(e);
		}
	}

}
