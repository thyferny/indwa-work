/**
 * ClassName SQLAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.execute;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.SQLAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.file.StringUtil;

public class SQLAnalyzer extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(SQLAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		Statement st=null;
		try {
			SQLAnalysisConfig config=(SQLAnalysisConfig)source.getAnalyticConfig();
			String sqlClause=config.getSqlClause();

			Connection connection = ((DataBaseAnalyticSource)source).getConnection();
			
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			logger.debug("SQLAnalyzer.doAnalysis():sqlClause="+sqlClause);
			String[] sqlClauses=sqlClause.split(";");
			for(int i=0;i<sqlClauses.length;i++){
				logger.debug("SQLAnalyzer.doAnalysis():sqlClauses["+i+"]="+sqlClauses[i]);
				if(!StringUtil.isEmpty(sqlClauses[i].trim())){
					st.execute(sqlClauses[i]);
				}
			}
			 AnalyzerOutPutObject output=new AnalyzerOutPutObject(null);
			 output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			 return  output;
		}  catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e);
			}
		}finally{
			if(st!=null){
				try {
					st.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		}
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SQLEXECUTE_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SQLEXECUTE_DESCRIPTION,locale)); 
		return nodeMetaInfo;
	}
}
