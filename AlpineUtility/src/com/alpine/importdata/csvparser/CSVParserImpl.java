/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * CSVParserImpl.java
 */
package com.alpine.importdata.csvparser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Gary
 * Aug 15, 2012
 */
public class CSVParserImpl implements ICSVParser {
	
	private static Logger log = Logger.getLogger(CSVParserImpl.class);

	/* (non-Javadoc)
	 * @see com.alpine.importdata.ICSVParser#parseCSV(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, com.alpine.importdata.ICSVParser.ParseHandler)
	 */
	@Override
	public void parseCSV(InputStream csvStream, boolean includeHeader, char separator, char quote,
			char escape, ParseHandler rowHandler) throws Exception {
		parseCSV(csvStream, includeHeader, separator, quote, escape, -1, rowHandler);
	}

	/* (non-Javadoc)
	 * @see com.alpine.importdata.ICSVParser#parseCSV(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, int, com.alpine.importdata.ICSVParser.ParseHandler)
	 */
	@Override
	public void parseCSV(InputStream csvStream, boolean includeHeader, char separator, char quote,
			char escape, int limintCount, ParseHandler rowHandler) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream));
		CSVReader parser = new CSVReader(reader, separator, quote, escape);
		try {
			String[] row;
			int rowIdx = 0;
			if(includeHeader){
				parser.readNext();// skip first row
			}
			while((row = parser.readNext()) != null){
				rowHandler.parseRow(row, rowIdx);
				if(++rowIdx == limintCount){
					break;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			reader.close();
		}
		
	}
	
	
}
