/**
 * ClassName VariableModelUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-10
 *
 * COPYRIGHT   2011  Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;

import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;

public class VariableModelUtility {

	public static String VARIABLE_PREFIX="@";
	public static String VARIABLE_ESCAPRE="\\\\";
	public static String VARIABLE_ESCAPRE_SUFFIX=";";
	public static String DEFAULT_SCHEMA=VARIABLE_PREFIX+"default_schema";
	public static String DEFAULT_PREFIX=VARIABLE_PREFIX+"default_prefix";
	public static String DEFAULT_TMPDIR=VARIABLE_PREFIX+"default_tempdir";
	public static String DEFAULT_SCHEMA_DEFAULTVALUE="public";
	public static String DEFAULT_PREFIX_DEFAULTVALUE="alp";
	public static String DEFAULT_TMPDIR_DEFAULTVALUE="/tmp";
	public static String Hadoop_Reduce_Number="mapred.reduce.tasks";
	public static String Hadoop_Util_Reduce_Number_Seted="alp.hadoop.reduce.tasks.seted";
	
	//for hadoop variable
	public static String HADOOP_PARAMETER_PREFIX=VARIABLE_PREFIX+"alpine.hadoop";
	public static String HADOOP_MAPRED_PARAMETER_PREFIX=VARIABLE_PREFIX+"alpine.mapred";//for each map-reduce
	
	public static String getReplaceValue(Map<String,String> variableMap,
			String paramValue) {
		if(variableMap!=null){
			if(StringUtil.isEmpty(paramValue)||!paramValue.contains(VARIABLE_PREFIX)){
				return paramValue;
			}
	
			String[] variableNameArray = generateMaxMatch(variableMap);
			
			//Note:Escape
			//First:replace all "\@" to "\@;"(Variable naming restriction:only number,word and "_" acceptable)
			paramValue=paramValue.replaceAll(
					VARIABLE_ESCAPRE+VARIABLE_PREFIX, 
					VARIABLE_ESCAPRE+VARIABLE_PREFIX+VARIABLE_ESCAPRE_SUFFIX);
			
			for(String variableName:variableNameArray){
				if(!paramValue.contains(variableName)){
					continue;
				}
				
				String variableValue=variableMap.get(variableName);
				//Then: repalce all variableName to variableValue(@default_schema to public)
				paramValue=paramValue.replaceAll(variableName, variableValue);
			}
			//Last: replace all "\@;" to "@"
			paramValue=paramValue.replaceAll(
					VARIABLE_ESCAPRE+VARIABLE_PREFIX+VARIABLE_ESCAPRE_SUFFIX, 
					VARIABLE_PREFIX);
		}
		return paramValue;
	}

	public static String replaceHadoopTmpPath(Map<String, String> variableMap){
		String tmpPath = VariableModelUtility.DEFAULT_TMPDIR_DEFAULTVALUE;
		if(variableMap!=null){
			if(variableMap.containsKey(VariableModelUtility.DEFAULT_TMPDIR)){
				tmpPath = variableMap.get(VariableModelUtility.DEFAULT_TMPDIR);
			}
		}
		if(tmpPath.endsWith(HadoopFile.SEPARATOR)==false){
			tmpPath=tmpPath+HadoopFile.SEPARATOR;
		}
		return tmpPath;
	}
			
	public static void replaceHadoopMapReduceVariable(
			Map<String, String> variableMap,
			Configuration conf,
			String operatorName,
			String jobName){
		if(variableMap==null) return;
		Iterator<Entry<String, String>> iter = variableMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith(HADOOP_MAPRED_PARAMETER_PREFIX)){
				//key like:"@hadoop.mapred.kmeans1.kmeansInit.reduce.number"
				String subStr = key.substring(key.indexOf(HADOOP_MAPRED_PARAMETER_PREFIX)+
						HADOOP_MAPRED_PARAMETER_PREFIX.length()+1, key.length());
				//subStr="kmeans1.kmeansInit.reduce.number"
				if(subStr.startsWith(operatorName)){
					subStr = subStr.substring(subStr.indexOf(operatorName)+
							operatorName.length()+1, subStr.length());
					//subStr="kmeansInit.reduce.number"
					if(subStr.startsWith(jobName)){
						subStr = subStr.substring(subStr.indexOf(jobName)+
								jobName.length()+1, subStr.length());
						//subStr="reduce.number"
						if(StringUtil.isEmpty(subStr)==false
								&&subStr.startsWith("alpine")==false){
							if(subStr.equals(Hadoop_Reduce_Number))
							{
								conf.set(Hadoop_Util_Reduce_Number_Seted, Resources.TrueOpt);
							}
							conf.set(subStr, value);
						}
					}
				}
			}
		}
	}
	
	public static boolean replaceHadoopVariable(
			Map<String, String> variableMap,
			Configuration conf){
		boolean isReplaced=false;
		if(variableMap==null) return isReplaced;
		Iterator<Entry<String, String>> iter = variableMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith(HADOOP_PARAMETER_PREFIX)){
				//key like:"@alpine.hadoop.ipc.client.connect.max.retries"
				String subStr = key.substring(key.indexOf(HADOOP_PARAMETER_PREFIX)+
						HADOOP_PARAMETER_PREFIX.length()+1, key.length());
				if(StringUtil.isEmpty(subStr)==false
						&&subStr.startsWith("alpine")==false){
					conf.set(subStr, value);
					isReplaced=true;
				}
			}
		}
		return isReplaced;
	}
	
	private static String[] generateMaxMatch(Map<String,String> variableMap) {
		List<String> variableNameList=new ArrayList<String>();
		Iterator<Entry<String, String>> iter = variableMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String variableName = entry.getKey();
			variableNameList.add(variableName);
		}
		String[] variableNameArray = variableNameList.toArray(new String[variableNameList.size()]);
		Comparator<String> stringLengthComparator=new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				if(arg0.length()>arg1.length()){
					return -1;
				}else if(arg0.length()<arg1.length()){
					return 1;
				}else{
					return arg0.compareTo(arg1);
				}
			}
		};
		Arrays.sort(variableNameArray,stringLengthComparator);
		return variableNameArray;
	}
	
}
