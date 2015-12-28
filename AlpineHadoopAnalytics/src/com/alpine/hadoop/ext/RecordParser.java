
package com.alpine.hadoop.ext;

import java.util.List;


public interface RecordParser {
	//@Deprecated
	//public String[] parse(String aRecordContent) throws Exception;
	
	//Since we have to support the nested XML and JSON, we have to get a list here
	public List<String[]> parse(String aRecordContent) throws Exception;

	//this is only for the LOG and CSV since it will be faster... avoid generate a list with size =1
	public String[] parseLine(String string) throws Exception;

}
