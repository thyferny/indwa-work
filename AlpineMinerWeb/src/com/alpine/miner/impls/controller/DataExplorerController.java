/**
 * ClassName  DataExplorerController.java
 *
 * Version information: 3.0
 *
 * Data: 2011-6-21
 *
 * COPYRIGHT (C) 2011  Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.AlpineMinerSDK;
import com.alpine.datamining.api.impl.algoconf.TableScatterMatrixConfig;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.table.TableScatterMatrixAnalyzer;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.dataexplorer.ClearFileInfo;
import com.alpine.miner.impls.dataexplorer.ClearTableInfo;
import com.alpine.miner.impls.dataexplorer.DataExplorerManager;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.web.resource.WebWorkFlowStepRunner;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.miner.workflow.runner.FlowRunningHelper;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.log.LogPoster;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author John Zhao
 * 
 */
@Controller
@RequestMapping("/main/dataexplorer.do")
public class DataExplorerController extends AbstractControler {
    private static Logger itsLogger = Logger.getLogger(DataExplorerController.class);

    DataExplorerManager dm= DataExplorerManager.INSTANCE;

	public DataExplorerController() throws Exception{
		//this is important
		super();
		AlpineMinerSDK.init();
	}

	@RequestMapping(params = "method=getTableData", method = RequestMethod.GET)
	public void getTableData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName, String resourceType,
			String isGeneratedTable, ModelMap model) throws IOException {
		itsLogger.debug(LogUtils.entry(
                "DataExplorerController", "getTableData",
                " dbConnName=" + dbConnName + " dbSchemaName=" + dbSchemaName
                        + " dbTableName=" + dbTableName));
		try {
			String user = getUserName(request);
			dbConnName = getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			VisualizationModelDataTable tableModel = dm.getTableDataVModel(
					user, dbConnName, dbSchemaName, dbTableName,
					resourceType, isGeneratedTable);
			writeModel(response, tableModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableData_DB", user);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}
	//see getCorrelationData
	@RequestMapping(params = "method=getScatPlotMartixData", method = RequestMethod.GET)
	public void getScatPlotMartixData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex, String resourceType,String isGeneratedTable,ModelMap model) throws IOException {
        itsLogger.debug(LogUtils.entry(
				"DataExplorerController",	"getTableData",
				" dbConnName=" + dbConnName + " dbSchemaName=" + dbSchemaName
				+ " dbTableName=" + dbTableName));
		try {
			String user = getUserName(request);
			dbConnName = getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			
			TableScatterMatrixAnalyzer analyzer= new TableScatterMatrixAnalyzer();
			TableScatterMatrixConfig config=new TableScatterMatrixConfig();

			config.setLocale(request.getLocale());
			config.setColumnNames(columnNameIndex);
			
			VisualizationModel visualizationModel = dm.runAnalyzerForVisual(user, dbConnName, dbSchemaName, dbTableName, config, analyzer, resourceType, isGeneratedTable, request.getLocale());
			
			writeModel(response, visualizationModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getScatPlotMartixData_DB", user);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
		
	}
	
	
 
	@RequestMapping(params = "method=getBarchartData", method = RequestMethod.GET)
	public void getBarchartData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String valueDomain,		String scopeDomain,
			String categoryType,	String resourceType,String isGeneratedTable,	ModelMap model)
			throws IOException {

        itsLogger.debug(LogUtils.entry("DataExplorerController", "getBarchartData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" valueDomain="+valueDomain+ " scopeDomain="+scopeDomain+" categoryType="+categoryType)) ;
		try {
		
			String user = getUserName(request);
			Locale locale = request.getLocale();  
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			VisualizationModel visualModel = dm.getBarchartVModel(dbConnName,
					dbSchemaName, dbTableName, valueDomain, scopeDomain,
					categoryType, resourceType, isGeneratedTable, user, locale);
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getBarchartData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	@RequestMapping(params = "method=getScatterData", method = RequestMethod.GET)
	public void getScatterData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName, String analysisColumn,
			String referenceColumn, String categoryColumn,String referenceColumnType, String resourceType,
			String isGeneratedTable, ModelMap model) throws IOException {
        itsLogger.debug(LogUtils.entry(
						"DataExplorerController",
						"getScatterData",
						" dbConnName=" + dbConnName + " dbSchemaName="
								+ dbSchemaName + " dbTableName=" + dbTableName
								+ " analysisColumn=" + analysisColumn
								+ " referenceColumn=" + referenceColumn
								+ " categoryColumn=" + categoryColumn));
		try {
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			VisualizationModelScatter scatterModel = dm.getScatterVModel( 
					dbConnName, dbSchemaName, dbTableName, analysisColumn,
					referenceColumn, categoryColumn, referenceColumnType,resourceType,
					isGeneratedTable, getUserName(request),request.getLocale());
			
			
			writeModel(response, scatterModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getScatterData_DB", getUserName(request));
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	 


	@RequestMapping(params = "method=getBoxAndWiskData", method = RequestMethod.GET)
	public void getBoxAndWiskData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String analysisValue,			String analysisSeries,
			String analysisType,	String resourceType,	String isGeneratedTable,	ModelMap model)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getBoxAndWiskData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" analysisValue="+analysisValue+ " analysisSeries="+analysisSeries+" analysisType="+analysisType)) ;
		try {
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			String user = getUserName(request);
 			Locale locale = request.getLocale();
			VisualizationModel visualModel = dm.getBoxWhiskerVModel(dbConnName,
					dbSchemaName, dbTableName, analysisValue, analysisSeries,
					analysisType, resourceType, isGeneratedTable, user, locale);
			
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getBoxAndWiskData_DB", user);
		} catch (Exception e) {
			//
			if(e instanceof AnalysisError){
				AnalysisError er = (AnalysisError) e;
				if(er.getMessage().contains("{0}")){
					er.setMessage(er.getErrorMsg());
				}
				generateErrorDTO(response, er, request.getLocale()) ;	
			}else{
				generateErrorDTO(response, e, request.getLocale()) ;	
			}
		}

	}


	
	
	@RequestMapping(params = "method=getTimeSeriesData", method = RequestMethod.GET)
	public void getTimeSeriesData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String idColumn,String valueColumn,String  groupByColumn,String resourceType,String isGeneratedTable,	ModelMap model)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getTimeSeriesData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" idColumn="+idColumn+ " valueColumn="+valueColumn)) ;

		try {
			String user = getUserName(request);
			Locale locale = request.getLocale(); 
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			VisualizationModel tableModel = dm.getTimeSeriesVModel(dbConnName,
					dbSchemaName, dbTableName, idColumn, valueColumn,
					groupByColumn, resourceType, isGeneratedTable, user, locale);
			
			writeModel(response, tableModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTimeSeriesData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	
	private void writeModel(HttpServletResponse response,
			VisualizationModel  visualModel,Locale locale) throws Exception { 
		String str = JSONUtility.toJSONString(visualModel,locale);
		
		ProtocolUtil.sendResponse(response, str);
	}


	
	
	@RequestMapping(params = "method=getColumnNamesWithType", method = RequestMethod.GET)
	public String getColumnNamesWithType(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String resourceType,String dbTableName,String columnType,String isGeneratedTable,
			ModelMap model) throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getColumnNamesWithType",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName
				+" dbTableName="+dbTableName+" columnType="+columnType));
 		
		try {
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			String user = getUserName(request);
			dbTableName = VisualUtils.refineTableName(dbSchemaName, dbTableName, user,  isGeneratedTable);
			
			  //MINER_WEB735  always get the latest column
			ProtocolUtil.sendResponse(response, 
					DBUtil.getColumnListWithType(getUserName(request),dbConnName, dbSchemaName, dbTableName,columnType, super.getResourceType(resourceType)));
		} catch (Exception e) {
			e.printStackTrace();
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

		return null;
	}
	
	
	

	@RequestMapping(params = "method=getUniverrateData", method = RequestMethod.GET)
	public void getUniverrateData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex ,String referenceColumn,	String resourceType,String isGeneratedTable,ModelMap model)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getUniverrateData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" columnNameIndex="+columnNameIndex+ " referenceColumn="+referenceColumn)) ;

		try {
			String user = getUserName(request);
			dbConnName=getUTFParamvalue(dbConnName,request) ;	
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			Locale locale = request.getLocale();
			
			VisualizationModelComposite visualModel = dm.getUniverateVModel(
					dbConnName, dbSchemaName, dbTableName, columnNameIndex,
					referenceColumn, resourceType, isGeneratedTable, user,locale);
			
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getUniverrateData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}


	


	@RequestMapping(params = "method=getStatisticsData", method = RequestMethod.GET)
	public void getStatisticsData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex ,	String resourceType, ModelMap model,String isGeneratedTable)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getStatisticsData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" columnNameIndex="+columnNameIndex )) ;

		try {
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			String user = getUserName(request);
			Locale locale = request.getLocale() ;
			VisualizationModel visualModel = dm.getStatisticsVModel(dbConnName,
					dbSchemaName, dbTableName, columnNameIndex, resourceType,
					isGeneratedTable, user, locale);
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getStatisticsData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}
 
	
	@RequestMapping(params = "method=getFrequencyData", method = RequestMethod.GET)
	public void getFrequencyData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex , String resourceType,ModelMap model , String isGeneratedTable)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getFrequencyData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" columnNameIndex="+columnNameIndex )) ;

		try {
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			String user = getUserName(request);
			Locale locale = request.getLocale() ;
			
			VisualizationModel visualModel = dm.getFrequencyVModel(dbConnName,
					dbSchemaName, dbTableName, columnNameIndex, resourceType,
					isGeneratedTable, user, locale);
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getFrequencyData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}


 

	//columnNameIndex  0_1_2_3_4
	@RequestMapping(params = "method=getCorrelationData", method = RequestMethod.GET)
	public void getCorrelationData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex, String resourceType,String isGeneratedTable,ModelMap model)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getCorrelationData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" columnNameIndex="+columnNameIndex  )) ;

		try {
			String user = getUserName(request);
			Locale locale = request.getLocale();
			dbConnName=getUTFParamvalue(dbConnName,request) ;	
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			VisualizationModel visualModel = dm.getCorrelationVModel(dbConnName,
					dbSchemaName, dbTableName, columnNameIndex, resourceType,
					isGeneratedTable, user, locale);
			
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getCorrelationData_DB", user);
		} catch (Exception e) {

			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}


	
	@RequestMapping(params = "method=getHistogramData", method = RequestMethod.GET)
	public void getHistogramData(HttpServletRequest request,
			HttpServletResponse response, String dbConnName,
			String dbSchemaName, String dbTableName,
			String columnNameIndex,String columnBins,String resourceType, String isGeneratedTable, ModelMap model)
			throws IOException {
        itsLogger.debug(LogUtils.entry("DataExplorerController", "getHistgramData",
				" dbConnName="+dbConnName+ " dbSchemaName="+dbSchemaName+" dbTableName="+dbTableName+
				" columnNameIndex="+columnNameIndex + " columnBins="+columnBins));

		try {
			String user = getUserName(request);
			Locale locale = request.getLocale();
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			AnalysisColumnBin[] columnBinArray = ProtocolUtil.toObject(columnBins, AnalysisColumnBin[].class);
			VisualizationModel visualModel = dm.getHistogramVModel(dbConnName,
					dbSchemaName, dbTableName, columnNameIndex, columnBinArray,
					resourceType, isGeneratedTable, user, locale);
			
			writeModel(response, visualModel,request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getHistogramData_DB", user);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}
	
	
	@RequestMapping(params = "method=clearInterspaceTable", method = RequestMethod.POST)
	public void clearInterspaceTable(String runnerUUID, HttpServletRequest request,
			HttpServletResponse response,
	 		ModelMap model) throws Exception{

		ClearTableInfo[] parameter= ProtocolUtil.getRequest(request, ClearTableInfo[].class);
		StringBuilder sb = new StringBuilder();
		String needAddPrefix = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX);
		String userName = getUserName(request);
		for(ClearTableInfo item : parameter){
			try {
				if("FILE".equals(item.getOutputType())){
					ClearFileInfo fileInfo = new ClearFileInfo();
					fileInfo.setConnectionName(item.getConnectionName());
					fileInfo.setFilePath(item.getTableName());
					fileInfo.setId(item.getId());
					fileInfo.setOperatorName(item.getOperatorName());
					fileInfo.setResourceType(item.getResourceType());
					fileInfo.setUuid(item.getUuid());
					dm.clearIntermediaryFile(userName, fileInfo);
				}else{
					dm.clearIntermediaryTable(userName,item);
				}
				//this is special used to clear the step run result
				FlowRunningHelper.getInstance().setOperatorFinished(item.getUuid(), false,userName);
				clearStepRunResult(userName, item, runnerUUID);
			} catch (Exception e) {
				String tableName ="true".equals(needAddPrefix) ? VisualUtils.refineTableName(item.getSchemaName(), item.getTableName(), userName, needAddPrefix) : item.getTableName();
				sb.append(item.getOutputType() + " " + tableName + ", ");
			}
		}
		if(sb.length() > 1){
			String msg =ErrorNLS.getMessage(ErrorNLS.Can_Not_Drop, request.getLocale(),new String[]{sb.substring(0, sb.length() - 2)});
			//generateErrorDTO(response, msg, request.getLocale());
			//MINERWEB-870
			ProtocolUtil.sendResponse(response, new ErrorDTO(333,msg));
		}else{
			returnSuccess(response);
		}
	}
	
	private void clearStepRunResult(String userName, ClearTableInfo tableInfo, String runnerUUID){
		WebWorkFlowStepRunner stepRunner = FlowRunningHelper.STEP_RUNNER_MAP.remove(userName);
		 if(stepRunner!=null){
			 stepRunner.dispose() ;
		}
	}
 
	
	@RequestMapping(params = "method=getTableMetadata", method = RequestMethod.GET)
	public void getTableMetadata(HttpServletRequest request,
			HttpServletResponse response,
			String dbConnName,
			String dbSchemaName,
			String dbTableName,
			String operatorClass,
			String resourceType,
			ModelMap model) throws IOException{
		
		try {
			String userName = getUserName(request);
			dbConnName=getUTFParamvalue(dbConnName,request) ;
			dbTableName = getUTFParamvalue(dbTableName,request) ;
			DbConnectionInfo dbinfo = ResourceManager.getInstance().getDBConnection(getUserName(request), dbConnName,super.getResourceType(resourceType));
			
			String needAddPrefix = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_DB,PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX);
			String tableName ="true".equals(needAddPrefix) && !"DbTableOperator".equals(operatorClass) ? VisualUtils.refineTableName(dbSchemaName, dbTableName, userName, needAddPrefix) : dbTableName;
			
			DBMetadataManger.INSTANCE.refreshSchema(dbinfo.getConnection(), dbSchemaName, true);
			DBMetadataManger.INSTANCE.refreshTable(dbinfo.getConnection(), dbSchemaName, tableName, true);
			List<TableColumnMetaInfo> columnInfoList = DBMetadataManger.INSTANCE.getTableColumnInfoList( dbinfo.getConnection(),   dbSchemaName,   tableName);
			List<Map<String,String>> result = new ArrayList<Map<String,String>>();
			if(columnInfoList!=null){
		
			for(TableColumnMetaInfo column : columnInfoList){
					Map<String,String> columnItem = new HashMap<String,String>();
					columnItem.put("columnName", column.getColumnName());
					columnItem.put("columnType", column.getColumnsType());
					result.add(columnItem);
				}
			}
			ProtocolUtil.sendResponse(response, result);
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableMetadata", getUserName(request));
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		} 
	}
 	
}