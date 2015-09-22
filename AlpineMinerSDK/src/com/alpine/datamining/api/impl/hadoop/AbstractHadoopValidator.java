/**
 * ClassName AbstractHadoopValidator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-11
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.pig.PigServer;

import com.alpine.datamining.api.impl.AbstractAnalyzer;
/** 
 * @author Peter
 *
 */
public abstract class AbstractHadoopValidator extends AbstractAnalyzer{
	
	private static Logger itsLogger= Logger.getLogger(AbstractHadoopValidator.class);
	
	
	//this is used for temp file for pig...
	public static final String OUT_PREFIX="FILE_" ;
	
	public String getOutputTempName() {
		if(null==getUUID()){
			setUUID(UUID.randomUUID().toString().replace("-", ""));
		}
		return  OUT_PREFIX+getUUID().replace(".", ""); 
	}
	
	protected void runPigScript(PigServer pigServer, String pigScript)
			throws IOException {
		String[] pigLines=pigScript.split("\n");

		for(String line:pigLines){
		    pigServer.registerQuery(line);
		    if(itsLogger.isDebugEnabled()){
		        itsLogger.debug("Registered the query of["+line+"]");
		    }
		}
	}
}