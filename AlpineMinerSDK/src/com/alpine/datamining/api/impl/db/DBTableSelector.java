/**
 * ClassName  DBTableSelector.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.AnalyzerOutPutDBTableSelection;
import com.alpine.datamining.api.impl.DBTableSelectorConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import org.apache.log4j.Logger;
/**
 * @author John Zhao
 *
 */
public class DBTableSelector extends AbstractAnalyzer {
    private static Logger itsLogger= Logger.getLogger(DBTableSelector.class);

    /* (non-Javadoc)
	 * @see com.alpine.datamining.api.DataAnalyzer#doAnalysis(com.alpine.datamining.api.AnalyticSource)
	 */
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException  {
 
		AnalyzerOutPutDBTableSelection output=new AnalyzerOutPutDBTableSelection();
		DBTableSelectorConfig config=(DBTableSelectorConfig)source.getAnalyticConfig();
		output.setDbConenctionName(config.getDbConnectionName());
		output.setSchemaName(config.getSchemaName());
		output.setTableName(config.getTableName());
		output.setDbInfo( new DataBaseInfo(config.getSystem(), 
				config.getUrl(), config.getUserName(), config.getPassword(),config.getUseSSL()));
		
		
		
		AnalyticNodeMetaInfo nodeMetaInfo=createNodeMetaInfo(output,config.getLocale());
		 
		output.setAnalyticNodeMetaInfo(nodeMetaInfo);
		
		Connection connection = ((DataBaseAnalyticSource)source).getConnection();
		
		  try {
			boolean  exists=isTableExist(  config.getSchemaName(),   config.getTableName(),  connection );
			if(exists==false){
				throw new AnalysisError(this,AnalysisErrorName.Table_doesnot_exists, config.getLocale(),config.getSchemaName()+"."+ config.getTableName());
			}
		} catch (SQLException e) {
			throw new AnalysisException(e);
		}
		return output;
	}

	/**
	 * @param output 
	 * @return
	 */
	private AnalyticNodeMetaInfo createNodeMetaInfo(AnalyzerOutPutDBTableSelection output,Locale locale) {
	 
			AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
			nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.TABLE_SELECTOR_NAME,locale));
			nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.TABLE_SELECTOR_DESCRIPTION,locale));
		 
			Properties props=new Properties();
			props.setProperty(SDKLanguagePack.getMessage(SDKLanguagePack.DtatBase_URL,locale), output.getDbInfo().getUrl());
			props.setProperty(SDKLanguagePack.getMessage(SDKLanguagePack.DtatBase_User_Name,locale), output.getDbInfo().getUserName());
			String schemaName=output.getSchemaName();
			String tableName=output.getTableName();
			if(schemaName!=null&&schemaName.trim().length()>0){
				tableName=schemaName+"."+tableName;
			}
			props.setProperty(SDKLanguagePack.getMessage(SDKLanguagePack.Table_Name_oneline,locale), tableName);
			
			nodeMetaInfo.setProperties(props);
			return nodeMetaInfo;
		}
	 

	public boolean isTableExist(String schemaName, String tableName,Connection connection ) throws SQLException {
	 
		boolean result=false;
		try {
			String[] tableTypes = {"TABLE","VIEW" };
			 
			DatabaseMetaData md =  connection
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
}
