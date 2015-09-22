/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OracleCreatorImpl.java
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
public class OracleCreatorImpl extends TableCreator {

	/**
	 * @param connInfo
	 */
	public OracleCreatorImpl(DbConnection connInfo) {
		super(connInfo);
	}
	public OracleCreatorImpl(String userName,String password,String url,String system,Locale locale,String useSSL){
		super(userName,password, url, system, locale ,useSSL);
	}
	@Override
	protected String convertDataType(DatabaseDataType dataType){
		switch(dataType){
		case BOOLEAN: 
			return "varchar2(10)";
		case CHAR: 
			return "char(255)";
		case DATETIME:
			return "timestamp";
		case DATE:
			return "date";
		case DOUBLE:
			return "number";
		case INTEGER:
			return "number";
		case NUMERIC:
			return "number";
		case VARCHAR:
			return "varchar2(4000)";
        case BIGINT:
            return "NUMBER";
		default:
			throw new IllegalArgumentException(dataType.toString());
		}
	}
}
