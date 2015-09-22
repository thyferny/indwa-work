/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pig.builtin;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;

import com.alpine.hadoop.ext.JSONInputFormat;
import com.alpine.hadoop.ext.JSONRecordParser;

 

/**
 * A load function that parses a line of input into fields using a delimiter to
 * set the fields. The delimiter is given as a regular expression. See
 * {@link java.lang.String#split(String)} and {@link java.util.regex.Pattern}
 * for more information.
 */
public class JsonPigStorage081 extends AbstractMultipleLinePigStorage081 {
	
	private String containerTag;
	private List<String> jsonPathList;
	private String jsonDataStructureType;
	private String containerJsonPath;
 
    public JsonPigStorage081(String containerTag, String jsonPathListString,
    		String jsonDataStructureType,String typeString,String containerJsonPath,String callBackURL,String uuid, String  operatorName) { //xpathListString is separatered with ,
		super(new JSONRecordParser(Arrays.asList(jsonPathListString.split(",")),jsonDataStructureType,containerJsonPath),
				typeString,callBackURL,uuid,  operatorName);

        this.containerTag = containerTag;
        this.jsonPathList = Arrays.asList(jsonPathListString.split(",")) ;
        this.jsonDataStructureType=jsonDataStructureType;
        this.containerJsonPath=containerJsonPath;
    }
     
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonPigStorage081)
            return equals((JsonPigStorage081)obj);
        else
            return false;
    }

    public boolean equals(JsonPigStorage081 other) {
        return  containerTag.equals(other.containerTag)
        		&&containerJsonPath.equals(other.containerJsonPath)
        		&&PigUtil.equalsFocusOrder(jsonPathList,other.jsonPathList)
        		&&jsonDataStructureType.equals(other.jsonDataStructureType);
    }
 
    
    @Override
    public InputFormat getInputFormat() {
    	return new JSONInputFormat(containerTag,jsonDataStructureType,containerJsonPath);//pase the tag  

    }

      

    @Override
    public int hashCode() {
    	 return  containerTag.hashCode();
    }
 

  
}
