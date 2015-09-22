/**
 * ClassName HistogramImageVisualizationType.java
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;
import com.alpine.miner.view.ui.dataset.DropDownTableEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.hadoop.HadoopDataType;

public class HistogramImageVisualizationType extends ImageVisualizationType {
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationType#generateOutPut(com.alpine.datamining.api.AnalyticOutPut)
	 */
	
	private static DropDownTableEntity entity;
	private HashMap<String,TableEntity> tableEntityMap;
	
	public static DropDownTableEntity getEntity() {
		return entity;
	}

	public static void setEntity(DropDownTableEntity entity) {
		HistogramImageVisualizationType.entity = entity;
	}

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
		}
		HistogramAnalysisResult  result=(HistogramAnalysisResult)obj;
		
		
		tableEntityMap = new HashMap<String,TableEntity>();
		String[] columns = new String[] {
				VisualLanguagePack.getMessage(VisualLanguagePack.COLUMN_NAME,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.BIN,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.BEGIN,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.END,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PERCENTAGE,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.ACC_COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.ACC_PERCENTAGE,locale)
		};
		AnalyticSource analyticSource = analyzerOutPut.getAnalyticNode().getSource();
		String[] columnTypes = null;
		
		if(analyticSource instanceof DataBaseAnalyticSource){
			columnTypes = new String[]{
					DataTypeConverterUtil.textType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
			};
		}else if(analyticSource instanceof HadoopAnalyticSource){
			columnTypes = new String[]{
					HadoopDataType.CHARARRAY,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
			};
		}

		List<BinHistogramAnalysisResult> histogramResultList = result.getResult();
		for (BinHistogramAnalysisResult histogramResult: histogramResultList) {
			String[] info = new String[] {
					histogramResult.getColumnName(),
					String.valueOf(histogramResult.getBin()),
					String.valueOf(histogramResult.getBegin()),
					String.valueOf(histogramResult.getEnd()),
					String.valueOf(histogramResult.getCount()),
					MessageFormat.format("{0,number,#.###%}", histogramResult.getPercentage()),
					String.valueOf(histogramResult.getAccumCount()),
					MessageFormat.format("{0,number,#.###%}", histogramResult.getAccumPercentage())
			};
			
			if(!tableEntityMap.containsKey(histogramResult.getColumnName())){
				TableEntity tablePanel = new TableEntity();
				tablePanel.setSystem((analyzerOutPut).getAnalyticNode().getSource().getDataSourceType());
				tablePanel.setColumn(columns);
				for(int i=0;i<columns.length;i++){
					tablePanel.addSortColumn(columns[i],columnTypes[i]);
				}
				tableEntityMap.put(histogramResult.getColumnName(), tablePanel);
			}
			tableEntityMap.get(histogramResult.getColumnName()).addItem(info);
		}
		String[] columnNames=new String[tableEntityMap.keySet().size()];
		int i=0;
		Iterator<String> iter = tableEntityMap.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
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

	class TableAndChart{
		DataTableVisualizationOutPut tableOutput;
		JFreeChartImageVisualizationOutPut chartOutput;
		public DataTableVisualizationOutPut getTableOutput() {
			return tableOutput;
		}
		public void setTableOutput(DataTableVisualizationOutPut tableOutput) {
			this.tableOutput = tableOutput;
		}
		public JFreeChartImageVisualizationOutPut getChartOutput() {
			return chartOutput;
		}
		public void setChartOutput(JFreeChartImageVisualizationOutPut chartOutput) {
			this.chartOutput = chartOutput;
		}
		
	}
}
