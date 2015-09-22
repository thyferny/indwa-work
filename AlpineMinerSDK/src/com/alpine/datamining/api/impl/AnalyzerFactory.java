/**
 * ClassName AnalyzerFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import com.alpine.datamining.api.DataAnalyzer;
import org.apache.log4j.Logger;
/**
 * @author John Zhao
 *
 */
public class AnalyzerFactory {
	//analyzerClass is full name...
    private static final Logger itsLogger = Logger.getLogger(AnalyzerFactory.class);
    public static DataAnalyzer getAnalyzer(String analyzerClass){
		try {
			Class cls=Class.forName(analyzerClass);
			return (DataAnalyzer) cls.newInstance();
		} catch ( Exception e) {
			e.printStackTrace();
			itsLogger.error("Create Analyzer Error:"+e.getLocalizedMessage());
			
		}
		return null;
		//zy: will use it later...
		
	}

}
