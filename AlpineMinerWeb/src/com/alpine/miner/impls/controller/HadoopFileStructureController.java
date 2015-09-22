package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.impl.db.attribute.CopyToDBAnalyzer;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.hadoop.ext.JSONRecordReader;
import com.alpine.hadoop.ext.LogParserFactory;
import com.alpine.hadoop.ext.json.JSONArray;
import com.alpine.hadoop.ext.json.JSONException;
import com.alpine.logparser.IAlpineLogParser;
import com.alpine.miner.impls.controller.hadoopmeta.HadoopDisplayInfo;
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
import com.alpine.miner.workflow.operator.parameter.*;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import net.sf.json.JSONObject;
import org.apache.hadoop.io.DataOutputBuffer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * ClassName: HadoopFileStructureController
 * <p/>
 * Data: 13-1-17
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
@Controller
@RequestMapping("/main/fileStructureManager.do")
public class HadoopFileStructureController extends AbstractControler{
    private static final int DATA_PREVIEW_LINES_LIMIT = 100;
    private static final int DATA_EXPLORER_LINES_LIMIT = 300;
    private static final int READ_SIZE_LIMITATION = 1;
    private static final int GUESS_LINE_NUMBER = 50;

    public HadoopFileStructureController() throws Exception {
        super();
    }

