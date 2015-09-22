/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ImportController.java
 *
 * Author sam_zang
 *
 * Version 2.0
 *
 * Date Jun 23, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.utility.common.VariableModelUtility;
import com.alpine.utility.log.LogPoster;
import com.alpine.utility.xml.XmlDocManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@RequestMapping("/main/flow/import_flow.do")
public class ImportController extends AbstractControler {

    public ImportController() throws Exception {
        super();
    }


    @RequestMapping(params = "method=importFlow", method = RequestMethod.POST)
    public String importFlow(String user, String comments, HttpServletRequest request,
                             HttpServletResponse response, ModelMap model) throws IOException {

        try {
            if (user == null) {
                user = getUserName(request);
            }
            if (comments != null) {
                comments = getUTFParamvalue(comments, request);
            }

            MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
            Iterator<String> itFileName = req.getFileNames();
            ResourceInfo.ResourceType rtype = ResourceInfo.ResourceType.Personal;
            List<FlowInfo> flowList = new LinkedList<FlowInfo>();
            while (itFileName.hasNext()) {
                String fn = itFileName.next();
                //avoid error
                if (fn == null || fn.trim().length() == 0) {
                    continue;
                }
                // System.out.println("file: " + fn);
                List<MultipartFile> fileList = req.getFiles(fn);

                // 1st, validate all file names before processing.
                for (MultipartFile f : fileList) {
                    String fileName = f.getOriginalFilename();

                    int endIndex = fileName.length() - Resources.AFM.length();
                    String name = fileName.substring(0, endIndex);
                    //replace space to '_', to make sure export function work well.
//                    name = name.replaceAll(" ", "_");

                    FlowInfo flowInfo = new FlowInfo(user, name, rtype);

                    String filePath = FilePersistence.FLOWPREFIX + flowInfo.getResourceType() + File.separator + user + File.separator + fileName;
                    File file = new File(filePath);
                    if (file.exists() == true) {
                        //get full info
                        flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo);

                        String oldVersion = flowInfo.getVersion();
                        //the init version flow
                        if (oldVersion == null || oldVersion.trim().length() == 0) {
                            oldVersion = FlowInfo.INIT_VERSION;
                            flowInfo.setVersion(oldVersion);
                        }
                        int newVersion = Integer.parseInt(oldVersion) + 1;
                        FlowVersionManager.INSTANCE.copyFlowToHistory(flowInfo);

                        flowInfo.setVersion(String.valueOf(newVersion));
                        flowInfo.setModifiedTime(System.currentTimeMillis());


                    } else if (hasHistoryVersion(name, user) == true) {
                        String version = getLatestFlowVersion(name, user);
                        if (version != null) {
                            String newVersion = String.valueOf(Integer.parseInt(version) + 1);
                            flowInfo.setVersion(newVersion);
                        } else {
                            flowInfo.setVersion(FlowInfo.INIT_VERSION);
                        }
                        flowInfo.setModifiedTime(System.currentTimeMillis());

                    } else {
                        flowInfo.setVersion(FlowInfo.INIT_VERSION);
                    }

                    System.out.println("import flow :new version =" + flowInfo.getVersion());
                    InputStream in = f.getInputStream();
                    StringBuilder sb = new StringBuilder();

                    int b;
                    while ((b = in.read()) != -1) {
                        sb.append((char) b);
                    }
                    String nativeString = sb.toString();
                    String xmlString = getUTF8StringFromImport(nativeString);

                    flowInfo.setXmlString(xmlString);
                    flowInfo.setComments(comments);
                    flowInfo.setModifiedUser(user);
                    //clear the temp path, so that it can get latest flow, not the cache
                    flowList.add(flowInfo);

                }
            }

            if (flowList.size() != 0) {
                // save the flows.
                for (FlowInfo info : flowList) {
                    rmgr.saveFlowXMLData(info);
                    String path = rmgr.generateResourceKey(info) + FilePersistence.INF;
                    //update the cache
                    rmgr.storeFlowInfo(path, info);

                }
                returnSuccess(response);
            }
            // return nothing
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());  //TODO need to add error string
        }
        return null;
    }


    @RequestMapping(params = "method=newFlow", method = RequestMethod.POST)
    public void newFlow(String user, String flowName, String comments,String catKey, String defaultPrefix, HttpServletRequest request,
                        HttpServletResponse response, ModelMap model) throws IOException {
        try {
            if (user == null) {
                user = getUserName(request);
            }
            if (comments != null) {
                comments = getUTFParamvalue(comments, request);
            }
            if (flowName != null) {
                flowName = getUTFParamvalue(flowName, request);
            }

            if (catKey != null)
            {
                catKey = getUTFParamvalue(catKey,request);
            }

            if (checkUser(user, request, response) == false) {
                return;
            }
            ResourceInfo.ResourceType rtype = ResourceInfo.ResourceType.Personal;
//            flowName = flowName.replaceAll(" ", "_");

            FlowInfo flowInfo = new FlowInfo(user, flowName, rtype);
            flowInfo.setVersion(FlowInfo.INIT_VERSION);
            flowInfo.setModifiedTime(System.currentTimeMillis());

            VariableModel vmodel = new VariableModel();
            if (defaultPrefix != null && defaultPrefix.trim().length()>0) vmodel.addVariable(VariableModelUtility.DEFAULT_PREFIX, defaultPrefix.trim());

            //now create empty xml
            XmlDocManager xmlDoc = new XmlDocManager();
            xmlDoc.createXmlDoc();
            Element root = xmlDoc.getXmlDoc().createElement("Process");
            root.setAttribute("UserName", user);
            root.setAttribute("Description", comments);
            root.setAttribute("Version", com.alpine.utility.db.Resources.minerEdition);
            xmlDoc.getXmlDoc().appendChild(root);
           root.appendChild(vmodel.toXMLElement(xmlDoc.getXmlDoc()));
            flowInfo.setXmlString(xmlDoc.xmlToString(null));
            flowInfo.setComments(comments);
            flowInfo.setModifiedUser(user);
            if (catKey != null && catKey.length()>0)
                flowInfo.setCategories(new String[]{catKey});

            String flowKey = rmgr.generateResourceKey(flowInfo); 
			// flow is ready, assuming that it's unique - now it's time to check if the flow already exists.
            String path = flowKey + FilePersistence.INF;
            File f= new File(path);
            if (f.exists())
            {
                String msg = ResourceBundle.getBundle("app", request.getLocale()).getString("createflow_alert_alreadyexists");

                ProtocolUtil.sendResponse(response,new ErrorDTO(ErrorDTO.UNKNOW_ERROR,msg));
                return;
            }


            //okay, flow is set - now save it.
            rmgr.saveFlowXMLData(flowInfo);
            //update the cache
            rmgr.storeFlowInfo(path, flowInfo);
            //init  the cache
            rmgr.getFlowData(flowInfo, request.getLocale());
            //returnSuccess(response);
            ProtocolUtil.sendResponse(response,flowInfo);
            LogPoster.getInstance().createAndAddEvent(LogPoster.Flow_Create, user);
            // return nothing
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }


    /**
     * @param flowName
     * @param user
     * @return
     */
    private String getLatestFlowVersion(String flowName, String user) {

        return FlowVersionManager.INSTANCE.getLatestFlowVersion(flowName, user);
    }


    /**
     * @param flowName
     * @param user
     * @return
     */
    private boolean hasHistoryVersion(String flowName, String user) {

        return FlowVersionManager.INSTANCE.hasHistoryVersion(flowName, user);
    }

}
