package com.alpine.miner.impls.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.datamining.api.AlpineMinerSDK;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.dataexplorer.HadoopDataExplorerManager;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceFlowManager;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.hadoop.HadoopFileOperator;
import com.alpine.miner.workflow.operator.parameter.AlpineLogFileStructureModel;
import com.alpine.miner.workflow.operator.parameter.CSVFileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.JSONFileStructureModel;
import com.alpine.miner.workflow.operator.parameter.XMLFileStructureModel;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.util.VisualUtils;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.log.LogPoster;

/**
 * ClassName: HadoopDataExplorerController
 * <p/>
 * Data: 12-8-30
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
@Controller
@RequestMapping("/main/hadoopDataexplorer.do")
public class HadoopDataExplorerController extends AbstractControler {
    HadoopDataExplorerManager hdDataManager = HadoopDataExplorerManager.INSTANSE;

    public HadoopDataExplorerController() throws Exception {
        //this is important
        super();
        AlpineMinerSDK.init();
    }

    @RequestMapping(params="method=getBoxAndWiskData4Hadoop", method=RequestMethod.GET)
    public void getBoxAndWiskData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String analysisValue,String analysisSeries,String analysisType, String useApproximation,
                                       String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");
        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        try {
            String user = super.getUserName(request);//request.getRemoteUser();
            Locale locale = request.getLocale();
            VisualizationModel visualModel = hdDataManager.getBoxAndWhiskersVModel(connectionInfo.getConnection(),hdfsm,hadoopFileFormat ,hadoopFilePath, analysisValue,
                    analysisSeries, analysisType, useApproximation,resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getBoxAndWiskData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }


    @RequestMapping(params="method=getBarchartData4Hadoop", method=RequestMethod.GET)
    public void getBarchartData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String valueDomain,String scopeDomain,String categoryType,
                                       String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");

        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        try {
            String user = super.getUserName(request);
            Locale locale = request.getLocale();
            VisualizationModel visualModel = hdDataManager.getBarchartVModel(connectionInfo.getConnection(),hdfsm,hadoopFileFormat ,hadoopFilePath, valueDomain,
                    scopeDomain, categoryType, resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getBarchartData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }
    @RequestMapping(params="method=getScatterPlotMatrixData4Hadoop", method=RequestMethod.GET)
    public void getScatterPlotMatrixData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String columnNameIndex, String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");

        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        try {
            String user = super.getUserName(request);
            Locale locale = request.getLocale();
            VisualizationModel visualModel = hdDataManager.getScatterPlotMatrixVModel(connectionInfo.getConnection(), hdfsm, hadoopFileFormat, hadoopFilePath, columnNameIndex,resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getScatPlotMartixData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }
    @RequestMapping(params="method=getHistogramData4Hadoop", method=RequestMethod.GET)
    public void getHistogramData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String columnNameIndex,String columnBins, String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");

        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        try {
            String user = super.getUserName(request);
            Locale locale = request.getLocale();
            AnalysisColumnBin[] columnBinArray = ProtocolUtil.toObject(columnBins, AnalysisColumnBin[].class);
            VisualizationModel visualModel = hdDataManager.getHistogramVModel(connectionInfo.getConnection(), hdfsm, hadoopFileFormat, hadoopFilePath, columnBinArray, resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getHistogramData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }

    @RequestMapping(params="method=getFrequencyData4Hadoop", method=RequestMethod.GET)
    public void getFrequencyData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String columnNameIndex,String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");

        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            String user = super.getUserName(request);
            Locale locale = request.getLocale();
            VisualizationModel visualModel = hdDataManager.getFrequencyVModel(connectionInfo.getConnection(), hdfsm, hadoopFileFormat, hadoopFilePath,columnNameIndex, resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getFrequencyData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }

    @RequestMapping(params="method=getStatisticsData4Hadoop", method=RequestMethod.GET)
    public void getStatisticsData4Hadoop(String hadoopConnectKey,String hadoopFilePath,String operatorUID,String flowInfoKey,
                                       String columnNameIndex,String resourceType,boolean isHDFileOperator,HttpServletRequest request, HttpServletResponse response) throws IOException{


        Map<String, Object> container = new HashMap<String, Object>();
        hadoopFilePath = URLDecoder.decode(hadoopFilePath, "UTF-8");

        HadoopConnectionInfo connectionInfo = getHadoopConnectInfoByKey(hadoopConnectKey);
        if (connectionInfo == null) {
            container.put("error", ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error, request.getLocale()));
            ProtocolUtil.sendResponse(response, container);
            return;
        }

        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        FileStructureModel hdfsm =null;
        String hadoopFileFormat = null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
                if(op.getOperator() instanceof HadoopFileOperator){
                    hadoopFileFormat = (String) op.getOperator().getOperatorParameter(HadoopFileSelectorConfig.NAME_HD_format).getValue();
                }else {
                    hadoopFileFormat = "Text File";
                }

            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        try {
            String user = super.getUserName(request);
            Locale locale = request.getLocale();
            VisualizationModel visualModel = hdDataManager.getStatisticsVModel(connectionInfo.getConnection(), hdfsm, hadoopFileFormat, hadoopFilePath,columnNameIndex, resourceType, user,isHDFileOperator,locale);
            writeModel(response, visualModel, request.getLocale());
			LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getStatisticsData_HD", user);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }

    @RequestMapping(params="method=getHadoopColumnNameWithType", method=RequestMethod.POST)
    public void getHadoopColumnNameWithType(String operatorUID,HttpServletRequest request, HttpServletResponse response) throws IOException{
        Map<String, Object> container = new HashMap<String, Object>();

        FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        String uuid = operatorUID;
        if(null==info){
            container.put("error",ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error,request.getLocale()));
            ProtocolUtil.sendResponse(response,container);
            return ;
        }
        OperatorWorkFlow flow = readWorkFlow(request, response, info);
        if(flow==null){
            return;
        }
        FileStructureModel hdfsm =null;
        for (UIOperatorModel op : flow.getChildList()) {

            if (uuid.equals(op.getUUID()) == false) {
                continue;
            }
            hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
        }

        //todo type new some model
        if(null==hdfsm){
            hdfsm = new CSVFileStructureModel();
        }
        container.put("columnNameList",hdfsm.getColumnNameList());
        container.put("columnTypeList",hdfsm.getColumnTypeList());
        ProtocolUtil.sendResponse(response, container);
    }

    @RequestMapping(params="method=getHadoopFileCsvTableData", method=RequestMethod.GET)
    public void getHadoopFileCsvTableData(String connectionKey, String path,String operatorUID,String flowInfoKey,boolean isHDFileOperator, HttpServletRequest request, HttpServletResponse response) throws IOException{
        HadoopConnectionInfo hci = null;
        Map<String, Object> container = new HashMap<String, Object>();
        path = URLDecoder.decode(path, "UTF-8");
        try{
            hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        }catch (Exception e){
            container.put("error",ErrorNLS.getMessage(ErrorNLS.HadoopFileOperator_Connection_Error,request.getLocale()));
            ProtocolUtil.sendResponse(response,container);
            return ;
        }
        Map<String,Object> contentMap = new HashMap<String,Object>();
        int size = AlpineMinerConfig.HADOOP_LINE_THRESHOLD;

        //get hadoop file structure model
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);

        FileStructureModel hdfsm =null;
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            path = VisualUtils.refineFileName(path,super.getUserName(request),isHDFileOperator); //add file prefix
            path = VariableModelUtility.getReplaceValue(workFlow.getVariableModelList().get(0), path);//replace variable for path.
            for (UIOperatorModel op : workFlow.getChildList()) {
                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                hdfsm = OperatorUtility.getHadoopFileStructureModel(op);
            }
        } catch (OperationFailedException e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        try {
            List<String> content= HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList( path, hci.getConnection(), size);
            StringBuffer sb = new StringBuffer();
            if(content!=null){
	            for (Iterator iterator = content.iterator(); iterator.hasNext();) {
					String line = (String) iterator.next();
					sb=sb.append(line).append("\n") ;
				}
            }
            contentMap.put(path,sb.toString());
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        container.put("content", contentMap);
        getHadoopFileStructureCFG(container,hdfsm);
        ProtocolUtil.sendResponse(response, container);
		LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableData_HD_CSV", getUserName(request));
    }

    @RequestMapping(params="method=getHadoopFileXmlDataTable", method=RequestMethod.POST)
    public void getHadoopFileXmlDataTable(String connectionKey, String path,String flowInfoKey, HttpServletRequest request, HttpServletResponse response) throws Exception{
    	new HadoopFileStructureController().getXMLFileContentAsTable(connectionKey, path, flowInfoKey, request, response);
		LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableData_HD_XML", getUserName(request));
    }

    @RequestMapping(params="method=getHadoopFileLogDataTable", method=RequestMethod.POST)
    public void getHadoopFileLogDataTable(String connectionKey, String path,String flowInfoKey,String operatorUID,
								            String logFormat,String logType,Integer size,String viewType,//viewType : dataexporer or previe
								            HttpServletRequest request, HttpServletResponse response) throws Exception {
    	new HadoopFileStructureController().getHadoopLogFileContent(connectionKey, path, flowInfoKey, operatorUID, logFormat, logType, size, viewType, request, response);
		LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableData_HD_LOG", getUserName(request));
    }
    
    @RequestMapping(params="method=getHadoopFileJsonDataTable", method=RequestMethod.POST)
    public void getHadoopFileJsonDataTable(String connectionKey, String path,HttpServletRequest request, HttpServletResponse response)throws Exception{
    	new HadoopFileStructureController().getJSONFileContentAsTable(connectionKey, path, request, response);
    	LogPoster.getInstance().createAndAddEvent(LogPoster.OPERATOR_EXPLORATION, "getTableData_HD_JSON", getUserName(request));
    }

    @RequestMapping(params = "method=getHadoopFileType4DataExplorer", method = RequestMethod.GET)
    public void getHadoopFileType4DataExplorer(String operatorUID,String flowInfoKey,
                                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String,Object> result = new HashMap<String,Object>();
        FileStructureModel model = null;
        try {
            model = getHadoopFileStructureModel(operatorUID,flowInfoKey,request,response);
        } catch (OperationFailedException e) {
            e.printStackTrace();
            result.put("error",new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
        }
        if(model instanceof CSVFileStructureModel){
            result.put("fileType","csv");
            result.put("model",model);
        }else if(model instanceof XMLFileStructureModel){
            result.put("fileType","xml");
            result.put("model",model);
        }else if(model instanceof AlpineLogFileStructureModel){
            result.put("fileType","alpineLog");
            result.put("model",model);
        }else if(model instanceof JSONFileStructureModel){
            //JSONFileStructureModel
            result.put("fileType","json");
            result.put("model",model);
        }else{
            result.put("fileType","csv");
            result.put("model",model);
        }
        ProtocolUtil.sendResponse(response, result);
    }

    private FileStructureModel getHadoopFileStructureModel(String operatorUID,String flowInfoKey,HttpServletRequest request, HttpServletResponse response) throws OperationFailedException{
        FileStructureModel model =null;
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
        for (UIOperatorModel op : workFlow.getChildList()) {
            if (operatorUID.equals(op.getUUID()) == false) {
                continue;
            }
            model = OperatorUtility.getHadoopFileStructureModel(op);
        }
        if(model==null){
            model = new CSVFileStructureModel();
        }
        return model;
    }


    private void getHadoopFileStructureCFG(Map<String, Object> container,FileStructureModel hdfsm){
    	
        if(hdfsm!=null){
        if(hdfsm instanceof CSVFileStructureModel){
        	   container.put("delimiter",((CSVFileStructureModel)hdfsm).getDelimiter());
               container.put("includeHeader",((CSVFileStructureModel)hdfsm).getIncludeHeader());
               container.put("other",((CSVFileStructureModel)hdfsm).getOther());
         
               
           
           container.put("escapChar",((CSVFileStructureModel)hdfsm).getEscapChar());
           container.put("quoteChar",((CSVFileStructureModel)hdfsm).getQuoteChar());	
        }
            
            container.put("columnNameList",hdfsm.getColumnNameList());
            container.put("columnTypeList",hdfsm.getColumnTypeList());

        }
    }

    private OperatorWorkFlow readWorkFlow(HttpServletRequest request,HttpServletResponse response, FlowInfo info) throws IOException {
        OperatorWorkFlow flow = null;
        try {
            flow = rmgr.getFlowData(info,request.getLocale());
            if (flow == null) {
                //1: flow not found
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(1));
                return null;
            }
        } catch (OperationFailedException e) {
            ProtocolUtil.sendResponse(response,
                    new ErrorDTO(1, e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return flow;
    }

    private HadoopConnectionInfo getHadoopConnectInfoByKey(String connectionKey) {
        HadoopConnectionInfo hci = null;
        try {
            hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        } catch (Exception e) {
        }
        return hci;
    }

    private void writeModel(HttpServletResponse response,
                            VisualizationModel visualModel, Locale locale) throws Exception {
        String str = JSONUtility.toJSONString(visualModel, locale);

        ProtocolUtil.sendResponse(response, str);
    }
}
