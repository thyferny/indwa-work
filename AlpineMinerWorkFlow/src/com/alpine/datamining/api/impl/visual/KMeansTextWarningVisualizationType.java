/**
 * ClassName KMeansTableVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.kmeans.ClusterModel;

public class KMeansTextWarningVisualizationType extends TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		ClusterModel clusterModel= null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof ClusterModel){
				clusterModel = (ClusterModel)obj;
			}
		}
		if(!clusterModel.getIsStable()){
			TextVisualizationOutPut output = new TextVisualizationOutPut(clusterModel.getStableInformation());
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.WARING_MESSAGE_TITLE,locale));
			return output;
		}
		
		return null;
	}
}
