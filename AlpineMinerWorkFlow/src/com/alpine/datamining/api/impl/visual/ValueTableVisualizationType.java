/**
 * ClassName ValueTableVisualizationType.java
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
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueAnalysisResult;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.hadoop.HadoopDataType;

public class ValueTableVisualizationType extends TableVisualizationType {

	private static final String N_A = "N/A";

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		List<ColumnValueAnalysisResult> list = null;
		if (analyzerOutPut instanceof AnalyzerOutPutObject) {
			obj = ((AnalyzerOutPutObject) analyzerOutPut).getOutPutObject();
			if (obj instanceof ValueAnalysisResult) {

				list = ((ValueAnalysisResult) obj).getValueAnalysisResult();
			}
		}
		if (list == null)
			return null;
		List<ColumnValueAnalysisResult> colAnalysisResultList = list;
		TableEntity panel = new TableEntity();
		panel.setSystem((analyzerOutPut).getAnalyticNode().getSource()
				.getDataSourceType());

		String[] columns = new String[] { VisualLanguagePack.getMessage(VisualLanguagePack.COLUMN_NAME,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TYPE,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.UNIQUE_VALUE_COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.NULL_COUNT,locale),
						VisualLanguagePack.getMessage(VisualLanguagePack.EMPTY_COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.ZERO_COUNT, locale),
						VisualLanguagePack.getMessage(VisualLanguagePack.MIN_VALUE,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.MAX_VALUE,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.STANDARD_DEVIATION,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.AVERAGE,locale),
						VisualLanguagePack.getMessage(VisualLanguagePack.POS_VALUE_COUNT,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.NEG_VALUE_COUNT,locale) };
		
		AnalyticSource analyticSource = analyzerOutPut.getAnalyticNode().getSource();
		String[] columnTypes = null;
		if(analyticSource instanceof DataBaseAnalyticSource){
			columnTypes = new String[] { DataTypeConverterUtil.textType,
					DataTypeConverterUtil.textType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType,
					DataTypeConverterUtil.numberType };
		}else if(analyticSource instanceof HadoopAnalyticSource){
			columnTypes = new String[] { HadoopDataType.CHARARRAY,
					HadoopDataType.CHARARRAY,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE,
					HadoopDataType.DOUBLE };
		}
		
		panel.setColumn(columns);
		for (int i = 0; i < columns.length; i++) {
			panel.addSortColumn(columns[i], columnTypes[i]);
		}


		for (ColumnValueAnalysisResult colAnalysisResult : colAnalysisResultList) {
			String[] info = new String[] {
					colAnalysisResult.getColumnName(),
					colAnalysisResult.getColumnType(),
					String.valueOf(colAnalysisResult.isCountNA() ? N_A
							: colAnalysisResult.getCount()),
					String
							.valueOf(colAnalysisResult.isUniqueValueCountNA() ? N_A
									: colAnalysisResult.getUniqueValueCount()),
					String.valueOf(colAnalysisResult.isNullCountNA() ? N_A
							: colAnalysisResult.getNullCount()),
					String.valueOf(colAnalysisResult.isEmptyCountNA() ? N_A
							: colAnalysisResult.getEmptyCount()),
					String.valueOf(colAnalysisResult.isZeroCountNA() ? N_A
							: colAnalysisResult.getZeroCount()),
					String.valueOf(colAnalysisResult.isMinNA() ? N_A
							: AlpineMath.powExpression(colAnalysisResult
									.getMin())),
					String.valueOf(colAnalysisResult.isMaxNA() ? N_A
							: AlpineMath.powExpression(colAnalysisResult
									.getMax())),
					String.valueOf(colAnalysisResult.isDeviationNA() ? N_A
							: AlpineMath.powExpression(colAnalysisResult
									.getDeviation())),
					String.valueOf(colAnalysisResult.isAvgNA() ? N_A
							: AlpineMath.powExpression(colAnalysisResult
									.getAvg())),
					String
							.valueOf(colAnalysisResult.isPositiveValueCountNA() ? N_A
									: colAnalysisResult.getPositiveValueCount()),
					String
							.valueOf(colAnalysisResult.isNegativeValueCountNA() ? N_A
									: colAnalysisResult.getNegativeValueCount()) };
			panel.addItem(info);
		}

		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(
				panel);
		// need not draw the table any more
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SUMMARY, locale));
		
		return output;
	}

}
