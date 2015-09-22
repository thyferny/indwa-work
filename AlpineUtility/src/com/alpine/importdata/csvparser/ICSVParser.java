/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ICSVParser.java
 */
package com.alpine.importdata.csvparser;

import java.io.InputStream;

/**
 * @author Gary
 * Aug 15, 2012
 */
public interface ICSVParser {

	ICSVParser INSTANCE = new CSVParserImpl();
	
	/**
	 * to parse csvStream, it will pass columns to rowHandler for each row.
	 * and will be handle closing after parse.
	 * @param csvStream
	 * @param separator
	 * @param quote
	 * @param escape
	 */
	void parseCSV(InputStream csvStream, boolean includeHeader, char separator, char quote, char escape, ParseHandler rowHandler) throws Exception;
	
	/**
	 * to parse csvStream
	 * @param csvStream
	 * @param separator
	 * @param quote
	 * @param escape
	 * @param parseCount
	 * @param rowHandler
	 */
	void parseCSV(InputStream csvStream, boolean includeHeader, char separator, char quote, char escape, int limintCount, ParseHandler rowHandler) throws Exception;
	
	interface ParseHandler{
		void parseRow(String[] columns, int rowIdx) throws Exception;
	}
}
