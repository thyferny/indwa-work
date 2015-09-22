/**
 * ClassName HadoopUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.utility.hadoop.HadoopConnection;
import org.apache.log4j.Logger;import com.google.gson.Gson;
/**
 * @author Eason
 * 
 */
public class HadoopUtility {
    private static final Logger itsLogger = Logger.getLogger(HadoopUtility.class);
    private static final String OP_DELETE = "DELETE";
	private static final String OP_GET_FILE_STATUS = "GETFILESTATUS"; 
	private static final String OP_LIST_STATUS = "LISTSTATUS";

	 
	public static boolean removeFiles(String path,
			HadoopConnection connection, boolean recursive) throws Exception {
		String url=connection.getHDFSUrl();//.getWebHDFSUrl();
		url = url+path+"?user.name=" +connection.getUserName()+
				"&op=" +OP_DELETE+"&recursive="+(recursive?"true":"false");
	 
		DeleteMethod delete=new DeleteMethod(url);
		
		HttpClient httpclient = new HttpClient();
		
		int result=0;
		
		try {
			result=httpclient.executeMethod(delete);//403 
			if (result != 200) {
				if(result==403){
//					DBUIError.No_Permission
					throw new RuntimeException("");
				}else{
//					DBUIError.Connection_Failed+": "
//					+ result
					throw new RuntimeException("");
				}
			}
			StringBuffer sb = new StringBuffer();
	 
			InputStream rstream = null;
		    rstream = delete.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	sb.append(line.replace("boolean", "alpineBoolean"));
		    }
		    br.close();
		   Gson gson = new  Gson();
		   DeleteStatus status =gson.fromJson(sb.toString(), DeleteStatus.class); 
		 if(status != null && status.isAlpineBoolean() != false){
			 return true;
		 }else{
			 return false;
		 }
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			return false;

		}finally {
			delete.releaseConnection();
		}
	}
	 
	  
	public static String readHadoopFileTOString(List<String> path,
			HadoopConnection connection) throws Exception {
	    int lineCount = 0;
	    int maxLineCount = 200;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < path.size(); i++){
			String url=connection.getHDFSUrl();//.getWebHDFSUrl();
			url = url+path.get(i)+"?user.name=" +connection.getUserName()+
			"&op=" +"OPEN";
			
			GetMethod get=new GetMethod(url);
			
			HttpClient httpclient = new HttpClient();
			
			int result=0;
			
			try {
				result=httpclient.executeMethod(get);//403 
				if (result != 200) {
					if(result==403){
						throw new RuntimeException("DBUIError.No_Permission");
					}else{
						throw new RuntimeException("DBUIError.Connection_Failed+");
					}
				}
		 
				InputStream rstream = null;
			    rstream = get.getResponseBodyAsStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
			    String line;
			    while ((line = br.readLine()) != null) {
			    	String newLineChar = System.getProperty("line.separator");
			    	sb.append(line);
			    	if(!line.endsWith(newLineChar)){
			    		sb.append(newLineChar);
			    	}
			    	lineCount++;
			    	if(lineCount >= maxLineCount){
					    br.close();	
			    		return sb.toString();
			    	}
			    }
			    br.close();	
			    rstream.close();
			    get.releaseConnection();
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				throw e;
			}
		}
		return sb.toString();
	}
	public static boolean fileExists(String path,
			HadoopConnection connection) throws Exception { 
		String url=connection.getHDFSUrl();//.getWebHDFSUrl();
		url = url+path+"?user.name=" +connection.getUserName()+
				"&op=" +OP_GET_FILE_STATUS;
	 
		GetMethod get=new GetMethod(url);
		
		HttpClient httpclient = new HttpClient();
		
		int result=0;
		
		try {
			result=httpclient.executeMethod(get);//403 
			if (result != 200) {
				if(result==404){
					return false;
				}else{
					throw new RuntimeException(""+result);
				}
			}
			StringBuffer sb = new StringBuffer();
	 
			InputStream rstream = null;
		    rstream = get.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	sb.append(line);
		    }
		    br.close();
		   Gson gson = new  Gson();
		   FileStatus status =gson.fromJson(sb.toString(), FileStatus.class); 
		 if(status!=null){
			 return true;
		 }
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		}finally {
			get.releaseConnection();
		}
		return  false;
	}
 
	public static String getDelimiterValue(
			AnalysisFileStructureModel fileStructureModel) {
		
		if(fileStructureModel instanceof AnalysisCSVFileStructureModel){
			AnalysisCSVFileStructureModel csvFileStructureModel=(AnalysisCSVFileStructureModel)fileStructureModel;
			String delimiter = csvFileStructureModel.getDelimiter();
			String otherValue =csvFileStructureModel.getOther();
			return getRealDelimiterValue(delimiter, otherValue);
		}else{
			return AnalysisCSVFileStructureModel.DELIMITER_VALUE[1];
		}
		
	}

	public static String getDelimiterValue(
			AnalysisCSVFileStructureModel fileStructureModel) {
		String delimiter = fileStructureModel.getDelimiter();
		String otherValue =fileStructureModel.getOther();
		return getRealDelimiterValue(delimiter, otherValue);
	}

	public static String getRealDelimiterValue(String delimiter,
			String otherValue) {
		String delimiterValue="";
		String[] delimiters = AnalysisCSVFileStructureModel.DELIMITER;
		for(int i=0;i<delimiters.length;i++){
			if(delimiter.equals(delimiters[i])){
				if(i==delimiters.length-1){
					
					delimiterValue=otherValue ;
				}else{
					delimiterValue=AnalysisCSVFileStructureModel.DELIMITER_VALUE[i];
				}
				break;
			}
		}
		return delimiterValue;
	}
}
