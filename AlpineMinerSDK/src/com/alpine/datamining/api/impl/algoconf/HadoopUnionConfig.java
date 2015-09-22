/**
 * ClassName HadoopUnionConfig.java
 *
 * Version information:1.00
 *
 * Date:July 6, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUnionConfig extends HadoopDataOperationConfig {

	private AnalysisHadoopUnionModel unionModel;

	private HashMap<String, List<String>> inputColumnMap;

	//filename -> filestructure
	private LinkedHashMap<String,  AnalysisFileStructureModel> fileStructureModelList;
	
	public HashMap<String, List<String>> getInputColumnMap() {
		return inputColumnMap;
	}

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
	
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
	public HadoopUnionConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
	 }
	
	public HadoopUnionConfig() {
		super();
		setParameterNames(parameters);
	}

	public AnalysisHadoopUnionModel getUnionModel() {
		return unionModel;
	}

	public void setUnionModel(AnalysisHadoopUnionModel unionModel) {
		this.unionModel = unionModel;
	}

	public void setInputColumnMap(HashMap<String, List<String>> inputColumnMap) { 
		this.inputColumnMap =inputColumnMap;
		
	}

	public void setInputFileStructureModelList(
			LinkedHashMap<String,  AnalysisFileStructureModel> fileStructureModelList) {
		this.fileStructureModelList = fileStructureModelList;
		
	}

	public LinkedHashMap<String,  AnalysisFileStructureModel>  getFileStructureModelList() {
		return fileStructureModelList;
	}
}
