/**
 * ClassName FrequencyAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.FrequencyAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.attributeanalysisresult.FrequencyAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;


/**
 *Eason
 */
public class FrequencyAnalysisAnalyzer extends AbstractDBAttributeAnalyzer{
	private static Logger logger= Logger.getLogger(FrequencyAnalysisAnalyzer.class);
	
	public static final int BOOLEAN = DataType.BOOLEAN;
	
	public static final int OTHER = DataType.OTHER;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		FrequencyAnalysisResult frequencyAnalysisResult=null;
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());
			
			FrequencyAnalysisConfig config=(FrequencyAnalysisConfig)source.getAnalyticConfig();
			
			String columnNames=config.getColumnNames();
			
			List<String> columnNamesList =getSpecifiedColumn(dataSet,columnNames);
			
			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			String tableName = ((DBTable) dataSet
					.getDBTable()).getTableName();
			
			Statement st = null;
			ResultSet rs = null;
			frequencyAnalysisResult = new FrequencyAnalysisResult(
					tableName);
			Iterator<Column> i = dataSet.getColumns().allColumns();
			while (i.hasNext()) {
				Column att = i.next();
				if (!columnNamesList.contains(att.getName()))
					continue;
				if(att.getValueType()==OTHER){
					ValueFrequencyAnalysisResult valueFrequencyAnalysisResult = new ValueFrequencyAnalysisResult();
					valueFrequencyAnalysisResult.setColumnName(att.getName());
					valueFrequencyAnalysisResult.setAllNA(true);
					frequencyAnalysisResult
					.addValueFrequencyAnalysisResult(valueFrequencyAnalysisResult);
					continue;
				}
			}

			Iterator<Column> it = dataSet.getColumns().allColumns();
			long line_count=dataSet.size();
			while (it.hasNext()) {
				Column att = it.next();
				if (!columnNamesList.contains(att.getName()))
					continue;
				if(att.getValueType()==OTHER){
					continue;
				}
				//if seelct an id column, then will cause error...
				String attName = StringHandler.doubleQ(att.getName());
				StringBuilder sb_fa=new StringBuilder("select '");
				sb_fa.append(att.getName()).append("', ").append(attName);
				sb_fa.append(" , count(*)*1.0, (count(*)*1.0/");
				sb_fa.append(line_count).append(")*1.0 from ").append(tableName).append(" group by "+attName+" order by "+attName);
				
				try {
					databaseConnection.getConnection().setAutoCommit(false);
					st = databaseConnection.createStatement(false);
					int fetchSize = Integer.parseInt(AlpineMinerConfig.FREQUENCY_ANALYSIS_THRESHOLD);
					st.setFetchSize(fetchSize);
					logger.debug("FrequencyAnalysisAnalyzer.doAnalysis():sql="+sb_fa);
					rs = st.executeQuery(sb_fa.toString());
					//all record? if too many will cause java memory error... 
					long count = 0;
					while (rs.next()) {
						count++;
						if(count>Long.parseLong(AlpineMinerConfig.FREQUENCY_ANALYSIS_THRESHOLD)){
								throw new AnalysisError(this, AnalysisErrorName.Exceed_MAX_Value_Numbers,config.getLocale(),
										 new Long(AlpineMinerConfig.FREQUENCY_ANALYSIS_THRESHOLD),att.getName());//
						}
						ValueFrequencyAnalysisResult valueFrequencyAnalysisResult = new ValueFrequencyAnalysisResult();
						valueFrequencyAnalysisResult.setColumnName(rs.getString(1));
						if(rs.getString(2)!=null)
						{
							valueFrequencyAnalysisResult
							.setColumnValue(rs.getString(2));
						}
						else
						{
							valueFrequencyAnalysisResult
							.setColumnValue("null value");
							valueFrequencyAnalysisResult.setColumnValueNA(true);
						}
						valueFrequencyAnalysisResult.setCount(rs.getLong(3));
						valueFrequencyAnalysisResult.setPercentage(rs.getFloat(4));
						frequencyAnalysisResult
								.addValueFrequencyAnalysisResult(valueFrequencyAnalysisResult);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				} finally {
					try {
						rs.close();
						st.close();
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error(e);
						throw new OperatorException(e.getLocalizedMessage());
					}
					databaseConnection.getConnection().setAutoCommit(true);
				}
			}
			
		} catch (Exception e) {
			 
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			}else{
				throw new AnalysisException(e.getLocalizedMessage());
			}
		} 
		
		AnalyzerOutPutObject outPut= new AnalyzerOutPutObject(frequencyAnalysisResult);
		outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(source.getAnalyticConfig().getLocale()));
		return outPut;
	}
	
	private List<String> getSpecifiedColumn(DataSet dataSet,String columnNames) {
		Iterator<Column> ii = dataSet.getColumns().allColumns();
		List<String> columnNamesList = new ArrayList<String>();
		if (columnNames != null) {
			String[] columnNamesArray = columnNames.split(",");
			for (int i = 0; i < columnNamesArray.length; i++) {
				columnNamesList.add(columnNamesArray[i]);
			}
		} else {
			while (ii.hasNext()) {
				columnNamesList.add((ii.next()).getName());
			}
		}
		return columnNamesList;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.FREQUENCY_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.FREQUENCY_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
