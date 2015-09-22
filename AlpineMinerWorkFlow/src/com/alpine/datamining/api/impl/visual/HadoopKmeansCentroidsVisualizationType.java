/**
 * ClassName HadoopKmeansTableDataVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansCentroidsVisualizationType extends
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
		TableEntity tableEntity = new TableEntity();
		
		if(clusterModel!=null&&clusterModel.getColumnNames()!=null){
			List<String> columnNames = clusterModel.getColumnNames();
			String[] titles=new String[columnNames.size()+1];
			titles[0]="Cluster";
			tableEntity.addSortColumn(titles[0],DataTypeConverterUtil.numberType);
			for(int i=0;i<columnNames.size();i++){
				titles[i+1]=columnNames.get(i);
				tableEntity.addSortColumn(columnNames.get(i),DataTypeConverterUtil.numberType);
			}
			tableEntity.setColumn(titles);
		}
		if(clusterModel!=null&&clusterModel.getCentroidsContents()!=null&&clusterModel.getColumnNames()!=null){
			Map<String, Map<String, Double>> centroidsContents = clusterModel.getCentroidsContents();
			List<String> columnNames = clusterModel.getColumnNames();
			Iterator<Entry<String, Map<String, Double>>> iter = centroidsContents.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Map<String, Double>> entry = iter.next();
				String clusterNum = entry.getKey();
				Map<String, Double> centroid = entry.getValue();
				String[] tableItem=new String[columnNames.size()+1];
				tableItem[0]=clusterNum;
				for(int i=0;i<columnNames.size();i++){
					tableItem[i+1]=String.valueOf(centroid.get(columnNames.get(i)));
				}
				tableEntity.addItem(tableItem);
			}
		}
			
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.CENTER_POINT,locale));
		return output;
	}
}
