/**
 * ClassName AbstractVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;


/**
 * @author John Zhao
 *
 */
public class AbstractVisualizationOutPut implements VisualizationOutPut {

	private DataAnalyzer analyzer;
	private VisualizationType visualizationType;
	private String description;
	private String name;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationOutPut#getAnalyzer()
	 */
	@Override
	public DataAnalyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(DataAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	public void setVisualizationType(VisualizationType visualizationType) {
		this.visualizationType = visualizationType;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationOutPut#getVisulizationType()
	 */
	@Override
	public VisualizationType getVisualizationType() {
		return visualizationType;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description;
	}

	@Override
	public String getName() {
		 
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public Object getVisualizationObject() {
		// TODO Auto-generated method stub
		return null;
	}


}
