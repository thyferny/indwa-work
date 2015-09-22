/**
 * ClassName XMLPigStorage081.java
 *
 * Version information: 1.00
 *
 * Date: 2012-11-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package org.apache.pig.builtin;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;

import com.alpine.hadoop.ext.XMLInputFormat;
import com.alpine.hadoop.ext.XMLRecordParser;

 

/**
 * A load function that parses a line of input into fields using a delimiter to
 * set the fields. The delimiter is given as a regular expression. See
 * {@link java.lang.String#split(String)} and {@link java.util.regex.Pattern}
 * for more information.
 */
public class XMLPigStorage081 extends AbstractMultipleLinePigStorage081 {
 
 
	private String containerTag;
	private String attrMode;
	private List<String> xpathList;
	private String jsonDataStructureType = null;
	private String containerJsonPath = null;
 
    public XMLPigStorage081(String containerTag, String xpathListString ,
    		String attrMode,String typeString,
    		String jsonDataStructureType,
			String containerJsonPath,String callBackURL,String uuid, String operatorName
			) { //xpathListString is separatered with ,
		super(new XMLRecordParser(Arrays.asList(xpathListString.split(",")),jsonDataStructureType,containerJsonPath),typeString,callBackURL,uuid,  operatorName);
    	this.attrMode=attrMode;
        this.containerTag = containerTag;
        this.xpathList = Arrays.asList(xpathListString.split(",")) ;
		this.jsonDataStructureType=jsonDataStructureType;
		this.containerJsonPath=containerJsonPath;
    }
    
     
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XMLPigStorage081)
            return equals((XMLPigStorage081)obj);
        else
            return false;
    }

    public boolean equals(XMLPigStorage081 other) {
		return containerTag.equals(other.containerTag)&&
				jsonDataStructureType.equals(other.jsonDataStructureType)&&
				containerJsonPath.equals(other.containerJsonPath)
				&& PigUtil.equalsFocusOrder(xpathList, other.xpathList);
    }

    
    
    @Override
    public InputFormat getInputFormat() {
    	return new XMLInputFormat(containerTag,attrMode,jsonDataStructureType,containerJsonPath);//pase the tag  

    }
   

    @Override
    public int hashCode() {
    	 return  containerTag.hashCode();
    }

    
}