    @RequestMapping(params="method=getHadoopFilesByPath", method= RequestMethod.GET)
    public void getHadoopFilesByPath(String connectionKey, String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<HadoopFile> hadoopFiles = null;
        if(path == null){
            path = HadoopHDFSFileManager.ROOT_PATH;
        }
        List<HadoopDisplayInfo> result = new ArrayList<HadoopDisplayInfo>();
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        try{
            //hadoopFiles = HadoopHDFSFileManager.INSTANCE.getHadoopFiles(path, hci.getConnection(), false);
            hadoopFiles = HadoopHDFSFileManager.INSTANCE.getHadoopFiles(path, hci.getConnection(), false);
        }catch(Exception e){
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        for(HadoopFile item : hadoopFiles){
            result.add(new HadoopDisplayInfo(item));
        }
        Collections.sort(result, new Comparator<HadoopDisplayInfo>() {

            @Override
            public int compare(HadoopDisplayInfo o1, HadoopDisplayInfo o2) {
                if (o1.isDir() == o2.isDir()) {
                    return o1.getName().compareTo(o2.getName());
                } else if (o1.isDir()) {
                    return -1;
                } else {
                    return 1;
                }
            }

        });
        ProtocolUtil.sendResponse(response, result);
    }

    @RequestMapping(params="method=gethadoopContent", method=RequestMethod.GET)
    public void gethadoopContent(String connectionKey, String path, Integer size, HttpServletRequest request, HttpServletResponse response) throws IOException{
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        String content;
        Map<String, String> container = new HashMap<String, String>();
        try {
            if(size == null){
                content = HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber(path, hci.getConnection(),DATA_PREVIEW_LINES_LIMIT);
            }else{
                content= HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber(path, hci.getConnection(), size);
            }
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        container.put("content", content);
        ProtocolUtil.sendResponse(response, container);
    }


    @RequestMapping(params = "method=getHadoopLogFileContent", method = RequestMethod.GET)
    public void getHadoopLogFileContent(String connectionKey, String path,String flowInfoKey,String operatorUID,
                                        String logFormat,String logType,Integer size,String viewType,//viewType : dataexporer or previe
                                        HttpServletRequest request, HttpServletResponse response) throws IOException {
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        AlpineLogFileStructureModel lfsm = null;
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setKey(flowInfoKey);
        try {
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            for (UIOperatorModel op : workFlow.getChildList()) {

                if (operatorUID.equals(op.getUUID()) == false) {
                    continue;
                }
                lfsm = (AlpineLogFileStructureModel) OperatorUtility.getHadoopFileStructureModel(op);
            }
        } catch (Exception e) {
            lfsm = null;
            //e.printStackTrace();
        }

        String content;
        if("dataexplorer".equals(viewType)==true){
            path = URLDecoder.decode(path, "UTF-8");
        }
        try {
            if(size == null){
                content = HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber(path, hci.getConnection(),DATA_PREVIEW_LINES_LIMIT);
            }else{
                content= HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber(path, hci.getConnection(), size);
            }
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }


        Map<String,Object> result = new HashMap<String,Object>();
        if(null==logType){
            logType="Log4J";
        }

        IAlpineLogParser parser = LogParserFactory.createALogParser(logFormat, logType);
        String[] lines=content.split("\n");
        List<String[]> contents = new ArrayList<String[]>();
        for(String line:lines){
            contents.add(parser.processTheLine(line));
        }
        List<String> preColumnNames = (null==lfsm?null:lfsm.getColumnNameList());
        List<String> preColumnTypesList =  (null==lfsm?null:lfsm.getColumnNameList());
        String[] columnNames = (null==preColumnNames?parser.getMatchingKeywords():preColumnNames.toArray(new String[]{}));
        String[] columnTypes = (null==preColumnTypesList?parser.getMatchingKeywords():preColumnTypesList.toArray(new String[]{}));
        if("preview".equals(viewType)){
            result.put("columnNames",parser.getMatchingKeywords());
            result.put("columnTypes",parser.getMatchingTypes());
            result.put("contents",contents);
            ProtocolUtil.sendResponse(response,result);
        }else{
            DataTable tables = getHadoopLogFile4VisualDataTable(columnNames,columnTypes,contents,path);
            VisualizationModelDataTable dataTable = new VisualizationModelDataTable(path,tables);
            JSONObject json = JSONUtility.toJSONObject(dataTable, request.getLocale());
            ProtocolUtil.sendResponse(response,json);
        }
    }

    private DataTable getHadoopLogFile4VisualDataTable(String[] columnHeads,String[] columnTypes,List<String[]> rowList,String path){
        DataTable tables= new DataTable();
        List<TableColumnMetaInfo> tableColumns=new ArrayList<TableColumnMetaInfo> ();
        for (int i = 0; i < columnHeads.length; i++) {
//            if("length".equals(columnHeads.get(i))==true){
//                columnHeads.set(i,columnHeads.get(i).toUpperCase());
//            }
            String columnType = "";
            if(columnTypes[i]!=null){
                columnType = columnTypes[i];
            }
            tableColumns.add(new TableColumnMetaInfo(columnHeads[i],columnType));
        }
        tables.setColumns(tableColumns) ;
        List<DataRow> tableRows =new ArrayList<DataRow> ();
        //get hadoop file structure model
        int lineNum = 0;
        for (int i = 0; i < rowList.size(); i++) {
            String[] row = rowList.get(i);
            if (row.length > 0) {
                DataRow dataRow = new DataRow();
                dataRow.setData(row);
                tableRows.add(dataRow);
                lineNum = lineNum + 1;
                if (lineNum > DATA_EXPLORER_LINES_LIMIT) {
                    tables.setRows(tableRows);
                    return tables;
                }
            }
        }
        tables.setRows(tableRows);
        return tables;
    }

    @RequestMapping(params="method=getXMLFileContent", method=RequestMethod.POST)
    public void getXMLFileContent(String connectionKey, String path,HttpServletRequest request, HttpServletResponse response)throws IOException{
        XMLFileStructureModel fileStructure = ProtocolUtil.getRequest(request, XMLFileStructureModel.class);
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);

        String domNode = "";
        try{
            String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M
            if(true== StringUtil.isEmpty(fileStructure.getContainer())){
                try{
                    parseXMLString(response, content);
                    ProtocolUtil.sendResponse4XML(response, content.trim());
                    return;
                }catch(Exception e){
                    //not a full xml for now
                }

//				 try to guess a xml dom
                while(true){
                    int roorStart = 0 ;
                    boolean noAttribute=true;
                    if(content.indexOf("<?xml")>-1){
                        roorStart = content.indexOf("?>")+2 ;
                    } else{
                        roorStart = content.indexOf("<");
                    }
                    content = content.substring(roorStart,content.length());
                    int firstBlank = content.indexOf(" ") ;
                    int firstEndBrake = content.indexOf(">") ;
                    int rootEnd = firstBlank;
                    if(firstEndBrake<firstBlank){
                        rootEnd = firstEndBrake+1;
                    }else{
                        noAttribute=false;
                    }
                    String rootTag = content.substring(0,rootEnd).trim() ;
                    String rootEndTag = rootTag.replace("<","</") ;
                    if(content.indexOf(rootEndTag)==-1){
                        content=content.substring(rootTag.length(), content.length());
                        int endIndex = content.indexOf(rootEndTag);
                        if(endIndex==-1){
                            if(noAttribute==false){
                                endIndex = content.indexOf(">");
                                if(endIndex==-1){
                                    break;
                                }else{
                                    endIndex=endIndex+1;
                                }
                            }else{
                                content=content.trim();
                                continue;
                            }
                        }else{
                            endIndex=endIndex+rootEndTag.length();
                        }
                        content=content.substring(endIndex, content.length()).trim();
                        continue;
                    }
                    domNode = content.substring(0,content.indexOf(rootEndTag)+rootTag.length()+1);
                    if(domNode.endsWith(">")==false){
                        domNode=domNode+">";
                    }

                    try{
                        Element element=parseXMLString(domNode);
                        NodeList childNodes = element.getChildNodes();
                        if(childNodes!=null&&childNodes.getLength()>1){
                            domNode="<alpineguessed>"+domNode+"</alpineguessed>";
                            //Element alpineguessed = document.createElement("alpineguessed");
                            //element.appendChild(alpineguessed);
                            break;
                        }else{
                            content=content.substring(rootTag.length(), content.length());
                            int endIndex = content.indexOf(rootEndTag);
                            if(endIndex==-1){
                                if(noAttribute==false){
                                    endIndex = content.indexOf(">");
                                    if(endIndex==-1){
                                        break;
                                    }else{
                                        endIndex=endIndex+1;
                                    }
                                }else{
                                    content=content.trim();
                                    continue;
                                }
                            }else{
                                endIndex=endIndex+rootEndTag.length();
                            }
                            content=content.substring(endIndex, content.length()).trim();
                        }
                    }catch (Exception e) {
                        content=content.substring(rootTag.length(), content.length());
                        int endIndex = content.indexOf(rootEndTag);
                        if(endIndex==-1){
                            if(noAttribute==false){
                                endIndex = content.indexOf(">");
                                if(endIndex==-1){
                                    break;
                                }else{
                                    endIndex=endIndex+1;
                                }
                            }else{
                                content=content.trim();
                                continue;
                            }
                        }else{
                            endIndex=endIndex+rootEndTag.length();
                        }
                        content=content.substring(endIndex, content.length()).trim();
                    }
                }
            }else if(false==StringUtil.isEmpty(fileStructure.getContainer())){
                String startTag = "<"+fileStructure.getContainer();
                String endTag =null;

                if(StringUtil.isEmpty(fileStructure.getAttrMode())){
                    //  not sure
                    if(content.indexOf(startTag)==content.indexOf(startTag+">")){
                        startTag = startTag +">" ;
                    }
                    else{
                        startTag = startTag+" " ;
                    }
                    endTag = "</"+fileStructure.getContainer()+">";

                    try {
                        parseXMLString(response,
                                content.substring(
                                        content.indexOf(startTag),
                                        content.indexOf(endTag)
                                                + endTag.length()));

                    } catch (Exception e) {
                        // not a full xml for now
                        endTag = "/>";// for single line xml
                    }
                }
                else if(fileStructure.getAttrMode().equals( XMLFileStructureModel.NO_ATTRIBUTE)){
                    startTag = startTag +">" ;
                    endTag = "</"+fileStructure.getContainer()+">";

                }else{ //has attribute
                    startTag = startTag +" " ;
                    if(fileStructure.getAttrMode().equals( XMLFileStructureModel.HALF_ATTRIBUTE)){
                        endTag = "</"+fileStructure.getContainer()+">";
                    }else{//pure attribute mode
                        endTag="/>";//for single line xml

                    }
                }
                if(content.indexOf(endTag)<0){
                }
                domNode = content.substring( content.indexOf(startTag),content.indexOf(endTag)+endTag.length()) ;

            }
        } catch (Exception e) {
            ProtocolUtil.sendResponse4XML(response, "<error code=\"9999\">"+e.getMessage()+"</error>");
            return;
        }
        ProtocolUtil.sendResponse4XML(response, domNode);
    }

    @RequestMapping(params="method=getXMLFileContentAsTable", method=RequestMethod.POST)
    public void getXMLFileContentAsTable(String connectionKey, String path,String flowInfoKey, HttpServletRequest request, HttpServletResponse response)throws IOException{
        XMLFileStructureModel fileStructure = ProtocolUtil.getRequest(request, XMLFileStructureModel.class);
        path = URLDecoder.decode(path, "UTF-8");
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        List<String> columnHeads = fileStructure.getColumnNameList();
        List<String> columnTypes = fileStructure.getColumnTypeList();
        DataTable tables= new DataTable();
        List<TableColumnMetaInfo> tableColumns=new ArrayList<TableColumnMetaInfo> ();
        for (int i = 0; i < columnHeads.size(); i++) {
            if("length".equals(columnHeads.get(i))==true){
                columnHeads.set(i,columnHeads.get(i).toUpperCase());
            }
            tableColumns.add(new TableColumnMetaInfo(columnHeads.get(i),columnTypes.get(i)));
        }
        tables.setColumns(tableColumns) ;
        List<DataRow> tableRows =new ArrayList<DataRow> ();
        try{
            //get hadoop file structure model
            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setKey(flowInfoKey);
            OperatorWorkFlow workFlow = ResourceFlowManager.instance.getFlowData(flowInfo,request.getLocale());
            path = VariableModelUtility.getReplaceValue(workFlow.getVariableModelList().get(0), path);
            String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize(path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M

            BufferedReader fsin = new BufferedReader(new StringReader(content));
            CopyToDBAnalyzer.XmlRecordReader parser = new CopyToDBAnalyzer.XmlRecordReader(fileStructure.getContainer(),
                    fileStructure.getxPathList(), fsin, fileStructure.getAttrMode(),
                    fileStructure.getXmlDataStructureType(),fileStructure.getContainerXPath()) ;
            List<String[]> rowList;
            int lineNum = 0 ;
            while ((rowList = parser.readNext()) != null) {
                for(String[] row:rowList){
                    //resultList.add(row) ;
                    if(row.length>0){
                        DataRow dataRow = new DataRow();
                        dataRow.setData(row);
                        tableRows.add(dataRow);
                        lineNum = lineNum + 1;
                        if(lineNum>=DATA_PREVIEW_LINES_LIMIT){
                            tables.setRows(tableRows ) ;
                            VisualizationModelDataTable dataTable = new VisualizationModelDataTable(path,tables);
                            JSONObject json = JSONUtility.toJSONObject(dataTable,request.getLocale());
                            ProtocolUtil.sendResponse(response,json);
                            return ;

                        }
                    }
                }
            }
            tables.setRows(tableRows ) ;
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        VisualizationModelDataTable dataTable = new VisualizationModelDataTable(path,tables);
        JSONObject json = JSONUtility.toJSONObject(dataTable,request.getLocale());
        ProtocolUtil.sendResponse(response,json);
    }

    @RequestMapping(params="method=getJSONContent", method=RequestMethod.POST)
    public void getJSONContent(String connectionKey, String path,HttpServletRequest request, HttpServletResponse response)throws Exception{
        JSONFileStructureModel fileStructure = ProtocolUtil.getRequest(request, JSONFileStructureModel.class);
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        String sturctureType = fileStructure.getJsonDataStructureType();
        String container = fileStructure.getContainer();
        if(sturctureType.equals(JSONFileStructureModel.STRUCTURE_TYPE_LINE)){
            String content = HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber( path, hci.getConnection(),GUESS_LINE_NUMBER);//50 line

            if(null==content){
                ProtocolUtil.sendResponse(response,"{error:'can not get content'}");
                return;
            }
            String[] lines = content.split("\n");
            List<com.alpine.hadoop.ext.json.JSONObject> objs = new ArrayList<com.alpine.hadoop.ext.json.JSONObject>();
            for (String line : lines) {
                objs.add(new com.alpine.hadoop.ext.json.JSONObject(line));
            }
            com.alpine.hadoop.ext.json.JSONObject mergedJSONObj = mergeJSONList(objs);

            if(container!=null&&container.length()!=0){
                List<String> needRemoved=new ArrayList<String>();
                Iterator iter = mergedJSONObj.keys();
                while(iter.hasNext()){
                    String key = (String)iter.next();
                    if(key.equals(container)==false){
                        needRemoved.add(key);
                    }
                }
                if(needRemoved.size()>0){
                    for(String key:needRemoved){
                        mergedJSONObj.remove(key);
                    }
                }
                ProtocolUtil.sendResponse(response, "{AlpineLineJSONVirtualRoot:"
                        + mergedJSONObj.toString() + ",sturctureType:\""
                        + sturctureType + "\"}");
            }else{
                ProtocolUtil.sendResponse(response, "{AlpineJSONVirtualRoot:"
                        + mergedJSONObj.toString() + ",sturctureType:\""
                        + sturctureType + "\"}");
            }

        }else if (sturctureType.equals(JSONFileStructureModel.STRUCTURE_TYPE_PURE_DATA_ARRAY)){
            if(StringUtil.isEmpty(container) == true){
                //this can not happen
            }else{
                String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M

                if(null==content){
                    ProtocolUtil.sendResponse(response,"{error:'can not get content'}");
                    return ;
                }

                String[] records = content.split("\""+container+"\"[ \n\t]*:[ \n\t]*");
                if(records.length>1){
                    StringBuilder sb = repairContentForArray(container, records);
                    ProtocolUtil.sendResponse(response,"{"+sb.toString()+",sturctureType:\""+sturctureType+"\"}");
                }
            }

        }else if (sturctureType.equals(JSONFileStructureModel.STRUCTURE_TYPE_OBJECT_ARRAY)){
            if(StringUtil.isEmpty(container) == true){
                //this can not happen
            }else{
                String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M

                if(null==content){
                    ProtocolUtil.sendResponse(response,"{error:'can not get content'}");
                    return ;
                }

                String[] records = content.split("\""+container+"\"[ \n\t]*:[ \n\t]*");
                if(records.length>1){
                    StringBuilder sb = repairContentForObject(container, records);
                    ProtocolUtil.sendResponse(response,"{"+sb.toString()+",sturctureType:\""+sturctureType+"\"}");
                }
            }

        }else{
            //standard mode or the first time

            try{
                //first time
                if(StringUtil.isEmpty(container) == true){
                    String content50Lines = HadoopHDFSFileManager.INSTANCE.readHadoopPathToStringByLineNumber( path, hci.getConnection(),GUESS_LINE_NUMBER);//50 line
                    content50Lines=content50Lines.trim();
                    if(content50Lines.startsWith("{")){
                        //guess whether each line is a json string (like facebook)
                        content50Lines=content50Lines.trim();
                        boolean isLineType = true;
                        List<String> resultList= new ArrayList<String>();
                        for (int i = 0; i < GUESS_LINE_NUMBER; i++) {
                            if(content50Lines==null)break;
                            int index = content50Lines.indexOf("\n");
                            String jsonString = null;
                            if (index != -1) {
                                jsonString = content50Lines.substring(0,index);
                                content50Lines = content50Lines.substring(index + 1,
                                        content50Lines.length());
                            } else if(i!=0){
                                jsonString = content50Lines.substring(0,content50Lines.length());
                                content50Lines = null;
                            } else{
                                jsonString=content50Lines;
                                content50Lines = null;
                                isLineType = false;
                            }
                            resultList.add(jsonString);
                            try {
                                new com.alpine.hadoop.ext.json.JSONObject(jsonString);
                            } catch (Exception e) {
                                isLineType = false;
                                break;
                            }
                        }
                        if(isLineType==true){
                            // this is line mode
                            sturctureType = JSONFileStructureModel.STRUCTURE_TYPE_LINE;
                            fileStructure
                                    .setJsonDataStructureType(sturctureType);

                            List<com.alpine.hadoop.ext.json.JSONObject> objs = new ArrayList<com.alpine.hadoop.ext.json.JSONObject>();
                            for (String line : resultList) {
                                objs.add(new com.alpine.hadoop.ext.json.JSONObject(
                                        line));
                            }
                            com.alpine.hadoop.ext.json.JSONObject mergedJSONObj = mergeJSONList(objs);
                            ProtocolUtil.sendResponse(response, "{AlpineJSONVirtualRoot:"
                                    + mergedJSONObj.toString()
                                    + ",sturctureType:\"" + sturctureType + "\"}");
                        }else{
                            String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M
                            if(null==content){
                                ProtocolUtil.sendResponse(response,"{error:'can not get content'}");
                                return;
                            }
                            content=content.trim();
                            try {
                                com.alpine.hadoop.ext.json.JSONObject jsonObject = new com.alpine.hadoop.ext.json.JSONObject(
                                        content);
                                ProtocolUtil.sendResponse(response,
                                        "{AlpineJSONVirtualRoot:" + jsonObject.toString()
                                                + ",sturctureType:\""
                                                + sturctureType + "\"}");
                            } catch (Exception e) {
                                if(guessContainer(response, sturctureType, content)==false){
                                    ProtocolUtil.sendResponse(response,
                                            "{error:'please input a container',sturctureType:\""+sturctureType+"\"}");
                                }
                            }
                        }
                    }
                }else{
                    String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M
                    if(null==content){
                        ProtocolUtil.sendResponse(response,"{error:'can not get content'}");
                        return;
                    }

                    String[] records = content.split("\""+container+"\"[ \n\t]*:[ \n\t]*");
                    if (records.length > 1) {
                        CopyToDBAnalyzer.JsonRecordReader reader = new CopyToDBAnalyzer.JsonRecordReader(container, null, new BufferedReader(new  StringReader(content)),
                                fileStructure.getJsonDataStructureType(),fileStructure.getJsonDataStructureType()) ;
                        content = reader.readARecordString();
                        if(content!=null){
                            ProtocolUtil.sendResponse(response,"{"+container+":"+content+",sturctureType:\""+sturctureType+"\"}");
                            return;
                        }else if (records[1].split("[ \n\t]*\\[[ \n\t]*\\[[ \n\t]*").length > 1) {
                            StringBuilder sb = repairContentForArray(container,
                                    records);
                            sturctureType = JSONFileStructureModel.STRUCTURE_TYPE_PURE_DATA_ARRAY;
                            fileStructure
                                    .setJsonDataStructureType(sturctureType);
                            ProtocolUtil.sendResponse(response,
                                    "{" + sb.toString() + ",sturctureType:\""
                                            + sturctureType + "\"}");
                            return;
                        } else if (records[1]
                                .split("[ \n\t]*\\[[ \n\t]*\\{[ \n\t]*").length > 1) {
                            sturctureType = JSONFileStructureModel.STRUCTURE_TYPE_OBJECT_ARRAY;
                            fileStructure
                                    .setJsonDataStructureType(sturctureType);
                            StringBuilder sb = repairContentForObject(
                                    container, records);
                            ProtocolUtil.sendResponse(response,
                                    "{" + sb.toString() + ",sturctureType:\""
                                            + sturctureType + "\"}");
                            return;
                        }
                    }

                    CopyToDBAnalyzer.JsonRecordReader reader = new CopyToDBAnalyzer.JsonRecordReader(container, null, new BufferedReader(new  StringReader(content)),
                            fileStructure.getJsonDataStructureType(),fileStructure.getJsonDataStructureType()) ;
                    content = reader.readARecordString();

                    ProtocolUtil.sendResponse(response,"{"+container+":"+content+",sturctureType:\""+sturctureType+"\"}");
                }
            } catch (Exception e) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(-999, e.getMessage()));
                return;
            }

        }
    }

    @RequestMapping(params="method=getJSONFileContentAsTable", method=RequestMethod.POST)
    public void getJSONFileContentAsTable(String connectionKey, String path,HttpServletRequest request, HttpServletResponse response)throws IOException{
        JSONFileStructureModel fileStructure = ProtocolUtil.getRequest(request, JSONFileStructureModel.class);
        path = URLDecoder.decode(path, "UTF-8");
        HadoopConnectionInfo hci = IHadoopConnectionFeatcher.INSTANCE.getHadoopConnection(connectionKey);
        List<String> columnHeads = fileStructure.getColumnNameList();
        List<String> columnTypes = fileStructure.getColumnTypeList();
        DataTable tables= new DataTable();
        List<TableColumnMetaInfo> tableColumns=new ArrayList<TableColumnMetaInfo> ();
        for (int i = 0; i < columnHeads.size(); i++) {
            if("length".equals(columnHeads.get(i))==true){
                columnHeads.set(i,columnHeads.get(i).toUpperCase());
            }
            tableColumns.add(new TableColumnMetaInfo(columnHeads.get(i),columnTypes.get(i)));
        }
        tables.setColumns(tableColumns) ;
        List<DataRow> tableRows =new ArrayList<DataRow> ();
        try{

            String content = HadoopHDFSFileManager.INSTANCE.readHadoopFileToStringBySize( path, hci.getConnection(),READ_SIZE_LIMITATION*1024);//1M

            BufferedReader fsin = new BufferedReader(new StringReader(content));
            CopyToDBAnalyzer.JsonRecordReader parser = new CopyToDBAnalyzer.JsonRecordReader(fileStructure.getContainer(),
                    fileStructure.getJsonPathList(), fsin,
                    fileStructure.getJsonDataStructureType(),fileStructure.getContainerJsonPath()) ;
            List<String[]> rowList;
            int lineNum = 0 ;
            while ((rowList = parser.readNext()) != null) {
                for(String[] row:rowList){
                    //resultList.add(row) ;
                    if(row.length>0){
                        DataRow dataRow = new DataRow();
                        dataRow.setData(row);
                        tableRows.add(dataRow);
                        lineNum = lineNum + 1;
                        if(lineNum>=DATA_PREVIEW_LINES_LIMIT){
                            tables.setRows(tableRows ) ;
                            VisualizationModelDataTable dataTable = new VisualizationModelDataTable(path,tables);
                            JSONObject json = JSONUtility.toJSONObject(dataTable,request.getLocale());
                            ProtocolUtil.sendResponse(response,json);
                            return ;

                        }
                    }
                }
            }
            tables.setRows(tableRows ) ;
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
            return;
        }
        VisualizationModelDataTable dataTable = new VisualizationModelDataTable(path,tables);
        JSONObject json = JSONUtility.toJSONObject(dataTable,request.getLocale());
        ProtocolUtil.sendResponse(response,json);
    }

    @RequestMapping(params="method=getFileStructureColumnNameWithType", method=RequestMethod.POST)
    public void getFileStructureColumnNameWithType(String connectionKey,String operatorUID,HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> container = new HashMap<String, Object>();
        FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        String uuid = operatorUID;

        OperatorWorkFlow flow = readWorkFlow(request, response, info);
        if(flow==null){
            return;
        }
        FileStructureModel hdfsm =null;
        for (UIOperatorModel op : flow.getChildList()) {
            if (uuid.equals(op.getUUID()) == false) {
                continue;
            }
            //System.out.print(op);
            hdfsm =(FileStructureModel) ParameterUtility.getParameterValue(op.getOperator(), OperatorParameter.NAME_HD_fileStructure);
        }
        //HadoopFileStructureModel hdfsm =(HadoopFileStructureModel) ParameterUtility.getParameterValue(operator, OperatorParameter.NAME_HD_fileStructure);
        if(null!=hdfsm && null!=hdfsm.getColumnNameList() && null!=hdfsm.getColumnTypeList() && hdfsm.getColumnNameList().size()==hdfsm.getColumnTypeList().size()){
            List<String> nameList = hdfsm.getColumnNameList();
            List<String> typeList = hdfsm.getColumnTypeList();
            List<Object> nameTypeList = new ArrayList<Object>();
            for(int i=0;i<nameList.size();i++){
                Map<String,String> valueTypeMap = new HashMap<String, String>();
                valueTypeMap.put("columnName",nameList.get(i));
                valueTypeMap.put("columnType",typeList.get(i));
                nameTypeList.add(valueTypeMap);
            }
            container.put("nameTypeList",nameTypeList);
        }else{
            //put error msg
        }
        ProtocolUtil.sendResponse(response, container);

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

    private Element parseXMLString(HttpServletResponse response, String content)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document xmlDoc = db.parse(new ByteArrayInputStream(content.getBytes("utf-8")));
        return  xmlDoc.getDocumentElement();

    }
    private Element parseXMLString(String content)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document xmlDoc = db.parse(new ByteArrayInputStream(content.getBytes("utf-8")));
        return  xmlDoc.getDocumentElement();

    }
    //for json structure begin

