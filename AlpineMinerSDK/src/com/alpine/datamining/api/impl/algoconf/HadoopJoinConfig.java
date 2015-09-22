/**
 * ClassName HadoopJoinConfig.java
 *
 * Version information:1.00
 *
 * Date:July 6, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;

/**
 * @author Jeff Dong
 *
 */
public class HadoopJoinConfig extends HadoopDataOperationConfig {

	private AnalysisHadoopJoinModel joinModel;
	
	public static final String[] JOIN_TYPE = new String[]{"JOIN","LEFT OUTER","RIGHT OUTER","FULL OUTER"};

	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
	public HadoopJoinConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
	 }
	
	public HadoopJoinConfig() {
		super();
		setParameterNames(parameters);
	}

	public AnalysisHadoopJoinModel getJoinModel() {
		return joinModel;
	}

	public void setJoinModel(AnalysisHadoopJoinModel joinModel) {
		this.joinModel = joinModel;
	}
}
