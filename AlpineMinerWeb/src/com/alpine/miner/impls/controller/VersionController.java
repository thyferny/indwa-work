/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * DBConnectionController.java
 * 
 * Author zhaoyong
 * 
 * Version 2.0
 * 
 * Date Aug 07, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.miner.interfaces.TempFileManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/main/flow/version.do")
public class VersionController extends AbstractControler  {
	
	public static final String VERSION_DOWNLOAD = "version_";

	public VersionController() throws Exception{
		super();
	}
	 
 
	@RequestMapping(params = "method=getFlowVersionInfos", method = RequestMethod.POST)
	public String getFlowVersionInfos(
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {

		try{
			String user = getUserName(request);
			
			FlowInfo[] infos = ProtocolUtil.getRequest(request, FlowInfo[].class);
			List<FlowInfo>  allVersions = new ArrayList<FlowInfo> (); 
			
			if(infos!=null){
				for(int i = 0;i<infos.length;i++){
					List<FlowInfo> versions = FlowVersionManager.INSTANCE.getFlowVersionInfos(infos[i]);
					if(versions!=null){
						allVersions.addAll(versions);
					}
						
				}
				//sort by  version
				Comparator< FlowInfo> comparator = new Comparator< FlowInfo>(){
					@Override
					public int compare(FlowInfo o1, FlowInfo o2) {
						return Integer.valueOf(o2.getVersion()).compareTo(Integer.valueOf(o1.getVersion()));
					}
				}; 
				Collections.sort(allVersions, comparator) ;
				
				
			}
			
			
			
			
			
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(allVersions));	
			//version, comments...
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
		return null;
	}
//1 copy to history
//replace with 	fromversion
	@RequestMapping(params = "method=replaceWithVersion", method = RequestMethod.POST)
	public void replaceWithVersion(   
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		try{
	 
				FlowInfo flowInfo = (FlowInfo) ProtocolUtil.getRequest(request,FlowInfo.class );
				flowInfo = FlowVersionManager.INSTANCE.replaceWithVersion(flowInfo,request.getLocale()) ;
				//now flowInfo is the latest one with the old content..
//				String fromversion =flowInfo.getVersion();
//				if(StringUtil.isEmpty(version)) {
//					version =FlowInfo.INIT_VERSION;
//				}
//				
//				
//				int newVersion = Integer.parseInt(version) + 1 ;
//				flowInfo.setVersion(String.valueOf(newVersion)) ;
				
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(flowInfo));
			
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
 
	}
	

	@RequestMapping(params = "method=downLoadFlowVersions", method = RequestMethod.POST)
	public void downLoadFlowVersions(  String flowName, String resourceType,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		try{
			String user = getUserName(request);
	 
				FlowInfo flowInfo = (FlowInfo) ProtocolUtil.getRequest(request,FlowInfo.class );
		 
				//System.out.println("real path: " + webAppPath);

				// copy flow file to export location and return path
				// to client.
				String version = flowInfo.getVersion(); 
				String fileName  = rmgr.getFlowVersionPath(flowInfo, Integer.parseInt(version));
				fileName = fileName+ Resources.AFM;
				File f= new File(fileName);
				String xmlData =  "";
				if(f.exists()==false){
					//could be the first version...
					flowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo) ;
					//if(flowInfo.getVersion().equals(version)){
					  xmlData = 	 rmgr.readFile(rmgr.generateResourceKey(flowInfo)+Resources.AFM);
					//}//
				}else{
					 xmlData = 	 rmgr.readFile(fileName);	
				}
				 
				
				String newFileName = flowInfo.getId()+FilePersistence.FLOW_SUFFIX_VERSION + flowInfo.getVersion()+Resources.AFM; 
				String path = TempFileManager.INSTANCE.getTempFolder4Flow() + File.separator + user 
					+ File.separator +newFileName;
			 
				 
				saveData2File(path, xmlData);
//				response.setContentType("application/x-download" ) ;
//				response.setHeader("Content-Disposition", "attachment;filename="+"version_download"+"_"+user 
//						+ File.separator +newFileName); 
//				response.getWriter().write(ProtocolUtil.toJson(newFileName)) ;
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(newFileName));
			
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
 
	}
	 
}