    private boolean guessContainer(HttpServletResponse response,
                                   String sturctureType, String content) throws Exception,
            IOException, UnsupportedEncodingException {
        String guessedContainer = null;
        int guessIndex=1;
        while(true){
            BufferedReader br = new BufferedReader(new StringReader(content));
            try {
                for(int i=0;i<guessIndex;i++){
                    guessedContainer = findContainer(br);
                }
                if(guessedContainer!=null){
                    int fisrtMatch = readUntilMatch(br);
                    DataOutputBuffer buffer = new DataOutputBuffer();
                    try {
                        if(fisrtMatch==0){
                            if(readUntilMatchSquare(JSONRecordReader.LEFT_SQUARE_BRACKET,
                                    JSONRecordReader.RIGHT_SQUARE_BRACKET,1,0,br,buffer)){
                                String newStr="["+new String(buffer.getData(),"utf-8").trim()+"]";
                                guessedContainer=guessedContainer.substring(1,guessedContainer.length()-1);
                                ProtocolUtil.sendResponse(response,"{"+guessedContainer+":"+
                                        newStr+",sturctureType:\""+sturctureType+"\",guessedContainer:\""+guessedContainer+"\"}");
                                return true;
                            }
                        }else if(fisrtMatch==1){
                            if(readUntilMatchSquare(JSONRecordReader.LEFT_BRACE,
                                    JSONRecordReader.RIGHT_BRACE,1,0,br,buffer)){
                                String newStr="{"+new String(buffer.getData(),"utf-8").trim()+"}";
                                guessedContainer=guessedContainer.substring(1,guessedContainer.length()-1);
                                ProtocolUtil.sendResponse(response,"{"+guessedContainer+":"+newStr+"," +
                                        "sturctureType:\""+sturctureType+"\",guessedContainer:\""+guessedContainer+"\"}");
                                return true;
                            }
                        }
                    } finally{
                        buffer.close();
                    }
                    guessIndex++;
                }else{
                    break;
                }
            } finally{
                br.close();
            }
        }
        return false;
    }

