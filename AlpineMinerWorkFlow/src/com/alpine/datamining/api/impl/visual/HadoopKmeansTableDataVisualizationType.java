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

import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.miner.view.ui.dataset.TableEntity;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansTableDataVisualizationType extends
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
		List<String> columnNames=clusterModel.getColumnNames();
		List<String> columnTypes=clusterModel.getColumnTypes();
		for(int i=0;i<columnNames.size();i++){
			tableEntity.addSortColumn(columnNames.get(i), columnTypes.get(i));
		}
		clusterModel.getColumnTypes();
		
		if(clusterModel!=null&&clusterModel.getDataSampleContents()!=null){
			List<String[]> dataSampleContents = clusterModel.getDataSampleContents();
			int index =0;
			for(String[] tableItem:dataSampleContents){
				if(index==0){
					tableEntity.setColumn(tableItem);
				}else{
					tableEntity.addItem(tableItem);
				}
				index++;
			}
		}
			
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
}
