/**
 * ClassName LogPigStorage081.java
 *
 * Version information: 1.00
 *
 * Date: 2012-7-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigTextInputFormat;

import com.alpine.hadoop.ext.LogRecordParser;

/**
 * 
 * @author nhosgur
 *
 */

public class LogPigStorage081  extends AbstractSingleLinePigStorage081 {
	
	private String logType ="";
	private String conversionPattern = "" ;

	public LogPigStorage081(String conversionPattern,String logType,String typeString,String callBackURL,String uuid, String  operatorName) {
		super("",typeString,callBackURL,uuid,  operatorName,new LogRecordParser( conversionPattern, logType)); //currently log has no header

		this.logType=logType;
		this.conversionPattern = conversionPattern;

	}
 

	//[TODO] verify it works
    @Override
    public boolean equals(Object obj) {
    	if(null==obj){
    		return false;
    	}
        if (obj instanceof LogPigStorage081)
            return equals((LogPigStorage081)obj);
        else
            return false;
    }

    public boolean equals(LogPigStorage081 other) {
    	if(null==other){
    		return false;
    	}
        return conversionPattern.equals(other.conversionPattern);
    }

     
    @Override
    public InputFormat getInputFormat() {
    	 return new PigTextInputFormat(); 
    }

 
    @Override
    public int hashCode() {
        return logType.hashCode()+conversionPattern.hashCode();
    }
  
    
}


 