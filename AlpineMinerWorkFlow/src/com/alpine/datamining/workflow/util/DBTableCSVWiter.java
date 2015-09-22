/**
 * ClassName  CSVWiter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-7-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 * 
 */
public class DBTableCSVWiter {
    private static final Logger itsLogger=Logger.getLogger(DBTableCSVWiter.class);
    public static final String SEPARATER_CHAR = ",";
	
	public static String write(String filePath,
			String[] columns,List<String[]> items,boolean overwrite) throws AnalysisException		  {
		FileOutputStream stream=null;
		try {
			
			 //avoid the "table" name
			filePath=filePath.replace("\"", "");
			File file = new File(filePath);
			if(overwrite==false){
				while(file.exists()==true){
					filePath=filePath.substring(0,filePath.length()-4)+"_new.csv";
					file = new File( filePath);
		 		}
			}
		 
				stream = new FileOutputStream(file);
			
			StringBuffer sb=new StringBuffer();
			for (int i = 0; i < columns.length; i++) {
				if(i!=0){
					sb.append(SEPARATER_CHAR);
				}
				sb.append(StringHandler.doubleQ(columns[i]));
			}
			sb.append("\n");

			for(String[] ss:items){
				for(String s:ss){
					sb.append(s).append(SEPARATER_CHAR);
				}
				if(sb.length()>0){
					sb=sb.deleteCharAt(sb.length()-1);
				}
				sb.append("\n");
				if(sb.length()>5000){
					stream.write(sb.toString().getBytes());
					sb=new StringBuffer();
				}
			}
			if(sb.length()>0){
				stream.write(sb.toString().getBytes());
			}
		} catch ( Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}finally{
			if(stream!=null){
				try {
					stream.close();
				} catch (IOException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
		}
		return filePath;
	}

	public static String write(String filePath,String[] columns,List<String[]> items)
			throws Exception {
		String str=null;
		try {
			str = write(filePath,columns,items,false);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		}
		return str;
	}

}