    private String findContainer(BufferedReader br) throws Exception{
        DataOutputBuffer buffer = new DataOutputBuffer();
        int doubleQCount=0;
        int lastB=0;
        boolean matchedDQ=false;
        boolean beginToRecord=false;
        boolean mathced=false;
        try {
            while (true) {
                int b = br.read();
                // end of file:
                if (b == -1)
                    break;
                if(b == 10||b == 9||b == 32){//"\n","\t"," "
                    continue;
                }
                if(matchedDQ){
                    if(b == 58){//":"
                        mathced=true;
                        break;
                    }else{
                        doubleQCount=0;
                        matchedDQ=false;
                        beginToRecord=false;
                        mathced=false;
                        buffer.close();
                        buffer = new DataOutputBuffer();
                    }
                }

                if(b == JSONRecordReader.DQ[0]
                        && lastB !=92){//"\"
                    beginToRecord=true;
                    doubleQCount++;
                    if(doubleQCount==2){
                        matchedDQ=true;
                    }
                }
                lastB=b;
                if(beginToRecord){
                    // save to buffer:
                    buffer.write(b);
                }
            }
        } finally{
            buffer.close();
        }
        if(mathced){
            return new String(buffer.getData(),"utf-8").trim();
        }
        return null;
    }

    private int readUntilMatch(BufferedReader br) throws IOException{
        while (true) {
            int b = br.read();
            // end of file:
            if (b == -1)
                return -1;
            if(b == 10||b == 9||b == 32){//"\n","\t"," "
                continue;
            }
            // check if we're matching:
            if (b == JSONRecordReader.LEFT_SQUARE_BRACKET[0]) {
                return 0;
            } else if(b == JSONRecordReader.LEFT_BRACE[0]){
                return 1;
            } else {
                return 2;
            }
        }
    }

