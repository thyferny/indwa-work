/**
 * ClassName XMLPigStorage010.java
 *
 * Version information: 1.00
 *
 * Date: 2012-7-10
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
 * 
 * @author john zhao
 * 
 */

public class XMLPigStorage010 extends AbstractMultipleLinePigStorage010 {

	private String containerTag;
	private List<String> xpathList;
	private String attrMode;
	private String jsonDataStructureType = null;
	private String containerJsonPath = null;

	public XMLPigStorage010(String containerTag, String xpathListString,
			String attrMode,String typeString,
			String jsonDataStructureType,
			String containerJsonPath,String callBackURL,  String uuid,  String operatorName) { // xpathListString is separatered with ,
		super(new XMLRecordParser(Arrays.asList(xpathListString.split(",")),jsonDataStructureType,containerJsonPath),typeString,callBackURL,uuid,  operatorName);

		this.containerTag = containerTag;
		this.attrMode = attrMode;
		this.xpathList = Arrays.asList(xpathListString.split(","));
		this.jsonDataStructureType=jsonDataStructureType;
		this.containerJsonPath=containerJsonPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XMLPigStorage010)
			return equals((XMLPigStorage010) obj);
		else
			return false;
	}

	public boolean equals(XMLPigStorage010 other) {
		return containerTag.equals(other.containerTag)&&
				jsonDataStructureType.equals(other.jsonDataStructureType)&&
				containerJsonPath.equals(other.containerJsonPath)
				&& PigUtil.equalsFocusOrder(xpathList, other.xpathList);
	}

	@Override
	public InputFormat getInputFormat() {
		return new XMLInputFormat(containerTag, attrMode,jsonDataStructureType,containerJsonPath);// pase the tag
	}

	@Override
	public int hashCode() {
		return containerTag.hashCode();
	}

}
