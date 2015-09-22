/**
 * 
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.interfaces.resource.Persistence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author sam_zang
 *
 */
public class ProtocolUtil {
	static Gson gson = new GsonBuilder()
						.setDateFormat(java.text.DateFormat.LONG)
						.setPrettyPrinting()
						.serializeSpecialFloatingPointValues()
						.create();
		
	/**
	 * Sending a string back to client.
	 * 
	 * @param response
	 * @param str
	 * @throws IOException
	 */
	public static void sendResponse(HttpServletResponse response, String str) throws IOException {
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(str);
	}

	/**
	 * Sending a string back to client.
	 *
	 * @param response
	 * @param str
	 * @throws IOException
	 */
	public static void sendResponse4XML(HttpServletResponse response, String str) throws IOException {
		response.setContentType("text/xml; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(str);
	}

	
	/**
	 * Sending a Java object back to client.
	 * 
	 * @param response
	 * @param obj
	 * @throws IOException
	 */
	public static void sendResponse(HttpServletResponse response, Object obj) throws IOException {
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(gson.toJson(obj));
	}

    /**
     * Sending a workflow image response
     *
     * @param response
     * @param imgPath
     * @throws IOException
     */
    public static void sendChorusImgResponse(HttpServletResponse response, String imgPath) throws IOException {
        response.setContentType("image/png");
        response.setStatus(HttpServletResponse.SC_OK);
        File f = new File(imgPath);
        BufferedImage bi = ImageIO.read(f);
        OutputStream out = response.getOutputStream();
        ImageIO.write(bi, "png", out);
        out.close();
    }

    /**
     * Sending a failed api authorization response.
     *
     * @param response
     * @param obj
     * @throws IOException
     */
    public static void sendChorusAuthFailure(HttpServletResponse response, Object obj) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(gson.toJson(obj));
    }

    /**
     * Sending a no session authorization response.
     *
     * @param response
     * @param obj
     * @throws IOException
     */
    public static void sendChorusSessionFailure(HttpServletResponse response, Object obj) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(gson.toJson(obj));
    }
	/**
	 * Get the object from client request.
	 * 
	 * @param <T>
	 * @param request
	 * @param classOfT
	 * @return
	 */
	public static <T> T getRequest(HttpServletRequest request, Class<T> classOfT) {		 
		try {
//			request.getHeaderNames()
//			request.getAttributeNames()
			String json = getRequestContent(request);
//			String str= new String(json.getBytes(),"utf-8");
			T obj = gson.fromJson(json, classOfT);
			return obj;
		} catch (Exception e) {
			// maybe some one send bad data. ignore error.
			e.printStackTrace();
		}
		return null;
	}
	 
	/**
	 * Convert an object to JSON string.
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}
	
	public static <T> T toObject(String json, Class<T> clazz){
		return gson.fromJson(json, clazz);
	}
	
	/**
	 * Get the request content as a string.
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private static String getRequestContent(HttpServletRequest request)
			throws IOException {
		StringBuffer contentBuilder = new StringBuffer();
		String line = null;
		// make sure the flow name works at safari
		BufferedReader reader = null;
		if (request.getHeader("user-agent").indexOf("Safari") > -1) {

			InputStreamReader isr = new InputStreamReader(request
					.getInputStream(), Persistence.ENCODING);
			reader = new BufferedReader(isr);
		} else {
			reader = request.getReader();
		}

		while ((line = reader.readLine()) != null) {
			contentBuilder.append(line);
		}

		return contentBuilder.toString();
	}
//User-Agent
	 public static <T> T getRequest(HttpServletRequest request, Class<T> classOfT,boolean isIE) {
		try {
			String json = getRequestContent(request,isIE);
			T obj = gson.fromJson(json, classOfT);
			return obj;
		} catch (Exception e) {
			// maybe some one send bad data. ignore error.
			e.printStackTrace();
		}
		return null;
	}

private static String getRequestContent(HttpServletRequest request, boolean isIE) throws IOException {
	StringBuffer contentBuilder = new StringBuffer();
	String line = null;
	//make sure the flow name and 
	BufferedReader reader = null;
//	if (request.getHeader("user-agent").indexOf("Safari") > -1) {

		InputStreamReader isr = new InputStreamReader(request.getInputStream(), Persistence.ENCODING);
		reader = new BufferedReader(isr);
//	} else {
	//	reader = request.getReader();
//	}
	while ((line = reader.readLine()) != null) {
		contentBuilder.append(line);
	}
	
	String content=     contentBuilder.toString();
//	if(isIE ==true){
//		content= new String(content.getBytes(SysConfigManager.INSTANCE.getServerEncoding()),Persistence.ENCODING);
//	}
	return content;
}
}
