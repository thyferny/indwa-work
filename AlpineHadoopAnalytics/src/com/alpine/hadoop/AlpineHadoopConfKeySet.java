/**
 * ClassName FileFormatHelper.java
 *
 * Version information: 1.00
 *
 * Date: Oct 30, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop;


/**
 * @author John Zhao
 *
 */
public interface AlpineHadoopConfKeySet {
	
	public static String DELIMITER_CHAR="alpine.hadoop.delimiter";
	public static String HEADER_LINE_VALUE="alpine.hadoop.header";
	public static String ESC_CHAR = "alpine.hadoop.escchar";
	public static String QUOTE_CHAR = "alpine.hadoop.quotechar";
	public static String COLUMN_TYPES="alpine.hadoop.columnTypes";
	public static String COLUMN_NAMES="alpine.hadoop.columnNames";
	public static String ALPINE_PREFIX="alp.prefix.";
	public static String Hadoop_Reduce_Number="mapred.reduce.tasks";
	public static String Hadoop_Util_Reduce_Number_Seted="alp.hadoop.reduce.tasks.seted";
	public static String ALPINE_BAD_COUNTER="alp.bad.counter";
	public static String TYPE_NOT_MATCH="type.not.match";
	
	public static final String KEY_MR_COMPRESS = "mapred.output.compress";
	public static final String KEY_MR_COMPRESS_CODE = "mapred.output.compression.codec";
	
	public static String SKIP_LINE = "alpine.hadoop.skipline";
	public static String DIRTY_PATH = "alpine.hadoop.dirtyfilepath";
	
	public static String VALUE_XPATH_DELIMITER = ","; 
	public static final String XML_START_TAG_KEY = "alpine.xmlinput.start";
	public static final String XML_END_TAG_KEY = "alpine.xmlinput.end";
	public static final String XML_XPATH_LIST_KEY = "alpine.xmlinput.xpaths";
	public static final String XML_TYPE_TAG_KEY = "alpine.xmlinput.type";
	public static final String XML_CONTAINER_PATH_TYPE_TAG_KEY = "alpine.xmlinput.containerpath";
	
	public static final String JSON_START_TAG_KEY = "alpine.jsoninput.start";
	public static final String JSON_PATH_TAG_KEY = "alpine.jsoninput.jsonpaths";
	public static final String JSON_TYPE_TAG_KEY = "alpine.jsoninput.type";
	public static final String JSON_CONTAINER_PATH_TYPE_TAG_KEY = "alpine.jsoninput.containerpath";
	
	public static final String INPUT_FORMAT_KEY = "alpine.input.format";//XML, TEXT FILE,JSON,APACHE LOG  
	
	public static final String INPUT_FORMAT_VALUE_CSV = "Text File" ;
	public static final String INPUT_FORMAT_VALUE_XML = "XML";
	public static final String INPUT_FORMAT_VALUE_JSON = "JSON";
	public static final String INPUT_FORMAT_VALUE_LOG = "Log File" ;
}
