/**
 * ClassName ProfileReader.java
 *
 * Version information: 1.00
 *
 * Data: 2011-6-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.profile.ProfileUtility;


public class ProfileReader {
	private static final Logger itsLogger = Logger.getLogger(ProfileReader.class);
	private static ProfileReader instance=null;
	
	
	
	private static final String FILE_NAME="AlpineMinerUI.prefs";
	private static final String FILE_PATH=System.getProperty("user.home")+
	File.separator+"alpineworkspace"+Resources.minerEdition+File.separator+".metadata"
	+File.separator+".plugins"+File.separator+"org.eclipse.core.runtime"
	+File.separator+".settings"+File.separator+FILE_NAME;
//	private static final String FILE_PATH_TEMP="C:\\Tools\\Eclipse\\runtime-AlpineMinerUI.product\\"+".metadata"
//	+File.separator+".plugins"+File.separator+"org.eclipse.core.runtime"
//	+File.separator+".settings"+File.separator+FILE_NAME;
	private   Properties properties=null;
	
	private ProfileReader(boolean init){
		   properties=new Properties();
		   if( init== true){
			   loadProperties();
		   }
	}
	
	public static ProfileReader getInstance(){
		if(instance==null){
			instance=new ProfileReader(true);
		}
		return instance;
		
	}
	
	public static ProfileReader getInstance(boolean init){
		if(instance==null){
			instance=new ProfileReader(init);
		}
		return instance;
		
	}
	
	private   FileInputStream getFile(){
		FileInputStream in=null;
		File f=new File(FILE_PATH);
		if(!f.exists()){
			itsLogger.debug(ProfileReader.class.getName()+":File not exists");
			return null;
		}
		try {
			itsLogger.debug(ProfileReader.class.getName()+" "+FILE_PATH+":exists");
			in = new FileInputStream(f);
			return in;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public   Properties loadProperties(){
		FileInputStream in=null;
		try {
			in=getFile();
			if(in!=null){
				properties.clear();
				properties.load(in);
			}
			return properties;
		} catch (FileNotFoundException e) {
			itsLogger.error(e);
			return null;
		} catch (IOException e) {
			itsLogger.error(e);
			return null;
		}finally{
			try {
				if(in!=null){
					in.close();
				}			
			} catch (IOException e) {
				itsLogger.error(e);
			}
		}
	}
	
	
	
	public   String getParameter(String para){	
		String value=null;
		if(StringUtil.isEmpty(properties.getProperty(para))==false){
			value=properties.getProperty(para);
		}else{
			value=ProfileUtility.INSTANCE.getPreferenceDefaltValue(para);
		}
		if(itsLogger.isInfoEnabled()){
			itsLogger.info(ProfileReader.class.getName()+":key:"+para+";value:"+value);
		}
		return value;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
}
