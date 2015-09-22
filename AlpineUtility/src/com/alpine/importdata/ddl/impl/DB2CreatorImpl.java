/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * DB2CreatorImpl.java
 */
package com.alpine.importdata.ddl.impl;

import java.util.Locale;

import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ddl.TableCreator;
import com.alpine.utility.db.DB2SqlType;
import com.alpine.utility.db.DbConnection;

/**
 * @author Gary
 * Aug 21, 2012
 */
public class DB2CreatorImpl extends TableCreator {

	/**
	 * @param connInfo
	 */
	public DB2CreatorImpl(DbConnection connInfo) {
		super(connInfo);
	}
	public DB2CreatorImpl(String userName,String password,String url,String system,Locale locale,String useSSL){
		super(userName,password, url, system, locale ,useSSL);
	}
	/* (non-Javadoc)
	 * @see com.alpine.importdata.ddl.TableCreator#convertDataType(com.alpine.importdata.DatabaseDataType)
	 */
	@Override
	protected String convertDataType(DatabaseDataType columnType) {
		switch(columnType){
		case BOOLEAN: 
			return "VARCHAR (10)";
		case CHAR: 
			return "VARCHAR (10)";
		case DATETIME:
			return "TIMESTAMP";
		case DATE:
			return "DATE";
		case DOUBLE:
			return "DOUBLE";
		case INTEGER:
			return "INTEGER";
		case NUMERIC:
			return "DOUBLE";
		case VARCHAR:
			return "VARCHAR (1000)";
        case BIGINT:
            return "BIGINT";
		default:
			throw new IllegalArgumentException(columnType.toString());
		}
	}
}
