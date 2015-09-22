/**
 * ClassName RecordParserFactory.java
 *
 * Version information: 1.00
 *
 * Date: Oct 30, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.ext;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import com.alpine.hadoop.AlpineHadoopConfKeySet;

/**
 * @author John Zhao
 *
 */
public class RecordParserFactory implements AlpineHadoopConfKeySet{
 
	
	public static String VALUE_XPATH_DELIMITER = ","; 
 
	public static RecordParser createRecordParser(Configuration config) throws Exception {
		String fileFormat = config.get(INPUT_FORMAT_KEY) ;
		if(fileFormat!=null){
			if(true==fileFormat.equalsIgnoreCase( INPUT_FORMAT_VALUE_CSV )){
				
		 
				String delimiter =",";
				if( config.get( DELIMITER_CHAR)!=null
						&& config.get( DELIMITER_CHAR).toString().length()!=0){
					delimiter = config.get( DELIMITER_CHAR);
				}  
				
				String escapChar = config.get( ESC_CHAR);
				String quoteChar = config.get( QUOTE_CHAR);
				if(escapChar!=null&&escapChar.trim().length()!=0
						&&quoteChar!=null&&quoteChar.trim().length()!=0){
					 
					return  new CSVRecordParser(delimiter.charAt(0), quoteChar.charAt(0), escapChar.charAt(0));
				}else {
					 return   new CSVRecordParser(delimiter.charAt(0) );
				}
				
				 
			}else if(true==fileFormat.equalsIgnoreCase( INPUT_FORMAT_VALUE_XML )){
				 List<String> xpathList = new ArrayList<String>();
				String xpathStr = config.get( XML_XPATH_LIST_KEY) ;
				if(xpathStr!=null){
					String[] xpathArray = xpathStr.split( VALUE_XPATH_DELIMITER) ;
					for (int i = 0; i < xpathArray.length; i++) {
						xpathList.add(xpathArray[i]);
					}
				}
				String sturctureType = config.get(XML_TYPE_TAG_KEY);
				String containerPath = config.get(XML_CONTAINER_PATH_TYPE_TAG_KEY);
				return new XMLRecordParser(xpathList,sturctureType,containerPath);
			}else if(true==fileFormat.equalsIgnoreCase( INPUT_FORMAT_VALUE_LOG )){
				String logFormat = config.get(LogParserFactory.LOG_FORMAT ) ;
				String logType = config.get( LogParserFactory.LOG_TYPE) ;
				
				return new LogRecordParser(logFormat,logType) ;
			}else if(true==fileFormat.equalsIgnoreCase( INPUT_FORMAT_VALUE_JSON )){
				List<String> jsonPathList = new ArrayList<String>();
				String jsonPathStr = config.get(JSON_PATH_TAG_KEY) ;
				if(jsonPathStr!=null){
					String[] jsonPathArray = jsonPathStr.split(AlpineHadoopConfKeySet.VALUE_XPATH_DELIMITER) ;
					for (int i = 0; i < jsonPathArray.length; i++) {
						jsonPathList.add(jsonPathArray[i]);
					}
				}
				String sturctureType = config.get(JSON_TYPE_TAG_KEY);
				String containerPath = config.get(JSON_CONTAINER_PATH_TYPE_TAG_KEY);
				return new JSONRecordParser(jsonPathList,sturctureType,containerPath);
			}else{
				throw new Exception("Can not create the RecordParser due to the unsipported file format:" + fileFormat) ;
			}
		} 
		else{
			throw new Exception("Can not create the RecordParser due to no file format") ;
		}
	}
}
