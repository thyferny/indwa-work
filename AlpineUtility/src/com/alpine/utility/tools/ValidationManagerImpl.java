package com.alpine.utility.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.alpine.utility.file.StringUtil;

public class ValidationManagerImpl implements ValidationManager {

	private static String[] invalidateChars = new String[]{
		"/", "\0",
		"\\", "/", ":", "*", "?", "\"", "<", ">", "|"
	}; 
	private static final List<String> invalidateChar4ResourceName = Arrays.asList(invalidateChars) ;
	
	@Override
	public String validateColumnName(String columnName, Locale locale,
			String dbType) {
				return null;
	
	}

	@Override
	public String validateResourceName(String resourceName, Locale locale) {
		if(StringUtil.isEmpty(resourceName)){
			return "Resource name can not be null";
		}
		for(String s:invalidateChar4ResourceName){
			if(resourceName.indexOf(s)>-1){
				return "Character not supported in resource name :\'"+s+"'";
			}
		}
		return null;
	}

	@Override
	public String validateTableName(String tableName, Locale locale,
			String dbType) {
		// TODO Auto-generated method stub
		return null;
	}

}
