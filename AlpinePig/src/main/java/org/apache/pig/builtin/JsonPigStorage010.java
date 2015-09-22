/**
 * ClassName JsonPigStorage010.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-16
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;

import com.alpine.hadoop.ext.JSONInputFormat;
import com.alpine.hadoop.ext.JSONRecordParser;

/**
 * 
 * @author Jeff Dong
 *
 */

public class JsonPigStorage010  extends AbstractMultipleLinePigStorage010 {
 
	private String containerTag;
	private String containerJsonPath;
	private List<String> jsonPathList;
	private String jsonDataStructureType;
 
    public JsonPigStorage010(String containerTag, String jsonPathListString,
    		String jsonDataStructureType , String typeString,
    		String containerJsonPath,String callBackURL,String uuid,  String operatorName) { //xpathListString is separatered with ,
        super(new JSONRecordParser(Arrays.asList(jsonPathListString.split(",")),jsonDataStructureType,containerJsonPath),  typeString,callBackURL, uuid,  operatorName); 

    	this.containerTag = containerTag;
        this.jsonPathList = Arrays.asList(jsonPathListString.split(",")) ;
        this.jsonDataStructureType=jsonDataStructureType;
        this.containerJsonPath=containerJsonPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonPigStorage010)
            return equals((JsonPigStorage010)obj);
        else
            return false;
    }

    public boolean equals(JsonPigStorage010 other) {
        return  containerTag.equals(other.containerTag)
        		&&containerJsonPath.equals(other.containerJsonPath)&&
        		PigUtil.equalsFocusOrder(jsonPathList,other.jsonPathList)
        		&&jsonDataStructureType.equals(other.jsonDataStructureType);
    }

    
    @Override
    public InputFormat getInputFormat() {
    	return new JSONInputFormat(containerTag,jsonDataStructureType,containerJsonPath);//pase the tag  
        
    }


    @Override
    public int hashCode() {
        return   containerTag.hashCode();
    }

   
}


 