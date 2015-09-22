/**
 * ClassName AdaBoostAnalyzerOutPutTrainModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.operator.Model;

public class AdaBoostAnalyzerOutPutTrainModel extends AnalyzerOutPutTrainModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6127597785604766932L;

	
	private Map<String,DataAnalyzer> dataAnalyzerMap=new HashMap<String,DataAnalyzer>();
	
	public AdaBoostAnalyzerOutPutTrainModel(Model model) {
		super(model);
	}
	
	public void addAnalyzer(String name,DataAnalyzer dataAnalyzer){
		dataAnalyzerMap.put(name, dataAnalyzer);
	}
	
	public DataAnalyzer getDataAnalyzer(String name){
		return dataAnalyzerMap.get(name);
	}

	public Map<String, DataAnalyzer> getDataAnalyzerMap() {
		return dataAnalyzerMap;
	}

	public void setDataAnalyzerMap(Map<String, DataAnalyzer> dataAnalyzerMap) {
		this.dataAnalyzerMap = dataAnalyzerMap;
	}
	
}
