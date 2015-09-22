/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * NZCreatorImpl.java
 */
package com.alpine.importdata.ddl.impl;

import java.util.Locale;

import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ddl.TableCreator;
import com.alpine.utility.db.DbConnection;

/**
 * @author Gary
 * Aug 21, 2012
 */
public class NZCreatorImpl extends TableCreator {

	/**
	 * @param connInfo
	 */
	public NZCreatorImpl(DbConnection connInfo) {
		super(connInfo);
	}
	public NZCreatorImpl(String userName,String password,String url,String system,Locale locale,String useSSL){
		super(userName,password, url, system, locale ,useSSL);
	}
	/* (non-Javadoc)
	 * @see com.alpine.importdata.ddl.TableCreator#convertDataType(com.alpine.importdata.DatabaseDataType)
	 */
	@Override
	protected String convertDataType(DatabaseDataType columnType) {
		switch(columnType){
		case BOOLEAN: 
			return "BOOL";
		case CHAR: 
			return "CHAR";
		case DATETIME:
			return "TIMESTAMP";
		case DATE:
			return "DATE";
		case DOUBLE:
			return "FLOAT";
		case INTEGER:
			return "INTEGER";
		case NUMERIC:
			return "NUMERIC";
		case VARCHAR:
			return "VARCHAR(1000)";
        case BIGINT:
            return "BIGINT";
		default:
			throw new IllegalArgumentException(columnType.toString());
		}
	}
}
