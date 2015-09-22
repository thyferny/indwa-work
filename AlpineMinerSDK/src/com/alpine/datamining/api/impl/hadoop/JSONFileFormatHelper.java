/**
 * ClassName JSONFileFormatHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-1
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.ext.JSONInputFormat;
import com.alpine.hadoop.ext.RecordParserFactory;

/**
 * @author Jeff Dong
 *
 */
public class JSONFileFormatHelper implements FileFormatHelper {

	private AnalysisJSONFileStructureModel fileStructureModel;
	private HadoopAnalyticSource hadoopSource;

	/**
	 * @param fileStructureModel
	 * @param hadoopSource 
	 */
	public JSONFileFormatHelper(AnalysisJSONFileStructureModel fileStructureModel, HadoopAnalyticSource hadoopSource) { 
		this.fileStructureModel = fileStructureModel ;
		this.hadoopSource=hadoopSource;
	}
	@Override
	public void initHadoopConfig(Configuration hadoopConf)
			throws AnalysisException {
		hadoopConf.set(AlpineHadoopConfKeySet.INPUT_FORMAT_KEY, RecordParserFactory.INPUT_FORMAT_VALUE_JSON) ;
		hadoopConf.set(AlpineHadoopConfKeySet.JSON_START_TAG_KEY, fileStructureModel.getContainer());
		hadoopConf.set(AlpineHadoopConfKeySet.JSON_TYPE_TAG_KEY, fileStructureModel.getJsonDataStructureType());
		hadoopConf.set(AlpineHadoopConfKeySet.JSON_CONTAINER_PATH_TYPE_TAG_KEY, fileStructureModel.getContainerJsonPath());
		List<String> jsonPathList = fileStructureModel.getJsonPathList(); 
		hadoopConf.set(AlpineHadoopConfKeySet.JSON_PATH_TAG_KEY, toJsonPathListString(jsonPathList)) ;
	}

	@Override
	public void setInputFormatClass(Job job) {
		job.setInputFormatClass(JSONInputFormat.class) ;
	}

	private String toJsonPathListString(List<String> jsonPathList) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> iterator = jsonPathList.iterator(); iterator.hasNext();) {
			String jsonPath = iterator.next();
			sb.append(jsonPath).append(AlpineHadoopConfKeySet.VALUE_XPATH_DELIMITER);
		}
		if(sb.length()>0){
			sb = sb.deleteCharAt(sb.length()-1); 
		}
		return sb.toString();
	}
}
