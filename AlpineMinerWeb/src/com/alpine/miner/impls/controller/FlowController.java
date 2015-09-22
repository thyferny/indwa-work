/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FlowController.java
 *
 * Author sam_zang
 * Version 3.0
 * Date Aug 20, 2011
 */

package com.alpine.miner.impls.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.datamining.workflow.util.ExecutableFlowExporter;
import com.alpine.datamining.workflow.util.OperaterSystemAdapter;
import com.alpine.datamining.workflow.util.OperaterSystemAdapterFactory;
import com.alpine.datamining.workflow.util.WorkFlowUtil;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.framework.DataStore;
import com.alpine.miner.framework.RequestUtil;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.editworkflow.flow.FlowDraftManager;
import com.alpine.miner.impls.editworkflow.flow.FlowDraftManager.DraftInfo;
import com.alpine.miner.impls.flowHistory.FlowHistoryInfo;
import com.alpine.miner.impls.flowHistory.FlowHistoryServiceFactory;
import com.alpine.miner.impls.license.LicenseManager;
import com.alpine.miner.impls.report.FlowResult;
import com.alpine.miner.impls.report.model.WebFlowReportGenerator;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.FlowInfoTree;
import com.alpine.miner.impls.web.resource.FlowResultInfo;
import com.alpine.miner.impls.web.resource.ModelInfo;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceFlowManager;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.interfaces.AnalysisModelManager;
import com.alpine.miner.interfaces.FlowHistoryService;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorModelImpl;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.reader.FlowMigrator;
import com.alpine.miner.workflow.runner.FlowRunningHelper;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.file.StringUtil;

/**
 * ClassName:FlowController
 *
 * Author   kemp zhang, john zhao, Sam Zang
 *
 * Version  Ver 1.0
 *
 * Date     2011-3-29    
 *
 * COPYRIGHT   2010 - 2011 Alpine Solutions. All Rights Reserved.    
 */
@Controller
@RequestMapping("/main/flow.do")
public class FlowController extends AbstractControler  {
    private static Logger itsLogger = Logger.getLogger(FlowController.class);
    public static final int RESULT_COLUMN_NUMBER = 7;

    protected static final String BAD_REQUEST = "Bad Request";
    protected static final int FLOW_NOT_FOUND = 1;
    protected static final int HAS_BEEN_UPDATED = 2;
    private static final int FLOW_ALREADY_EXIST = 3;
    private static final int COPY_FLOW_FAILED = 4;

    public static FlowController INSTANCE = null;
    

    public FlowController() throws Exception{
        super();
        //have to tell DataExplorerManagerImpl where to set the steprun finished when clear temp table
        INSTANCE=this;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(params = "method=getFlows", method = RequestMethod.GET)
    public void getFlows(String user, String type, HttpServletRequest request,
                         HttpServletResponse response, ModelMap model) throws IOException {
        try {
            if (checkUser(user, request, response) == false) {
                return;
            }

            String path = type;
            String str = null;
            if (type.equals(ResourceType.Personal.name())) {
                path = type + File.separator + user;
                str = ProtocolUtil.toJson(rmgr.getFlowList(path));
            } else {
                List<FlowInfoTree> total = new LinkedList<FlowInfoTree>();

                path = ResourceType.Public.name();
                FlowInfoTree tree = new FlowInfoTree(path);
                List list = rmgr.getFlowList(path);

                tree.addChildren(list);
                total.add(tree);
                total.addAll(list);

                // A group get. build a tree.
                String[] groups = ProviderFactory.getAuthenticator("").getUserGroups(user);
                if (groups != null && groups.length > 0) {
                    for (String g : groups) {
                        path = ResourceType.Group.name() + File.separator + g;
                        list = rmgr.getFlowList(path);

                        tree = new FlowInfoTree(g);
                        tree.addChildren(list);
                        total.add(tree);
                        total.addAll(list);
                    }
                }
                str = ProtocolUtil.toJson(total);
            }

            ProtocolUtil.sendResponse(response, str);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale()) ;
        }
    }

    @RequestMapping(params = "method=openWorkFlow", method = RequestMethod.POST)
    public void openWorkFlow(String user, HttpServletRequest request,
            HttpServletResponse response, ModelMap model) throws IOException {
    	 if (checkUser(user, request, response) == false) {
             return  ;
         }
         FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        
         try {
        	 ResourceFlowManager.instance.forceClearCache(info.getKey());// clear cache first to make sure always load latest flow info.
        	 FlowDraftManager.getInstance().clearDraft(user);
        	OperatorWorkFlow flow = rmgr.getFlowData(info,request.getLocale());
        	if (flow == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_NOT_FOUND));
                return  ;
        	}
        	
			FlowDTO flowDTO=  new FlowDTO(info, flow, user);
			FlowRunningHelper.getInstance().disposeStepRunner(user);
			
