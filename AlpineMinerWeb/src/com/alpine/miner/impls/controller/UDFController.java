/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AdminController.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.miner.workflow.operator.customize.UDFManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/main/udf.do")
public class UDFController extends AbstractControler {
    private static Logger itsLogger = Logger.getLogger(UDFController.class);

    public UDFController() throws Exception {
		super();

	}

	@RequestMapping(params = "method=getUDFModels", method = RequestMethod.GET)
	public void getUDFModels(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {

		try {
			String user = getUserName(request);
			List<CustomizedOperatorModel> models = UDFManager.INSTANCE
					.getAllCustomizedOperatorModels();

			String resultJson = ProtocolUtil.toJson(models);
			ProtocolUtil.sendResponse(response, resultJson);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	@RequestMapping(params = "method=deleteUDFModel", method = RequestMethod.POST)
	public void deleteUDFModel(HttpServletRequest request,
			HttpServletResponse response, String operatorName,ModelMap model) throws IOException {
		 
		try {
			UDFManager.INSTANCE.deleteCustomizedOperatorModels(operatorName);
			returnSuccess(response) ;
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	@RequestMapping(params = "method=uploadUDFModels", method = RequestMethod.POST)
	public void uploadUDFModels(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {

		try {
			String user = getUserName(request);

			MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
			Iterator<String> itFileName = req.getFileNames();
			if (itFileName.hasNext() == false) {
				generateErrorDTO(response,"Import udf file not found !", request.getLocale()) ;	
	 
				return;
			} else {
				while (itFileName.hasNext()) {
					String fn = itFileName.next();
					// avoid error
					if (fn == null || fn.trim().length() == 0) {
						continue;
					}
					// System.out.println("file: " + fn);
					List<MultipartFile> fileList = req.getFiles(fn);

					// 1st, validate all file names before processing.
					for (MultipartFile f : fileList) {
						importUDFFile(user, f);

					}
				}
			}
			ProtocolUtil.sendResponse(response,true);

			// return nothing
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

	}

	private void importUDFFile(String user, MultipartFile f) throws Exception {
		String fileName = f.getOriginalFilename();

		// save to a temp file for udf manager
		String tempDir = System.getProperty("java.io.tmpdir"); 
		String filePath = tempDir + File.separator + "UDF_"
				+ System.currentTimeMillis() + fileName;

		InputStream in = f.getInputStream();

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}

		} finally {
			if (out != null) {
				out.close();
				out.flush();
			}
			itsLogger.info("Temp udf file saved:" + filePath);
		}

		UDFManager.INSTANCE.importUDFFile(filePath);
	}
}
