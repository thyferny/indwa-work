/**
 * ClassName HadoopKmeansTextVisualizationType.java
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
import com.alpine.datamining.operator.hadoop.output.ClusterOutputBasicInfo;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansTextVisualizationType extends TextVisualizationType {

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
		
		StringBuffer sb = new StringBuffer();
		if (clusterModel!=null&&clusterModel.getOutputText()!=null) {
			ClusterOutputBasicInfo outputText = clusterModel.getOutputText();
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_COLUMN_NAME,locale)
					+" : "+outputText.getClusterColumName());
			sb.append("\n");
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_COUNT,locale)
					+" : "+outputText.getClusterCount());
			sb.append("\n");
			sb.append(VisualLanguagePack.getMessage(VisualLanguagePack.AVG_MEASUREMENT,locale)
					+" : "+outputText.getAvgDistanceMeasurement());
		}
		TextVisualizationOutPut output = new TextVisualizationOutPut(sb.toString());
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
		return output;
	}
}
