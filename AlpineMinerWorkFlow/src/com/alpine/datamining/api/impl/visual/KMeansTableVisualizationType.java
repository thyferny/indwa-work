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
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.miner.view.ui.dataset.TableEntity;

public class KMeansTableVisualizationType extends TableVisualizationType {

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
		TableEntity table = new TableEntity();
		List<String[]> list = clusterModel.getClustersArrays();
		if(list == null || list.size()==0){return null;}
		
			int n = list.get(0).length-3;
			String[] columns = new String[3+n];
			columns[0] = VisualLanguagePack.getMessage(VisualLanguagePack.VARIABLES,locale);
			columns[1] = VisualLanguagePack.getMessage(VisualLanguagePack.STATES,locale);
			for(int i=0;i<list.get(0).length;i++){
				if(i==2){
					columns[i] = VisualLanguagePack.getMessage(VisualLanguagePack.POPULATION,locale)+": "+list.get(0)[i].split(";")[0];
				}
				if(i>2){
					columns[i] = VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER,locale)+list.get(0)[i].split(";")[0];
				}
			}
			table.setColumn(columns);
			
			String columnCategory = columns[1];
			table.setColumnColorCategory(columnCategory);
			
			String[] columnScale = new String[columns.length-2];
			for(int i=0;i<columnScale.length;i++){
				columnScale[i] = columns[i+2];
			}
			table.setColumnColorScale(columnScale);
		
			for(String[] temp:list){
				String[] items = new String[columns.length];
				for(int i=0;i<temp.length;i++){
					if(i<2){
						items[i] = temp[i];
					}else{
						items[i]= temp[i].split(";")[1];
					}
				}
				table.addItem(items);
			}
			
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(table);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_PROFILES,locale));
		return output;
	}
}
