/**
 * ClassName FrequencyTableVisualizationType.java
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.data.category.DefaultCategoryDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.attributeanalysisresult.FrequencyAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.miner.view.ui.dataset.DropDownTableEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.hadoop.HadoopDataType;

public class FrequencyTableVisualizationType extends TableVisualizationType {

	private static final String N_A = "N/A";
	private HashMap<String,TableEntity> tableEntityMap;
	private static DropDownTableEntity entity;
	


	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		
//		CompositeVisualizationOutPut composite = new CompositeVisualizationOutPut();
		
		String system =(analyzerOutPut).getAnalyticNode().getSource().getDataSourceType();
		Object obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
		List<ValueFrequencyAnalysisResult> list = null;
		if(obj instanceof FrequencyAnalysisResult){
			list = ((FrequencyAnalysisResult)obj).getFrequencyAnalysisResult();
		}
		
		tableEntityMap = new HashMap<String,TableEntity>();
		
		HashMap<String,List<ValueFrequencyAnalysisResult>> resultMap=new HashMap<String,List<ValueFrequencyAnalysisResult>>();
		for (ValueFrequencyAnalysisResult freqAnalysisResult: list){
			if(!resultMap.containsKey(freqAnalysisResult.getColumnName())){
				List<ValueFrequencyAnalysisResult> resultList=new ArrayList<ValueFrequencyAnalysisResult>();
				resultList.add(freqAnalysisResult);
				resultMap.put(freqAnalysisResult.getColumnName(), resultList);
			}else{
				resultMap.get(freqAnalysisResult.getColumnName()).add(freqAnalysisResult);
			}
		}
		
		if (list!=null) {
			String[] columns = new String[] {
					VisualLanguagePack.getMessage(VisualLanguagePack.COLUMN_NAME,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.COUNT,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.PERCENTAGE,locale)
			};
			AnalyticSource analyticSource = analyzerOutPut.getAnalyticNode().getSource();
			String[] columnType = null;
			
			if(analyticSource instanceof DataBaseAnalyticSource){
				columnType = new String[]{
						DataTypeConverterUtil.textType,
						DataTypeConverterUtil.textType,
						DataTypeConverterUtil.numberType,
						DataTypeConverterUtil.numberType};
			}else if(analyticSource instanceof HadoopAnalyticSource){
				columnType = new String[]{
						HadoopDataType.CHARARRAY,
						HadoopDataType.CHARARRAY,
						HadoopDataType.DOUBLE,
						HadoopDataType.DOUBLE};
			};
			
			for (ValueFrequencyAnalysisResult freqAnalysisResult: list) {
				String[] info = new String[] {
						freqAnalysisResult.isColumnNameNA()?N_A:freqAnalysisResult.getColumnName(),
						freqAnalysisResult.isColumnValueNA()?N_A:freqAnalysisResult.getColumnValue(),
						freqAnalysisResult.isCountNA()?N_A:String.valueOf(freqAnalysisResult.getCount()),
						freqAnalysisResult.isPercentageNA()?N_A:MessageFormat.format("{0,number,#.###%}", freqAnalysisResult.getPercentage())
				};
				if(!tableEntityMap.containsKey(freqAnalysisResult.getColumnName())){
					TableEntity te=new TableEntity();
					te.setColumn(columns);
					te.setSystem(system);
					for(int n=0;n<columns.length;n++){
						if(n==1){
							if(analyticSource instanceof DataBaseAnalyticSource){
								List<TableColumnMetaInfo> columnsMetaInfos = ((DataBaseAnalyticSource)analyticSource).getTableInfo().getColumns();
								for(TableColumnMetaInfo metaInfo:columnsMetaInfos){
									if(metaInfo.getColumnName().equals(freqAnalysisResult.getColumnName())){
										te.addSortColumn(columns[n],metaInfo.getColumnsType());
										break;
									}
								}
							}else if(analyticSource instanceof HadoopAnalyticSource){
								AnalysisFileStructureModel fileStructureModel = ((HadoopAnalyticSource)analyticSource).getHadoopFileStructureModel();
								for(int i=0;i<fileStructureModel.getColumnNameList().size();i++){
									if(fileStructureModel.getColumnNameList().get(i).equals(freqAnalysisResult.getColumnName())){
										te.addSortColumn(columns[n],fileStructureModel.getColumnTypeList().get(i));
										break;
									}
								}
							}
						}else{
							te.addSortColumn(columns[n],columnType[n]);
						}
						
					}
					tableEntityMap.put(freqAnalysisResult.getColumnName(), te);
				}
				tableEntityMap.get(freqAnalysisResult.getColumnName()).addItem(info);
			}
		}
		Set<String> keyEnum = tableEntityMap.keySet();

		String[] columnNames=new String[tableEntityMap.keySet().size()];
		int i=0;
		Iterator<String> iter = keyEnum.iterator();
		while(iter.hasNext()){
			String key =iter.next();
			columnNames[i]=key;
			i++;
		}
		Arrays.sort(columnNames);
		entity=new DropDownTableEntity();
		entity.setColumnNames(columnNames);
		entity.setEntity(tableEntityMap.get(columnNames[0]));
		entity.setObj(obj);
		entity.setTableEntityMap(tableEntityMap);
		DropDownAndTableListVisualizationOutput vout=new DropDownAndTableListVisualizationOutput();
		vout.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SUMMARY,locale));
		vout.setEntity(entity);
		vout.setObj(obj);
		return vout;
		
	}
	
	class TableAndDataset{
		TableEntity te = new TableEntity();
		public TableEntity getTe() {
			return te;
		}
		public void setTe(TableEntity te) {
			this.te = te;
		}
		public DefaultCategoryDataset getDataset() {
			return dataset;
		}
		public void setDataset(DefaultCategoryDataset dataset) {
			this.dataset = dataset;
		}
		DefaultCategoryDataset dataset =new DefaultCategoryDataset();
	}
}
