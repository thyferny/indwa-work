/**
 * ClassName HadoopKmeansWarningVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansWarningVisualizationType extends
		TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		ClusterOutputModel clusterModel= null;
		
		if(analyzerOutPut instanceof HadoopKmeansOutput){
			obj = ((HadoopKmeansOutput)analyzerOutPut).getClusterModel();
			if(obj instanceof ClusterOutputModel){
				clusterModel = (ClusterOutputModel)obj;
			}
		}
		if(!clusterModel.isStable()){
			TextVisualizationOutPut output = new TextVisualizationOutPut(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.KMEANS_NOTSTABLE, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.WARING_MESSAGE_TITLE,locale));
			return output;
		}
		return null;
	}
}