    private StringBuilder repairContentForObject(String container,
                                                 String[] records) throws Exception{
        String record = records[1];
        DataOutputBuffer buffer = new DataOutputBuffer();
        BufferedReader fsin = new BufferedReader(new StringReader(record));
        int count=0;
        StringBuilder sb=new StringBuilder();
        sb.append("\"").append(container).append("\"").append(":[");
        try {
            while(readUntilMatch(JSONRecordReader.LEFT_BRACE,
                    JSONRecordReader.RIGHT_BRACE,0,0,fsin,buffer)
                    &&count<GUESS_LINE_NUMBER){
                String newStr=new String(buffer.getData(),"utf-8").trim();
                sb.append(newStr).append(",");
                count++;
                buffer = new DataOutputBuffer();
            }
        } finally{
            buffer.reset();
        }
        sb=sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb;
    }

    private StringBuilder repairContentForArray(String container, String[] records)
            throws IOException, UnsupportedEncodingException {
        String record = records[1];
        record=record.substring(record.indexOf("[")+1,record.length());
        record=record.substring(record.indexOf("["),record.length());
        DataOutputBuffer buffer = new DataOutputBuffer();
        BufferedReader fsin = new BufferedReader(new StringReader(record));
        int count=0;
        StringBuilder sb=new StringBuilder();
        sb.append("\"").append(container).append("\"").append(":[");
        try {
            while(readUntilMatch(JSONRecordReader.LEFT_SQUARE_BRACKET,
                    JSONRecordReader.RIGHT_SQUARE_BRACKET,0,0,fsin,buffer)
                    &&count<GUESS_LINE_NUMBER){
                String newStr=new String(buffer.getData(),"utf-8").trim();
                sb.append(newStr).append(",");
                count++;
                buffer = new DataOutputBuffer();
            }
        } finally{
            buffer.reset();
        }
        sb=sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb;
    }

