/**
 * ClassName RecordParser.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-24
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.ext;

import java.util.List;

/**
 * @author Jeff Dong
 *
 */
public interface RecordParser {
	//@Deprecated
	//public String[] parse(String aRecordContent) throws Exception;
	
	//Since we have to support the nested XML and JSON, we have to get a list here
	public List<String[]> parse(String aRecordContent) throws Exception;

	//this is only for the LOG and CSV since it will be faster... avoid generate a list with size =1
	public String[] parseLine(String string) throws Exception;

}
