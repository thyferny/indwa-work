/**
 * ClassName HistogramAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;



/**
 * Eason
 */
public class HistogramAnalysisConfig extends AbstractAnalyticConfig{
	
 
	private static final List<String> parameterNames = new ArrayList<String>();

	private AnalysisColumnBinsModel columnBinModel;
 

	public AnalysisColumnBinsModel getColumnBinModel() {
		return columnBinModel;
	}

	public void setColumnBinModel(AnalysisColumnBinsModel columnBinModel) {
		this.columnBinModel = columnBinModel;
	}

	public HistogramAnalysisConfig(){
		 setParameterNames(parameterNames);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.HistogramShapeVisualizationType"+","
				+"com.alpine.datamining.api.impl.visual.HistogramImageVisualizationType");
	}

}