    private boolean readUntilMatch(byte[] leftArray,byte rightArray[],int leftCount,
                                   int rightCount, BufferedReader fsin, DataOutputBuffer buffer) throws IOException{
        boolean beingToRecord=false;
        while (true) {
            int b = fsin.read();
            // end of file:
            if (b == -1)
                return false;
            // check if we're matching:
            if (b == rightArray[0]) {
                rightCount++;
                if(leftCount==rightCount){
                    buffer.write(b);
                    return true;
                }
            } else if(b == leftArray[0]){
                leftCount++;
                beingToRecord=true;
            }
            if(beingToRecord){
                // save to buffer:
                buffer.write(b);
            }
        }
    }

    private boolean readUntilMatchSquare(byte[] leftArray,byte rightArray[],int leftCount,int rightCount,
                                         BufferedReader fsin, DataOutputBuffer buffer) throws IOException{
        while (true) {
            int b = fsin.read();
            // end of file:
            if (b == -1)
                return false;
            // check if we're matching:
            if (b == rightArray[0]) {
                rightCount++;
                if(leftCount==rightCount){
                    return true;
                }
            } else if(b == leftArray[0]){
                leftCount++;
            }
            // save to buffer:
            buffer.write(b);
        }
    }

    private com.alpine.hadoop.ext.json.JSONObject mergeJSONList(
            List<com.alpine.hadoop.ext.json.JSONObject> objs) throws Exception {

        Map<String,Object> jsonMap=new LinkedHashMap<String,Object>();
        for(com.alpine.hadoop.ext.json.JSONObject jsonObj:objs){
            handleJsonObject(jsonMap, jsonObj);
        }

        return retrieveJSONObject(jsonMap);
    }

