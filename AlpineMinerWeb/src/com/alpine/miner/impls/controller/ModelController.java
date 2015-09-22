/**
 * ClassName  DBTableContainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ModelInfo;
import com.alpine.miner.interfaces.AnalysisModelManager;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author John Zhao
 * 
 */
@Controller
@RequestMapping("/main/model.do")
public class ModelController extends AbstractControler {
    private static Logger itsLogger = Logger.getLogger(ModelController.class);
    //public static final String EXPORT_MODEL = "model";

	public ModelController() throws Exception{
		super();
	}

	//not used yet for now
	@RequestMapping(params = "method=deleteModelList", method = RequestMethod.POST)
	public void deleteModelList(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {
		try{
			ModelInfo[] modelInfo = ProtocolUtil.getRequest(request, ModelInfo[].class);
			if (modelInfo == null) {
				return  ;
			}
			for(int i= 0 ;i< modelInfo.length;i++){
				AnalysisModelManager.INSTANCE.deleteModel(  modelInfo[i]) ;
			}
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
		returnSuccess(response) ;
	}
	
	@RequestMapping(params = "method=replaceModel", method = RequestMethod.POST)
	public void replaceModel(HttpServletRequest request,
			HttpServletResponse response,String modelName, ModelMap model) throws Exception {

		ModelForm form = ProtocolUtil.getRequest(request, ModelForm.class);
		ModelInfo newModel = form.getModelInfo();
		FlowInfo flowInfo = form.getFlowInfo();
		if (newModel == null) {
			return  ;
		}
		try{
			AnalysisModelManager.INSTANCE.replaceModel(flowInfo, modelName, newModel ,request.getLocale());
	
		ProtocolUtil.sendResponse(response,ProtocolUtil.toJson(flowInfo)	);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	 
	}
	
	@RequestMapping(params = "method=getModelList", method = RequestMethod.GET)
	public void getModelList(HttpServletRequest request,
			HttpServletResponse response, String flowName,String categories,String modelName, 
			ModelMap model) throws Exception {
 		itsLogger.info(LogUtils.entry("ModelController", "getModelList",
                 " flowName=" + flowName + " categories=" + categories + " modelName=" + modelName));
 	try{
 		String user = getUserName(request); 
 		flowName = getUTFParamvalue(flowName, request);
 		modelName = getUTFParamvalue(modelName, request); 
 		List<ModelInfo> modelList=AnalysisModelManager.INSTANCE.getModelInfoList(user, flowName, modelName,request.getLocale()) ;

 		
 		ProtocolUtil.sendResponse(response,modelList	);
 	} catch (Exception e) {
		generateErrorDTO(response, e, request.getLocale());
	}
	}
	
	@RequestMapping(params = "method=getModelVisualization", method = RequestMethod.POST)
	public void getModelVisualization(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws Exception {

		ModelInfo modelInfo = ProtocolUtil.getRequest(request, ModelInfo.class,isIE(request));
		if (modelInfo == null) {
			return  ;
		}
		try{
            AlpineUtil.VALUE_PASSER.set(getUserName(request));
			String visualJson  = AnalysisModelManager.INSTANCE.getModelVisualization(modelInfo,request.getLocale());
	
			ProtocolUtil.sendResponse(response, visualJson	);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
	
	private boolean isIE(HttpServletRequest request) {
				String browserName = request.getHeader("user-agent");    
		// is IE
		if(browserName.indexOf("MSIE")!=-1){
			return true;
		}
		else {
			return false;
		}
	}

	//this is for model download
	@RequestMapping(params = "method=exportModel", method = RequestMethod.POST)
	public void exportModel(  HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {
		try{
			String user = getUserName(request);
			
			ModelInfo info = ProtocolUtil.getRequest(request, ModelInfo.class);
			if (info == null) {
				return  ;
			}
		 
			String folder = TempFileManager.INSTANCE.getTempFolder4Model() + File.separator +user; 
			File file = new File(folder) ;
			if(false==file.exists()) {
				file.mkdir();
			}
			String fileName = "model" + Resources.AFM;  
			//avoid the URL error...
	//		fileName = fileName.replaceAll(" ", "_") ;
			String path = folder + File.separator + fileName;
			try {
				AnalysisModelManager.INSTANCE.saveModelInFlowFile(path, info,request.getLocale()) ;
			} catch (Exception e) {
				generateErrorDTO(response, e, request.getLocale()) ;	
			}
		 
	 
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(fileName));
 
		}
		  catch (Exception e) {
				generateErrorDTO(response, e, request.getLocale());
			}
	}
	
	public static class ModelForm{
		private FlowInfo flowInfo;
		private ModelInfo modelInfo;
		public FlowInfo getFlowInfo() {
			return flowInfo;
		}
		public ModelInfo getModelInfo() {
			return modelInfo;
		}
	}
}
