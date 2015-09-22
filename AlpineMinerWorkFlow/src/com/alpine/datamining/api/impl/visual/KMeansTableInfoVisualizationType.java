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
import java.util.ArrayList;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.tools.AlpineMath;

public class KMeansTableInfoVisualizationType extends TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		ClusterModel clusterModel= null;
		
		TableEntity tableEntity = new TableEntity();
		tableEntity.setSystem((analyzerOutPut).getAnalyticNode().getSource().getDataSourceType());
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof ClusterModel){
				clusterModel = (ClusterModel)obj;
//				tableName = clusterModel.getResultTableName();
			}
		}
		
		if(clusterModel == null)return null;
		
		ArrayList<ArrayList<String>> list = clusterModel.getCenterPoint();
		if(list != null && list.size()>0){
			
			for(int i=0;i<list.size();i++){
				if(i==0){
					String[] columns = new String[list.get(i).size()];
					for(int j=0;j<list.get(i).size();j++){
						columns[j] = list.get(i).get(j);
						tableEntity.addSortColumn(columns[j],DataTypeConverterUtil.numberType);
					}
					tableEntity.setColumn(columns);
					
				}else{
					String[] item = new String[list.get(i).size()];
					for(int j=0;j<list.get(i).size();j++){
						item[j] = AlpineMath.doubleExpression(Double.parseDouble(list.get(i).get(j)));
					}
					tableEntity.addItem(item);
				}
			}
			
			DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.CENTER_POINT,locale));
 
			return output;
		}
		
		return null;
	}
}
