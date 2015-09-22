package com.alpine.hadoop.ext;

import com.alpine.logparser.ApacheLogParser;
import com.alpine.logparser.IAlpineLogParser;
import com.alpine.logparser.Log4JLogParser;

public class LogParserFactory {
	public static final String LOG_TYPE="LOG_TYPE";
	public static final String LOG_FORMAT="LOG_FORMAT";
	
	public static final String LOG_TYPE_APACHE_WEB_SERVER = "Apache Web Server";
	public  static final String LOGTYPE_APACHE = "Apache Log";
	public  static final String LOGTYPE_LOG4J = "Log4J";
	public  static final String[] SUPPORTED_LOG_TYPES=new String[]{LOGTYPE_APACHE,LOGTYPE_LOG4J,LOG_TYPE_APACHE_WEB_SERVER};
	
	public static IAlpineLogParser createALogParser(String logFormat,String logType){
		if(null==logType||null==logFormat){
			throw new IllegalArgumentException("Neither Log format nor log type can be null");
		}
		
		if(logType.equalsIgnoreCase(LOGTYPE_LOG4J)){
			return new Log4JLogParser(logFormat);
		}else if(logType.equalsIgnoreCase(LOGTYPE_APACHE)||logType.equalsIgnoreCase(LOG_TYPE_APACHE_WEB_SERVER)){
			return new ApacheLogParser(logFormat);
		}else{
			throw new IllegalArgumentException("Log format must be either["+LOGTYPE_APACHE+", or"+LOGTYPE_LOG4J+", or"+
					LOG_TYPE_APACHE_WEB_SERVER+"]");
		}
		
	}
	
}
