/**
 * ClassName :DataExplorerManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-25
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.dataexplorer;

import java.io.IOException;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;

/**
 * @author zhaoyong
 *
 */
public interface DataExplorerManager {

	public static final int SCATTER_CATEGORY_INDEX = 2;
	// max points number per group for time series
	//max is 30 ,default is 30 ...
//	public static final int TIME_SERIES_CHART_MAX = 30;  
	//max is 200 ,default is 60 ...
//	public static final int SCATTER_POINTS_MAX = 60;
	
//	/public static final int CLUSTER_POINTS_MAX = 60;
	
	
	
	public static final DataExplorerManager INSTANCE = new DataExplorerManagerImpl();

	/**
	 * @param user
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param resourceType
	 * @param isGeneratedTable
	 * @return
	 */
	VisualizationModelDataTable getTableDataVModel(String user,
			String dbConnName, String dbSchemaName, String dbTableName,
			String resourceType, String isGeneratedTable) throws  Exception;

 
	/**
	 * @param user
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param config
	 * @param analyzer
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	VisualizationModel runAnalyzerForVisual(String user, String dbConnName,
			String dbSchemaName, String dbTableName,
			AnalyticConfiguration config, AbstractDBAnalyzer analyzer,
			String resourceType, String isGeneratedTable, Locale locale) 
			throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param valueDomain
	 * @param scopeDomain
	 * @param categoryType
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getBarchartVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String valueDomain,
			String scopeDomain, String categoryType, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param analysisValue
	 * @param analysisSeries
	 * @param analysisType
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getBoxWhiskerVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String analysisValue,
			String analysisSeries, String analysisType, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param columnNameIndex
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getStatisticsVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale) throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param columnNameIndex
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getFrequencyVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale)throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param columnNameIndex
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	VisualizationModel getCorrelationVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String resourceType, String isGeneratedTable, String user,
			Locale locale) throws Exception;


	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param columnNameIndex
	 * @param columnBins
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getHistogramVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			AnalysisColumnBin[] columnBins, String resourceType, String isGeneratedTable,
			String user, Locale locale) throws Exception;
 

	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param idColumn
	 * @param valueColumn
	 * @param groupByColumn
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale
	 * @return
	 */
	VisualizationModel getTimeSeriesVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String idColumn,
			String valueColumn, String groupByColumn, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception;


	/**
	 * @param userName
	 * @param parameter
	 * @throws Exception 
	 */
	void clearIntermediaryTable(String userName, ClearTableInfo parameter) throws Exception;

	void clearIntermediaryFile(String userName, ClearFileInfo parameter) throws Exception;

	/**
	 * @param dbConnName
	 * @param dbSchemaName
	 * @param dbTableName
	 * @param columnNameIndex
	 * @param referenceColumn
	 * @param resourceType
	 * @param isGeneratedTable
	 * @param user
	 * @param locale 
	 * @return
	 */
	VisualizationModelComposite getUniverateVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String columnNameIndex,
			String referenceColumn, String resourceType,
			String isGeneratedTable, String user, Locale locale) throws Exception;


	VisualizationModelScatter getScatterVModel(String dbConnName,
			String dbSchemaName, String dbTableName, String analysisColumn,
			String referenceColumn, String categoryColumn,
			String referenceColumnType, String resourceType,
			String isGeneratedTable, String user, Locale locale)
			throws Exception, IOException;           

}