			ProtocolUtil.sendResponse(response,flowDTO);
		} catch (OperationFailedException e) {
			e.printStackTrace();
            //this is special for udf
            if(e.getMessage()!= null && e.getMessage().equals("1076")) {
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND, ErrorNLS.getMessage(ErrorNLS.UDF_MODEL_NOT_FOUND, request.getLocale())));
            }else{
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND, e.getMessage()));
            }
		}
         
    }

    @RequestMapping(params = "method=hasFlowDraft", method = RequestMethod.GET)
    public void hasFlowDraft(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	String user = getUserName(request);
    	boolean haveDraft = FlowDraftManager.getInstance().hasDraftFlow(user);
    	ProtocolUtil.sendResponse(response, haveDraft);
    }

    @RequestMapping(params = "method=openWorkFlowAsDraft", method = RequestMethod.GET)
    public void openWorkFlowAsDraft(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	String user = getUserName(request);
        try {
        	DraftInfo draftInfo = FlowDraftManager.getInstance().getDraft(user);
	       	if (draftInfo == null) {
               ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_NOT_FOUND));
               return  ;
	       	}
	       	OperatorWorkFlow flow = draftInfo.getWorkFlow();
	       	rmgr.updateFlow(draftInfo.getFlowInfo(), flow);//sync draft to edit cache.
			FlowDTO flowDTO=  new FlowDTO(draftInfo.getFlowInfo(), flow, user);
			
			FlowRunningHelper.getInstance().disposeStepRunner(user);

			
			Map resultContainer = new HashMap();
			resultContainer.put("flowInfo", draftInfo.getFlowInfo());
			resultContainer.put("flowDTO", flowDTO);
			ProtocolUtil.sendResponse(response, resultContainer);
		} catch (Exception e) {
			e.printStackTrace();
           //this is special for udf
           if(e.getMessage()!= null && e.getMessage().equals("1076")) {
               ProtocolUtil.sendResponse(response,
                       new ErrorDTO(FLOW_NOT_FOUND, ErrorNLS.getMessage(ErrorNLS.UDF_MODEL_NOT_FOUND, request.getLocale())));
           }else{
               ProtocolUtil.sendResponse(response,
                       new ErrorDTO(FLOW_NOT_FOUND, e.getMessage()));
           }
		}
    }
    
    @RequestMapping(params = "method=getFlowData", method = RequestMethod.POST)
    public void getFlowData(String user, HttpServletRequest request,
                            HttpServletResponse response, ModelMap model) throws IOException {


        if (checkUser(user, request, response) == false) {
            return  ;
        }

        FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        String version = info.getVersion();
        //get the full info (version has been changed after add)
        info=FlowVersionManager.INSTANCE.relaodFlowInfo(info) ;
        if (info == null) {
            ProtocolUtil.sendResponse(response,new ErrorDTO(FLOW_NOT_FOUND));
            return  ;
        }

        OperatorWorkFlow flow = null;
        try {
         //   info.setTmpPath("");
            if(info.getVersion().equals(version) ==false) {
                info =FlowVersionManager.INSTANCE.relaodFlowInfo4Version(info,version) ;
                //not the current version, it is a history version...
                flow = rmgr.getFlowData4Version(info,info.getVersion(),request.getLocale());

            }else{
                flow = rmgr.getFlowData(info,request.getLocale());
            }



            if (flow == null) {
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND));
                return  ;
            }
            FlowDTO flowDTO=  new FlowDTO(info, flow, user);
            ProtocolUtil.sendResponse(response,flowDTO);
        } catch ( Exception e) {
            e.printStackTrace();
            //this is special for udf
            if(e.getMessage()!=null&&e.getMessage().equals("1076")) {

                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND, ErrorNLS.getMessage(ErrorNLS.UDF_MODEL_NOT_FOUND, request.getLocale())));
            }else{
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND, e.getMessage()));

            }

        }

    }

    @RequestMapping(params = "method=copyFlowList", method = RequestMethod.POST)
    public void copyFlowList(String user, HttpServletRequest request,
                             HttpServletResponse response, ModelMap model) throws IOException {

        if (checkUser(user, request, response) == false) {
            return  ;
        }

        FlowInfo[] list = ProtocolUtil.getRequest(request, FlowInfo[].class);
        if (list == null) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(9999,BAD_REQUEST));
        }

        try{

            ResourceInfo.ResourceType type = ResourceInfo.ResourceType.Personal;
            FlowInfo dest = null;
            for (FlowInfo src : list) {
                dest = new FlowInfo(user, src.getId(), type);
                //this is importtant to get all the data...
                if(flowExists(dest)==false){
                    continue;
                }

            }
            String lastFlowNewVersion=FlowInfo.INIT_VERSION;
            for (FlowInfo src : list) {
                dest = new FlowInfo(user, src.getId(), type);

                if (rmgr.copyFlow(src, dest) == false) {
                    ProtocolUtil.sendResponse(response, new ErrorDTO(COPY_FLOW_FAILED));
                    return  ;
                }else{
                    lastFlowNewVersion = dest.getVersion();
                }
            }

            ProtocolUtil.sendResponse(response, new String("{\"newFlowVersion\":" +lastFlowNewVersion+"}"));
        }
        catch (Exception e) {

            generateErrorDTO(response, e,request.getLocale());
        }


    }

    @RequestMapping(params = "method=renameFlow", method = RequestMethod.POST)
    public void renameFlow(String newFlowName, HttpServletRequest request,
                              HttpServletResponse response, ModelMap model) throws IOException {

        try{
            String user = getUserName(request);
            //change the encoding to support the chinese

            newFlowName = getUTFParamvalue(newFlowName, request);
//            newFlowName = newFlowName.replaceAll(" ", "_");
            FlowInfo  srcFlow = ProtocolUtil.getRequest(request, FlowInfo.class);
            if (srcFlow == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(9999,BAD_REQUEST));
                return ;
            }
            String oldVersion = srcFlow.getVersion();
            ResourceInfo.ResourceType type = srcFlow.getResourceType();
            FlowInfo dest = null;
            //id = name
            dest = new FlowInfo(user, newFlowName, type);
            dest.setVersion(oldVersion);
            dest.setCategories(srcFlow.getCategories()); //renamed into same directory
            String path = rmgr.generateResourceKey(dest)+FilePersistence.INF;
            File file = new File(path);
            if(file.exists()==true){
                ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_ALREADY_EXIST));
                return;
            }

            if (rmgr.moveFlow(srcFlow, dest) == false) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(COPY_FLOW_FAILED));
                return ;

            }

            ProtocolUtil.sendResponse(response, dest);
        }
        catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale()) ;
        }

    }

    @RequestMapping(params = "method=duplicateFlow", method = RequestMethod.POST)
    public void duplicateFlow(String newFlowName, HttpServletRequest request,
                              HttpServletResponse response, ModelMap model) throws IOException {

        try{
            String user = getUserName(request);
            //change the encoding to support the chinese

            newFlowName = getUTFParamvalue(newFlowName, request);


            FlowInfo  srcFlow = ProtocolUtil.getRequest(request, FlowInfo.class);
            if (srcFlow == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(9999,BAD_REQUEST));
                return ;
            }

            ResourceInfo.ResourceType type = srcFlow.getResourceType();
            FlowInfo dest = null;
            //id = name
            dest = new FlowInfo(user, newFlowName, type);
            String path = rmgr.generateResourceKey(dest)+FilePersistence.INF;
            File file = new File(path);
            if(file.exists()==true){
                ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_ALREADY_EXIST));
                return;
            }
            if (rmgr.copyFlow(srcFlow, dest) == false) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(COPY_FLOW_FAILED));
                return ;

            }
            ProtocolUtil.sendResponse(response, new String[0]);
        }
        catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale()) ;
        }

    }

    @RequestMapping(params = "method=deleteFlowList", method = RequestMethod.POST)
    public void deleteFlowList(String user, String type,
                               HttpServletRequest request, HttpServletResponse response,
                               ModelMap model) throws IOException {
        try {
            if (checkUser(user, request, response) == false) {
                return  ;
            }

            FlowInfo[] list = ProtocolUtil
                    .getRequest(request, FlowInfo[].class);
            if (list == null) {
                generateErrorDTO(response, BAD_REQUEST, request.getLocale()) ;
                return  ;
            }

            for (FlowInfo src : list) {
                src.setModifiedUser(user);
                rmgr.deleteFlow(src);
            }

            getFlows(user, type, request, response, model);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }


    @RequestMapping(params = "method=shareFlow", method = RequestMethod.POST)
    public void shareFlow(String type, String name, String user, HttpServletRequest request,
                          HttpServletResponse response, ModelMap model) throws IOException {

        if (checkUser(user, request, response) == false) {
            return  ;
        }

        FlowInfo src = ProtocolUtil.getRequest(request, FlowInfo.class);
        if (src == null) {
            generateErrorDTO(response, BAD_REQUEST, request.getLocale()) ;
            return;
        }

        try{
            ResourceInfo.ResourceType rtype = ResourceInfo.ResourceType.valueOf(type);
            FlowInfo dest = new FlowInfo(user, src.getId(), rtype);

            if(rtype.equals(ResourceType.Group)){
                dest.setGroupName(name) ;
            }



            if (rmgr.copyFlow(src, dest) == false) {
                generateErrorDTO(response, "Create flow failed.", request.getLocale()) ;
            }else{
                returnSuccess(response) ;
            }
        }
        catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=publishFlow", method = RequestMethod.POST)
    public void publishFlow(HttpServletRequest request,
                            HttpServletResponse response,  String user, String pwd,   ModelMap model) throws IOException {
        try {
            FlowInfo flowInfo = ProtocolUtil
                    .getRequest(request, FlowInfo.class,true);
            if (flowInfo == null) {
                return;
            }
            String comments = flowInfo.getComments();
            //avoid "" in HTML text area MINERWEB-716
            if(comments == null||comments.trim().equals("")){
                comments =" ";
            }
            AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
            UserInfo u = auth.getUserInfoByName(user);

            String password =null;
            if(pwd!=null){
                password = (String)StringUtil.stringToObject(pwd);
            }
            if (user == null) {
                ProtocolUtil.sendResponse(response, Resources.NO_USER);
                return;
            } else if (u == null) {
                ProtocolUtil.sendResponse(response,Resources.NO_USER);
                return;
            }
            else if (auth.authenticate(user, password) == false) {
                ProtocolUtil.sendResponse(response,Resources.BAD_PASSWORD);
                return ;
            }
            //login OK...
            if (flowInfo.getResourceType().equals(ResourceType.Personal)){
                //already exist, add version
                if (flowExists(flowInfo) == true) {
                    String xmlString = flowInfo.getXmlString();
                    flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo) ;

                    String oldVersion= flowInfo.getVersion();
                    if(StringUtil.isEmpty(oldVersion)){
                        oldVersion=FlowInfo.INIT_VERSION;
                    }
                    int newVersion = Integer.parseInt(oldVersion) + 1;
                    FlowVersionManager.INSTANCE.copyFlowToHistory(flowInfo   );
                    //update current flow
                    flowInfo.setVersion(String.valueOf(newVersion));
                    flowInfo.setXmlString(xmlString);
                }else  {//find the previous version
                    String name = flowInfo.getId();
//					  user = flowInfo.getCreateUser();
                    if(FlowVersionManager.INSTANCE.hasHistoryVersion(name,user)==true){
                        String version = FlowVersionManager.INSTANCE.getLatestFlowVersion(name,user);
                        if(StringUtil.isEmpty(version)==false){
                            String newVersion = String.valueOf(Integer.parseInt(version) +1);
                            flowInfo.setVersion(newVersion) ;
                        }else{
                            flowInfo.setVersion(FlowInfo.INIT_VERSION) ;
                        }
                    }else{
                        flowInfo.setVersion(FlowInfo.INIT_VERSION) ;
                    }
                }
            }else {//public and group
                if (flowExists(flowInfo) == true) {
                    String xmlString = flowInfo.getXmlString();
                    //???
                    flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo) ;
                    String oldVersion= flowInfo.getVersion();
                    if(StringUtil.isEmpty(oldVersion)){
                        oldVersion=FlowInfo.INIT_VERSION;
                    }
                    int newVersion = Integer.parseInt(oldVersion) + 1;
                    FlowVersionManager.INSTANCE.copyFlowToHistory(flowInfo   );
                    //update current flow
                    flowInfo.setVersion(String.valueOf(newVersion));
                    flowInfo.setXmlString(xmlString);
                }else{//still need found the deleted version...
                    if(FlowVersionManager.INSTANCE.hasHistoryVersion(flowInfo)==true){
                        String version = FlowVersionManager.INSTANCE.getLatestFlowVersion(flowInfo);
                        if(StringUtil.isEmpty(version)==false){
                            String newVersion = String.valueOf(Integer.parseInt(version) +1);
                            flowInfo.setVersion(newVersion) ;
                        }else{
                            flowInfo.setVersion(FlowInfo.INIT_VERSION) ;
                        }
                    }else{
                        flowInfo.setVersion(FlowInfo.INIT_VERSION) ;
                    }
                }
            }
            flowInfo.setComments(comments) ;
            flowInfo.setModifiedUser(user) ;
            flowInfo.setModifiedTime(System.currentTimeMillis()) ;
            rmgr.saveFlowXMLData(flowInfo);
            ProtocolUtil.sendResponse(response, "success");
        } catch (Exception e) {
            e.printStackTrace();
            itsLogger.error(e.getMessage(),e) ;
            ProtocolUtil.sendResponse(response,
                    "ERROR:" + buildErrorString(e,request.getLocale()) );
        }
    }

    @RequestMapping(params = "method=exportFlow", method = RequestMethod.POST)
    public void exportFlow(String user, HttpServletRequest request,
                           HttpServletResponse response, ModelMap model) throws IOException {
        try{
            if (checkUser(user, request, response) == false) {
                return  ;
            }
            FlowInfo[] infos = ProtocolUtil.getRequest(request, FlowInfo[].class);
            if (infos == null) {
                generateErrorDTO(response, BAD_REQUEST, request.getLocale()) ;
                return  ;
            }
            String explorTempFlowPath = TempFileManager.INSTANCE.getTempFolder4Flow()+ File.separator  +user
                    + File.separator + "export_flow";
            File tempDir = new File(explorTempFlowPath);
            if(tempDir.exists()==false){
                tempDir.mkdir();
            }else{
                FileUtils.deleteDirectory(tempDir);
                tempDir = new File(explorTempFlowPath);
                if(tempDir.exists()==false){
                    tempDir.mkdir();
                }
            }
            for(FlowInfo info:infos){
                Persistence.INSTANCE.loadXMLFlowData(info);

                String xmlData =  info.getXmlString();
                String path = explorTempFlowPath +File.separator+info.getId()+Resources.AFM;
                saveData2File(path, xmlData);
            }
            String zipFileTemp = TempFileManager.INSTANCE.getTempFolder4Flow()+ File.separator  +user
                    + File.separator;
            String zipFileName = "export_flow.zip";
            File resultFile = new File(zipFileTemp+zipFileName) ;
            if(resultFile.exists()==true){
                resultFile.delete() ;
            }
            zipFolderToFile(explorTempFlowPath,zipFileTemp+zipFileName,request.getLocale());
            String url = zipFileName;
            ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(url));
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

	@RequestMapping(params = "method=exportExecFlow", method = RequestMethod.POST)
	public void exportExecFlow(String user, String adapter,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		try {
			if (checkUser(user, request, response) == false) {
				return;
			}
			FlowInfo[] infos = ProtocolUtil.getRequest(request,
					FlowInfo[].class);
			if (infos == null || infos.length == 0) {
				generateErrorDTO(response, BAD_REQUEST, request.getLocale());
				return;
			}
			String targetDirectory = TempFileManager.INSTANCE
					.getTempFolder4Flow()
					+ File.separator
					+ user
					+ File.separator + "export_flow_executable";
			String zipFileTemp = TempFileManager.INSTANCE.getTempFolder4Flow()
					+ File.separator + user + File.separator;
			File zipFileTempDir = new File(zipFileTemp);
			if (zipFileTempDir.exists() == false) {
				zipFileTempDir.mkdir();
			}
			File tempDir = new File(targetDirectory);
			if (tempDir.exists() == false) {
				tempDir.mkdir();
			} else {
				FileUtils.deleteDirectory(tempDir);
				tempDir = new File(targetDirectory);
				if (tempDir.exists() == false) {
					tempDir.mkdir();
				}
			}
			String zipFileName = "export_flow_executable.zip";
			String realFilePath = ExecutableFlowExporter.class.getResource("/")
					.toURI().getPath();
			String jarFileRootDir = realFilePath.substring(0,
					realFilePath.lastIndexOf("classes/"))
					+ "lib";
			String extraLibRootDir = jarFileRootDir;
			// String
			// extraLibRootDir=realFilePath.substring(0,realFilePath.lastIndexOf("classes/"))+"lib/../../chart_lib";

			for (FlowInfo info : infos) {
				String sourceFileName = Persistence.INSTANCE
						.generateResourceKey(info) + FilePersistence.AFM;
				String flowName = sourceFileName.substring(
						sourceFileName.lastIndexOf(File.separator) + 1,
						sourceFileName.length());

				OperatorWorkFlow workflow = WorkFlowUtil.getWorkflow(
						sourceFileName, request.getLocale(),
						ResourceType.Personal, user);

				if(!isWorkflowValid(workflow)){
					String msg = MessageFormat.format(PropertyResourceBundle.getBundle("app", request.getLocale()).getString("invalid_flow_var_name"), new String[]{workflow.getName()});
					ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, msg));
					return;
				}
				OperaterSystemAdapter systemAdapter = OperaterSystemAdapterFactory
						.getAdatper(adapter);
				String tempFlowFileName = handleModelPathAndSubflow(workflow,
						targetDirectory + File.separator
								+ AlpineAnalyticEngine.MODEL_FOLDER, user,
						flowName, request.getLocale(), targetDirectory);
				File sourceFile = new File(tempFlowFileName);
				File destFile = new File(targetDirectory + File.separator
						+ flowName);
				FileUtils.copyFile(sourceFile, destFile);
				ExecutableFlowExporter.exportExecutableFlow(targetDirectory,
						jarFileRootDir, extraLibRootDir, sourceFile,
						systemAdapter, true, hasHadoopOperator(workflow),
						LicenseManager.LICENSE_FILE, ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_LOG,PreferenceInfo.KEY_LOG_CUST_ID));
			}
			File resultFile = new File(zipFileTemp + zipFileName);
			if (resultFile.exists() == true) {
				resultFile.delete();
			}
			zipFolderToFile(targetDirectory, zipFileTemp + zipFileName,
					request.getLocale());

			String url = zipFileName;
			FileUtils.deleteDirectory(tempDir);
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(url));
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}

	private String handleModelPathAndSubflow(OperatorWorkFlow workflow,
			String modelSavePath, String userName, String flowName,
			Locale locale, String targetDirectory) throws Exception {
		// if have model operator with predictor, need save to model folder and
		// change the path to "./model/xxx.am" to make it can be auto reused
		if (workflow != null && workflow.getChildList() != null) {
			List<UIOperatorModel> childs = workflow.getChildList();
			for (UIOperatorModel uiOperatorModel : childs) {
				Operator operator = uiOperatorModel.getOperator();
				if (operator instanceof ModelOperator
						&& (operator.getParentOperators() == null || operator
								.getParentOperators().size() == 0)) {
					// a predicotr's model
					ModelOperator modelOperator = (ModelOperator) operator;
					String modelName = uiOperatorModel.getId();
					EngineModel engineModel = modelOperator.getModel();
					if (engineModel != null) {
						File modelFolder = new File(modelSavePath);
						if (modelFolder.exists() == false) {
							modelFolder.mkdir();
						}
						String modelFileName = modelSavePath + File.separator
								+ modelName + FilePersistence.SUFFIX_AM;
						ParameterUtility
								.getParameterByName(modelOperator,
										OperatorParameter.NAME_Model_File_Path)
								.setValue(
										"./"
												+ AlpineAnalyticEngine.MODEL_FOLDER
												+ File.separator + modelName
												+ FilePersistence.SUFFIX_AM);
						FileOutputStream outStream = null;
						try {

							OperatorWorkFlow workFlow = new OperatorWorkFlow();
							workFlow.setUserName(userName);
							workFlow.setVersion(FlowMigrator.Version_3);

							UIOperatorModel uiom = new UIOperatorModelImpl();
							uiom.setClassName("ModelOperator");
							uiom.setId(modelName);
							OperatorPosition operatorPosition = new OperatorPosition(
									100, 100);
							uiom.setPosition(operatorPosition);
							uiom.setUUID(System.currentTimeMillis() + "");

							uiom.initiateOperator(locale);
							uiom.getOperator().setOperModel(uiom);

							((ModelOperator) uiom.getOperator())
									.setModel(engineModel);
							workFlow.addChild(uiom);

							XMLWorkFlowSaver workflowSaver = new XMLWorkFlowSaver();

							File modelFile = new File(modelFileName);
							if (modelFile.exists()) {
								modelFile.delete();
							}
							workflowSaver.doSave(modelFileName, workFlow,
									userName, false);

							if (logger.isDebugEnabled()) {
								logger.debug("Execxutable flow export,model saved to :"
										+ modelFileName);
							}

						} catch (Exception e) {
							// not a big deal
							logger.error(e.getMessage(), e);
							throw e;
						} finally {
							if (outStream != null) {
								try {
									outStream.close();
								} catch (IOException e) {
									logger.error(e.getMessage(), e);
								}
							}
							modelOperator.setModel(null);
							// this time we done want to save model from global
							// cache
							modelOperator.setSaveModelFromCache(false);
						}
					}
				} else if (operator instanceof SubFlowOperator) {
					SubFlowOperator subflowOperator = (SubFlowOperator) operator;
					String subflowName = (String) ParameterUtility
							.getParameterValue(subflowOperator,
									OperatorParameter.NAME_subflowPath);
					if (StringUtil.isEmpty(subflowName) == false) {
						OperatorWorkFlow subworkflow = subflowOperator
								.getSubWorkflow();
						String subflowPath = handleModelPathAndSubflow(
								subworkflow, modelSavePath, userName,
								subflowName, locale, targetDirectory);
						File sourceFile = new File(subflowPath);

						File destFile = new File(targetDirectory
								+ File.separator + subflowName
								+ FilePersistence.AFM);
						FileUtils.copyFile(sourceFile, destFile);
					}
				}

			}
		}

		XMLWorkFlowSaver workflowSaver = new XMLWorkFlowSaver();
		String tmpPath = System.getProperty("java.io.tmpdir");
		if (tmpPath.endsWith(File.separator) == false) {
			tmpPath = tmpPath + File.separator;
		}
		tmpPath = tmpPath + "flow" + System.currentTimeMillis();
		new File(tmpPath).mkdir();
		tmpPath = tmpPath + File.separator + flowName;
		workflowSaver.doSave(tmpPath, workflow, userName, false);

		// save to temp file
		return tmpPath;
	}
    
    private boolean isWorkflowValid(OperatorWorkFlow workflow){
    	boolean isValid = true;
    	for(UIOperatorModel operatorModel : workflow.getChildList()){
    		isValid &= operatorModel.getOperator().isVaild(workflow.getParentVariableModel());
    	}
    	return isValid;
    }
	 
	private boolean hasHadoopOperator(OperatorWorkFlow workflow) {
		List<UIOperatorModel> child = workflow.getChildList();
		for (Iterator iterator = child.iterator(); iterator.hasNext();) {
			UIOperatorModel uiOperatorModel = (UIOperatorModel) iterator.next();
			if (true == isHadoopOperaotr(uiOperatorModel.getOperator())) {
				return true;
			}
		}
		return false;
	}

	private boolean isHadoopOperaotr(Operator operator) {
		if( operator instanceof HadoopOperator){
			return true;
		}else if(operator instanceof CopyToDBOperator){
			return true;
		}
		else if(operator instanceof SubFlowOperator){  
			List<Operator> child = ((SubFlowOperator)operator).getChildOperators();
			 if(child!=null){
				 for (Operator operator2 : child) {
					if(isHadoopOperaotr(operator2)){
						return true;
					}
				}
			 }
		}
		return false;
	}

    private void zipFolderToFile(String sourceFolder, String zipFileName,Locale locale)
    {
        File f = new File (zipFileName);
        if(f.exists()==false){
            try {
                f.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        itsLogger.info("Saving zip file to: " + f.getAbsolutePath());
        // create a ZipOutputStream to zip the data to
        ZipOutputStream zos =null;;
        try {
            zos = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            WebFlowReportGenerator generator = new WebFlowReportGenerator(locale);
            generator.zipDir(sourceFolder, zos,sourceFolder);


        } catch ( Exception e) {
            itsLogger.error(e.getMessage(),e) ;

            e.printStackTrace();
        }finally{
            if(zos!=null){
                try {
                    new File(sourceFolder).delete();
                    zos.close();

                } catch (IOException e) {
                    itsLogger.error(e.getMessage(),e) ;
                    e.printStackTrace();
                }
            }
        }
    }

    //save flow from the UI
    @RequestMapping(params = "method=completeUpdate", method = RequestMethod.POST)
    public void completeUpdate(String user, HttpServletRequest request,
                               HttpServletResponse response, ModelMap model) throws IOException {
        if (checkUser(user, request, response) == false) {
            return;
        }
        try {
            FlowDTO flowDTO = ProtocolUtil.getRequest(request, FlowDTO.class);
            if (flowDTO == null) {
                generateErrorDTO(response, BAD_REQUEST, request.getLocale());
                return;
            }
            FlowInfo flowInfo = flowDTO.getFlowInfo();
            flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo);
            String oldVersion = flowInfo.getVersion();
            // the init version flow
            if (oldVersion == null || oldVersion.trim().length() == 0) {
                oldVersion = FlowInfo.INIT_VERSION;
                flowInfo.setVersion(oldVersion);
            }
            int newVersion = Integer.parseInt(oldVersion) + 1;
            FlowVersionManager.INSTANCE.copyFlowToHistory(flowInfo);

            flowDTO.getFlowInfo().setVersion(String.valueOf(newVersion));
            OperatorWorkFlow flow;
            flow = rmgr.getFlowData(flowDTO.getFlowInfo(),request.getLocale());
            if (flow == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_NOT_FOUND));
                return;
            }
            // coordinate update
            flowDTO.updateCoordinate(flow);
            // now save the flow.
            rmgr.updateFlowFinish(flowDTO.getFlowInfo(), flow);
            FlowDraftManager.getInstance().clearDraft(user);
            FlowHistoryServiceFactory.getService(flowInfo.getModifiedUser()).pushNewFlowHistory(new FlowHistoryInfo(flowDTO.getFlowInfo()));
            ProtocolUtil.sendResponse(response, flowDTO.getFlowInfo());
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=cancelUpdate", method = RequestMethod.POST)
    public void cancelUpdate(String user, HttpServletRequest request,
                             HttpServletResponse response, ModelMap model) throws IOException {
        if (checkUser(user, request, response) == false) {
            return  ;
        }
        FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        if (info == null) {
            generateErrorDTO(response, BAD_REQUEST, request.getLocale());
            return;
        }
        try {
//        	StepedAnalyticRunner.removeContext(info.getKey());
        	FlowRunningHelper.getInstance().disposeStepRunner(user) ;
            rmgr.updateFlowCancel(info);
            FlowDraftManager.getInstance().clearDraft(user);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
        returnSuccess(response);
    }

    @RequestMapping(params = "method=getFlowHistoryByUser", method = RequestMethod.GET)
    public void getFlowHistoryByUser(String userName, HttpServletRequest request,HttpServletResponse response) throws Exception{
        try {
            FlowHistoryService service = FlowHistoryServiceFactory.getService(userName);
            Collection<FlowHistoryInfo> history = service.getFlowHistory();
            ProtocolUtil.sendResponse(response, history);
        } catch (IOException e) {
            itsLogger.error(e.getMessage(),e);
            generateErrorDTO(response, e, request.getLocale())  ;
        }
    }

    @RequestMapping(params = "method=pushNewFlowHistory", method = RequestMethod.POST)
    public void pushNewFlowHistory(HttpServletRequest request,HttpServletResponse response) throws IOException{
        FlowInfo  flowInfo = ProtocolUtil.getRequest(request, FlowInfo.class);
        try {
            String userName = getUserName(request);
            FlowHistoryService service = FlowHistoryServiceFactory.getService(userName);
            service.pushNewFlowHistory(new FlowHistoryInfo(flowInfo));
        } catch (IOException e) {
            generateErrorDTO(response, e, request.getLocale())  ;
        }
    }

    @RequestMapping(params = "method=removeFlowHistory", method = RequestMethod.POST)
    public void removeFlowHistory(HttpServletRequest request,HttpServletResponse response) throws IOException{
        FlowInfo  flowInfo = ProtocolUtil.getRequest(request, FlowInfo.class);
        try {
            String userName = getUserName(request);
            FlowHistoryService service = FlowHistoryServiceFactory.getService(userName);
            service.removeFlowHistory(new FlowHistoryInfo(flowInfo));
        } catch (IOException e) {
            generateErrorDTO(response, e, request.getLocale())  ;
        }
    }

    @RequestMapping(params = "method=clearFlowHistory", method = RequestMethod.GET)
    public void clearFlowHistory(String userName, HttpServletRequest request,HttpServletResponse response){
        FlowHistoryService service = FlowHistoryServiceFactory.getService(userName);
        service.clearFlowHistory();
    }


    @RequestMapping(params = "method=getFlowResultInfoList", method = RequestMethod.GET)
    public void getFlowResultInfoList(  HttpServletRequest request,
                                        HttpServletResponse response, ModelMap model) throws IOException {
        try {
            String user = getUserName(request);

            List<FlowResultInfo> flows = rmgr.getFlowResultInfos(user);

            ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(flows));
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }
    }

    @RequestMapping(params = "method=getFlowResultInfoData", method = RequestMethod.GET)
    public void getFlowResultInfoData(String flowName,String uuid, HttpServletRequest request,
                                      HttpServletResponse response, ModelMap model) throws IOException {
        try {
            String user = getUserName(request);

            flowName = getUTFParamvalue(flowName, request);

            String json =rmgr.getFlowResultJsonStrInfo(user, flowName, uuid);

            initGroupByModule(user,json,uuid,flowName,request,request.getLocale());

            ProtocolUtil.sendResponse(response, json);
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }
    }

    //For Logistic/Linear Regression group by
    private void initGroupByModule(String userName,String jsonString,String uuid,String flowName,HttpServletRequest request,Locale locale){
        if(null!=jsonString){
            //init group by module
            if(jsonString.indexOf("\"visualType\":199")!=-1 || jsonString.indexOf("\"visualType\":200")!=-1){
               FilePersistence persistence = new FilePersistence();
               List<ModelInfo> modelInfos = null;
                try {
                    modelInfos = persistence.getModelInfoList(userName,flowName,"",locale);
                    for(int i=0;i<modelInfos.size();i++){
                        ModelInfo info = modelInfos.get(i);
                        if(info.getId().equals(uuid)==true){
                            AlpineUtil.VALUE_PASSER.set(getUserName(request));
                            AnalysisModelManager.INSTANCE.getModelVisualization(info,request.getLocale());
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @RequestMapping(params = "method=deleteFlowResultInfo", method = RequestMethod.POST)
    public void deleteFlowResultInfo(HttpServletRequest request,
                                     HttpServletResponse response, ModelMap model) throws IOException {
        FlowInfo[]  flowInfos = ProtocolUtil.getRequest(request, FlowInfo[].class);
        try {
            String user = getUserName(request);
            boolean[] results = new boolean[flowInfos.length];
            int i = 0;
            for(FlowInfo flow : flowInfos){
// 				String flowName = getUTFParamvalue(flowName, request);
                results[i++] = rmgr.deleteFlowResultInfo(user, flow.getKey(), flow.getId()) ;
            }
            ProtocolUtil.sendResponse(response, results);
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }
    }
    
    @RequestMapping(params = "method=exportHTMLResult", method = RequestMethod.POST)
    public void exportHTMLResult(  HttpServletRequest request,String flowName,boolean isIE, boolean forChorus,
                                   HttpServletResponse response, ModelMap model) throws IOException {
        //en : us...
        flowName= getUTFParamvalue(flowName, request) ;
        FlowResult flowResult = ProtocolUtil.getRequest(request, FlowResult.class,isIE);
        if (flowResult == null) {
            generateErrorDTO(response, BAD_REQUEST, request.getLocale()) ;
        }else{
            WebFlowReportGenerator generator= new WebFlowReportGenerator(request.getLocale()) ;
            try {
                String zipFileName = generator.exportHTMLReport(flowName,   flowResult, forChorus);
                //zipFileName is based on the rootpath...
                ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(zipFileName));
            } catch (Exception e) {
                generateErrorDTO(response, e, request.getLocale())  ;
                return;
            }
        }
    }

} 
