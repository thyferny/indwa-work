package com.alpine.miner.impls.controller.util;

import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.interfaces.resource.Persistence;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Servlet implementation class CommonFileDownLoaderServlet
 */
public class CommonFileDownLoaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CommonFileDownLoaderServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		StringBuffer path = new StringBuffer();
		String tempType = request.getParameter("tempType");
		if(null==tempType || "".equals(tempType)){
			return ;
		}else if("temp_model".equals(tempType)){
			path.append(TempFileManager.INSTANCE.getTempFolder4Model());
		}else if("temp_report".equals(tempType)){
			path.append(TempFileManager.INSTANCE.getTempFolder4Report());
		}else if("temp_flow".equals(tempType)){
			path.append(TempFileManager.INSTANCE.getTempFolder4Flow());
		}
		
		
		String fileName = request.getParameter("downloadFileName");
		String filePath = request.getParameter("filePath");
		//System.out.println(path+filePath+fileName);
		File downLoadFile = new File(path+filePath+fileName);
		//reset outputstream
		response.reset(); 
		response.setCharacterEncoding(Persistence.ENCODING);
		response.setHeader("Content-Disposition", "attachment;filename="+fileName.replaceAll(" ", "_"));
		// charset=utf-8
		
		if(fileName.contains(".zip")){
			response.setContentType("application/zip");
		}else{
			response.setContentType("text/xml;charset=utf-8");
		}
		
	    //OutputStreamWriter os=new OutputStreamWriter( response.getOutputStream(),Persistence.ENCODING);//
		OutputStream os=new BufferedOutputStream( response.getOutputStream());	
		//read file
		InputStream fis = new BufferedInputStream(new FileInputStream(downLoadFile));
		byte[] buffer = new byte[(int) downLoadFile.length()];
		fis.read(buffer);
		fis.close();
		os.write(buffer);
		os.close();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
