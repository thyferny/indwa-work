package com.alpine.utility.tools;

import java.util.Locale;

public interface ValidationManager {
	
	public static ValidationManager instance=new ValidationManagerImpl();
	
	
	//resource name include the flowname and dbconnection name now
	//If return value is null,validte is ok,if not null,the return value is error message.
	public String validateResourceName(String resourceName,Locale locale );
	public String validateTableName(String tableName,Locale locale ,String dbType);
	public String validateColumnName(String columnName,Locale locale ,String dbType);

}
