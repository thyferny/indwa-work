/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * PostgresCreatorImpl.java
 */
package com.alpine.importdata.ddl.impl;

import java.util.Locale;

import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ddl.TableCreator;
import com.alpine.utility.db.DbConnection;

/**
 * @author Robbie
 * Sep 25, 2012
 */
public class GreenplumCreatorImpl extends TableCreator {

    /**
     * @param connInfo
     */
    public GreenplumCreatorImpl(DbConnection connInfo) {
        super(connInfo);
    }
    public GreenplumCreatorImpl(String userName,String password,String url,String system,Locale locale,String useSSL){
        super(userName,password, url, system, locale ,useSSL);
    }
    @Override
    protected String convertDataType(DatabaseDataType dataType){
        switch(dataType){
            case BOOLEAN:
                return "boolean";
            case CHAR:
                return "\"char\"";
            case DATETIME:
                return "timestamp with time zone";
            case DATE:
                return "date";
            case DOUBLE:
                return "double precision";
            case INTEGER:
                return "integer";
            case NUMERIC:
                return "numeric";
            case VARCHAR:
                return "text";
            case BIGINT:
                return "bigint";
            default:
                throw new IllegalArgumentException(dataType.toString());
        }
    }
}