    private com.alpine.hadoop.ext.json.JSONObject retrieveJSONObject(
            Map<String, Object> jsonMap) throws JSONException {

        Iterator<Map.Entry<String, Object>> iter = jsonMap.entrySet().iterator();


        com.alpine.hadoop.ext.json.JSONObject newObj=new com.alpine.hadoop.ext.json.JSONObject();

        while(iter.hasNext()){
            Map.Entry<String, Object> entry = iter.next();
            Object value = entry.getValue() ;
            if(value instanceof String){
                newObj.put(entry.getKey(), value);
            }else if(value instanceof Map ){
                newObj.put(entry.getKey(), retrieveJSONObject((Map)value));

            }else if (value instanceof JSONArray){
                newObj.put(entry.getKey(), value);

            }

        }
        return newObj;
    }

    private void handleJsonObject(Map<String, Object> jsonMap,
                                  com.alpine.hadoop.ext.json.JSONObject jsonObj) throws JSONException {
        Iterator iter = jsonObj.keys();
        while(iter.hasNext()){
            String key=(String)iter.next();
            Object obj = jsonObj.get(key);
            if(obj instanceof com.alpine.hadoop.ext.json.JSONObject){
                Map<String, Object> newjsonMap =null;
                if(jsonMap.containsKey(key)==false){
                    newjsonMap = new LinkedHashMap<String, Object>();
                    jsonMap.put(key, newjsonMap);
                } else if(jsonMap.get(key) instanceof Map){
                    newjsonMap =  (Map) jsonMap.get(key) ;
                }
                if(newjsonMap!=null){
                    handleJsonObject(newjsonMap, (com.alpine.hadoop.ext.json.JSONObject)obj);
                }

            }else if(obj instanceof JSONArray){
                JSONArray jsonArray=((JSONArray)obj);
                if(jsonArray.length()>0){
//						ArrayList<Map<String, Object>> newjsonMapList = new ArrayList<Map<String, Object> >();
//						for(int i=0;i<jsonArray.length();i++){
//							Object newObj = jsonArray.get(i);
//							if(newObj instanceof com.alpine.hadoop.ext.json.JSONObject){
//								Map<String, Object> newjsonMap = new LinkedHashMap<String, Object>();
//								handleJsonObject(newjsonMap, (com.alpine.hadoop.ext.json.JSONObject)newObj);
//								newjsonMapList.add(newjsonMap);
                    //
//							}
//
//						}
                    jsonMap.put(key, jsonArray);
                }
            }else{
                if(jsonMap.containsKey(key)==false){
                    jsonMap.put(key, String.valueOf(obj));
                }
            }
        }
    }

    //for json structure end
}
