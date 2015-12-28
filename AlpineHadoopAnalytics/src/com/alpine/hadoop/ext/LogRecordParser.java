
package com.alpine.hadoop.ext;

import java.util.ArrayList;
import java.util.List;

import com.alpine.logparser.IAlpineLogParser;
 
 
public class LogRecordParser extends AbstractRecordParser{
	private IAlpineLogParser parser; 
	public LogRecordParser(String logFormat,String logType){
		if(null==logFormat||null==logType){
			throw new IllegalArgumentException("Neither log type nor log format can be null");
		}
		this.parser=LogParserFactory.createALogParser(logFormat,logType);
	}
	@Override
	public List<String[]> parse(String line) throws Exception {
		List<String[]> theList=new ArrayList<String[]>(1);
		theList.add(parser.processTheLine(line));
		return theList;
	}
	@Override
	public String[] parseLine(String line) throws Exception {
		return parser.processTheLine(line);
	}
}
