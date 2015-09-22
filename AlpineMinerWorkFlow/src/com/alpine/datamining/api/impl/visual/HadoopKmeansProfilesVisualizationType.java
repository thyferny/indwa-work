/**
 * ClassName HadoopKmeansProfilesVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputBasicInfo;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputProfiles;
import com.alpine.datamining.operator.hadoop.output.ClusterRangeInfo;
import com.alpine.datamining.operator.hadoop.output.KmeansValueRange;
import com.alpine.miner.view.ui.dataset.TableEntity;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansProfilesVisualizationType extends
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
	TableEntity table = new TableEntity();
	ClusterOutputProfiles outputProfiles = clusterModel.getOutputProfiles();
	ClusterOutputBasicInfo basicInfo = clusterModel.getOutputText();
	if(outputProfiles!=null&&basicInfo!=null){
		int clusterCount = basicInfo.getClusterCount();
		String[] tableColumn=new String[3+clusterCount];
		table.setColumn(tableColumn);
		
		tableColumn[0] = VisualLanguagePack.getMessage(VisualLanguagePack.VARIABLES,locale);
		tableColumn[1] = VisualLanguagePack.getMessage(VisualLanguagePack.STATES,locale);
		
		String columnCategory = tableColumn[1];
		table.setColumnColorCategory(columnCategory);
		
		long totalRowCounts = outputProfiles.getTotalRowCounts();
		tableColumn[2] = VisualLanguagePack.getMessage(VisualLanguagePack.POPULATION,locale)+": "+totalRowCounts;
		
		List<ClusterRangeInfo> clusterRangeInfos = outputProfiles.getClusterRangeInfo();
		if(clusterRangeInfos!=null){
			for(int i=0;i<clusterRangeInfos.size();i++){
				tableColumn[i+3]=VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER,locale)+clusterRangeInfos.get(i).getClusterName()+",Size: "
						+clusterRangeInfos.get(i).getClusterRowCounts();
			}
		}
		
		String[] columnScale = new String[1+clusterCount];
		for(int i=0;i<columnScale.length;i++){
			columnScale[i] = tableColumn[i+2];
		}
		table.setColumnColorScale(columnScale);
		
		List<String> columnNames = clusterModel.getColumnNames();
		Map<String, List<KmeansValueRange>> columnRangeMap = outputProfiles.getColumnRangeMap();
		if(columnNames!=null&&columnRangeMap!=null&&clusterRangeInfos!=null){
			for(String columnName:columnNames){
				String[] tableItem=new String[3+clusterCount];
				tableItem[0]=columnName;
				List<KmeansValueRange> columnRangeList = columnRangeMap.get(columnName);
				if(columnRangeList!=null){
					StringBuffer sb=new StringBuffer();
					for(KmeansValueRange valueRange:columnRangeList){
						double minValue = valueRange.getMinValue();
						double maxValue = valueRange.getMaxValue();
						sb.append(minValue).append(" - ").append(maxValue).append(",");
					}
					sb=sb.deleteCharAt(sb.length()-1);
					tableItem[1]=sb.toString();
				}
				Map<Integer,Long> sumMap=new HashMap<Integer,Long>();
				for(int i=0;i<clusterRangeInfos.size();i++){
					StringBuffer sb=new StringBuffer();
					Map<String, List<Long>> columnRangeRowCountMap = clusterRangeInfos.get(i).getColumnRangeRowCountMap();
					List<Long> columnRangeRowCountList = columnRangeRowCountMap.get(columnName);
					int index=0;
					for(Long l:columnRangeRowCountList){
						sb.append(l).append(",");
						if(sumMap.containsKey(index)){
							Long oldSum = sumMap.get(index);
							Long newSum=oldSum+l;
							sumMap.put(index, newSum);
						}else{
							sumMap.put(index, l);
						}
						index++;
					}
					sb=sb.deleteCharAt(sb.length()-1);
					tableItem[3+i]=sb.toString();
				}
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<sumMap.size();i++){
					sb.append(sumMap.get(i)).append(",");
				}
				sb=sb.deleteCharAt(sb.length()-1);
				tableItem[2]=sb.toString();
				
				table.addItem(tableItem);
			}
		}
	}

	DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(table);
	output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER_PROFILES,locale));
	return output;
	
	}
}
