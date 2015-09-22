/**
 * ClassName JDBCProperties.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eason 
 */
public class JDBCProperties {

	private static List<JDBCProperties> jdbcProperties = new ArrayList<JDBCProperties>();
    private String name;
    private String defaultPort;
    private String urlPrefix;
    private String dbNameSeperator;
    private String integerName;
    private String realName;
    private String floatName;
    private String varcharName;
    private String textName;
    private String identifierQuoteOpen;
    private String identifierQuoteClose;
    private String valueQuoteOpen;
    private String valueQuoteClose;
    
    
    public JDBCProperties(String name, 
    		String defaultPort, 
    		String urlPrefix, 
    		String dbNameSeperator,
            String varcharName,
            String textName,
            String integerName, 
            String realName,
            String floatName,
            String identifierQuoteOpen,
            String identifierQuoteClose,
            String valueQuoteOpen,
            String valueQuoteClose) {
        this.name = name;
        this.defaultPort = defaultPort;
        this.urlPrefix = urlPrefix;
        this.dbNameSeperator = dbNameSeperator;
        this.varcharName = varcharName;
        this.textName = textName;
        this.integerName = integerName;
        this.realName = realName;
        this.floatName = floatName;
        this.identifierQuoteOpen = identifierQuoteOpen;
        this.identifierQuoteClose = identifierQuoteClose;
        this.valueQuoteOpen = valueQuoteOpen;
        this.valueQuoteClose = valueQuoteClose;
    }

    public String getDbNameSeperator() {
        return dbNameSeperator;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    public String getName() {
        return name;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String getIntegerName() {
        return integerName;
    }
    
	public String getTextName() {
		return textName;
	}
	
    public String getRealName() {
        return realName;
    }
    
    public String getFloatName() {
        return floatName;
    }
    
    public String getVarcharName() {
        return varcharName;
    }
    
    public String getIdentifierQuoteOpen() {
    	return this.identifierQuoteOpen;
    }
    
    public String getIdentifierQuoteClose() {
    	return this.identifierQuoteClose;
    }
    
    public String getValueQuoteOpen() {
    	return this.valueQuoteOpen;
    }
    
    public String getValueQuoteClose() {
    	return this.valueQuoteClose;
    }
    
    
    static {
        jdbcProperties.add(new JDBCProperties("PostgreSQL",
                "5432",
                "jdbc:postgresql://",
                "/",
                "VARCHAR",
                "BLOB",
                "INTEGER",
                "REAL",
                "FLOAT",
                "\"",
                "\"",
                "'",
                "'"));
        jdbcProperties.add(new JDBCProperties("Greenplum",
                "5432",
                "jdbc:postgresql://",
                "/",
                "VARCHAR",
                "BLOB",
                "INTEGER",
                "REAL",
                "FLOAT",
                "\"",
                "\"",
                "'",
                "'"));
        jdbcProperties.add(new JDBCProperties("Oracle",
                "1521",
                "jdbc:oracle:thin:@",
                ":",
                "VARCHAR2",
                "BLOB",
                "INTEGER",
                "REAL",
                "FLOAT",
                "\"",
                "\"",
                "'",
                "'"));
        jdbcProperties.add(new JDBCProperties("DB2",
                "50000",
                "jdbc:db2://",
                "/",
                "VARCHAR",
                "BLOB",
                "INTEGER",
                "REAL",
                "FLOAT",
                "\"",
                "\"",
                "'",
                "'"));
        jdbcProperties.add(new JDBCProperties("Netezza",
                "5480",
                "jdbc:netezza://",
                "/",
                "VARCHAR",
                "BLOB",
                "INTEGER",
                "REAL",
                "FLOAT",
                "\"",
                "\"",
                "'",
                "'"));
    }
 
   public static List<JDBCProperties>  getJDBCProperties() {
	   return jdbcProperties;
	   
   }
}
