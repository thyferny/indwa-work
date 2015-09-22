/**
 * ClassName KMeansTextVisualizationType.java
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

public class KMeansTextVisualizationType extends TextVisualizationType {

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
		
		StringBuffer sb = new StringBuffer();
		if (clusterModel!=null) {
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_COLUMN_NAME,locale)
					+" : "+clusterModel.getClusterColumn());
			sb.append("\n");
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_COUNT,locale)
					+" : "+clusterModel.getNumberOfClusters());
			sb.append("\n");
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.AVG_MEASUREMENT,locale)
					+" : "+clusterModel.getMeasureAvg());
		}
		TextVisualizationOutPut output = new TextVisualizationOutPut(sb.toString());
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
		return output;
	}
}
