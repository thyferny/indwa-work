/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ICreateParameters.java
 */
package com.alpine.importdata.ddl;

import java.util.List;

import com.alpine.importdata.DatabaseDataType;

/**
 * @author Gary
 * Aug 21, 2012
 */
public interface ICreateParameter {

	String getSchemaName();
	
	String getTableName();
	
	List<columnMetaInfo> getColumnMetaList();
	
	interface columnMetaInfo{
		
		String getColumnName();
		
		DatabaseDataType getColumnType();
	}
}
