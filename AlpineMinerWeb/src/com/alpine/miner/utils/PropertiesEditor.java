/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * PropertiesEditor.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class PropertiesEditor {

	public static Map readProp(String configPath) throws IOException {
		File file = new File(configPath);
		if(file.exists()==false){
			file.createNewFile();
			return new Properties();
		}else{
			Properties props= new Properties();
			FileInputStream is = new FileInputStream(file); 
			try{
				props.load(is) ;
			}finally{
				is.close();
			}
			return  props;
		}
 	}

	public static void storeProp(Map<String, String> returnProps,
			String configPath) throws Exception {
		
		Properties porps = new   Properties();
		for (Iterator iterator = returnProps.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next(); 
			String val = returnProps.get(key);
			if(val != null){
				porps.setProperty(key, val);
			}
		}
		OutputStream os = new FileOutputStream(new File(configPath));
		try{
			porps.store(os, "") ; 
		}finally{
			os.close();
		}
	}
 
}
