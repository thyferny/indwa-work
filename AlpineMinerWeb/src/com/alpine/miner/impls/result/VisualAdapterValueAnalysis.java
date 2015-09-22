/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OutPutJSONAdapterFactory.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueAnalysisResult;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.BarchartSeries;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelBarChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterValueAnalysis extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	private static final String N_A = "N/A";
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterValueAnalysis();
	private static final int COLUMN_INDEX_NAME=0;//1 is type, no use...
	private static final int COLUMN_INDEX_TYPE=1;//1 is type, no use... 
	private static final int COLUMN_INDEX_NUMBER=2;
	private static final int COLUMN_INDEX_ONLY=3;
	private static final int COLUMN_INDEX_NOVALUE=4;
	private static final int COLUMN_INDEX_EMPTYVALUE=5;
	private static final int COLUMN_INDEX_ZEROVALUE=6;
	private static final int COLUMN_INDEX_POSITIVE=11;
	private static final int COLUMN_INDEX_NAGETIVE=12;
	private static final int COLUMN_INDEX_MIN=7;
	private static final int COLUMN_INDEX_AVG=10;
	private static final int COLUMN_INDEX_STD=9;
	private static final int COLUMN_INDEX_MAX=8;
 
	private VisualAdapterValueAnalysis(){
	}
 
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
 
			List<ColumnValueAnalysisResult> list = null;
			if (outPut instanceof AnalyzerOutPutObject) {
				Object obj = ((AnalyzerOutPutObject) outPut).getOutPutObject();
				if (obj instanceof ValueAnalysisResult) {

					list = ((ValueAnalysisResult) obj).getValueAnalysisResult();
				}
			}
			DataSourceType stype = DataSourceType.getDataSourceType(
					outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType());
 
		String[] columns = generateColumns(locale); 
		List<String[]> rows=createValueRows(list);
 
		
//		List<VisualizationModel> models= new ArrayList<VisualizationModel>();
		String[] types = new String[]{
				DBUtil.TYPE_CATE,
				DBUtil.TYPE_CATE,
				
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER
				
		}; 

//        //list of bar chart
//        //form com.alpine.datamining.api.impl.visual.ValueNumericVisualizationType
//        VisualizationModelLayered valueModel = generateValueBarCharts( columns,  rows,stype,locale);
//        if(valueModel!=null){
//            models.add(valueModel) ;
//        }
//
//        //from com.alpine.datamining.api.impl.visual.ValueShapeVisualizationType
//		models.add(generateNumericBarChars(columns,  rows ,  locale )) ;
//
//
//        //from ValueTableVisualizationType
//        models.add(generateTableModel(  columns,  rows,  locale,types, VisualNLS.getMessage(VisualNLS.SUMMARY,locale)));
//
//        VisualizationModelComposite  visualModel
//			= new VisualizationModelComposite(outPut.getDataAnalyzer().getName(),models);
//
//		return visualModel;
        return generateTableModel(  columns,  rows,  locale,types, outPut.getDataAnalyzer().getName());
	}
 	
	private VisualizationModelDataTable generateTableModel( String[] columns,
			List<String[]> rows,Locale locale,String[] types, String title) {
		
		DataTable tables= new DataTable();
		List<TableColumnMetaInfo> tableColumns=new ArrayList<TableColumnMetaInfo> (); 
		for (int i = 0; i < columns.length; i++) {
			tableColumns.add(new TableColumnMetaInfo(columns[i],types[i])); //type.get(columns[i]
		}
		tables.setColumns(tableColumns) ;
		List<DataRow> tableRows =new ArrayList<DataRow> ();
		for (Iterator<String[]> iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next(); 
			DataRow dataRow = new DataRow();
			dataRow.setData(row);
			tableRows.add(dataRow);
			
		}
		tables.setRows(tableRows ) ;
		VisualizationModelDataTable dataTable = new VisualizationModelDataTable(title,tables);
		return dataTable;
	}
  
	private VisualizationModelLayered generateNumericBarChars( 
			String[] columns, List<String[]> rows,Locale locale) {
		
		List<String> keys = VisualUtils.getColumnValue(rows,COLUMN_INDEX_NAME);
		HashMap<String, VisualizationModel> modelMap =createNumericBarCharModelMap(keys,rows,  locale);
		VisualizationModelLayered visualizationModel= new VisualizationModelLayered(VisualNLS.getMessage(VisualNLS.COUNT_SHAPE_ANALYSIS,locale),
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale),	 keys, modelMap);
 	
		return visualizationModel;
	 
	}
	 
	private HashMap<String, VisualizationModel> createNumericBarCharModelMap(
			List<String> keys, List<String[]> rows,Locale locale) {
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel>();
		int i=0;
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key =  iterator.next(); 
			VisualizationModelBarChart model=createNumericBarCharModel(key,rows.get(i),  locale) ; 
			modelMap.put(key, model);
			i++;
		}
		
		return modelMap;
	}
 
	private VisualizationModelBarChart createNumericBarCharModel(
			String key, String[] strings,Locale locale) {
		List<BarchartSeries> series=new ArrayList<BarchartSeries>();
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_NUMBER])}));

		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.UNIQUE_VALUE_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_ONLY])	}));
		
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.NULL_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_NOVALUE])	}));
		
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.EMPTY_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_EMPTYVALUE])	}));
		
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.ZERO_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_ZEROVALUE])	}));
		
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.POS_VALUE_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_POSITIVE])	}));
		
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.NEG_VALUE_COUNT,locale), 
							new float[]{parseValue(strings[COLUMN_INDEX_NAGETIVE])	}));
 
		
		//Addy by Will for precision
		float max = series.get(0).getYValues()[0];
		float min = series.get(0).getYValues()[0];
		for (int i = 0; i < series.size(); i++) {
			if(max<series.get(i).getYValues()[0]){
				max = series.get(i).getYValues()[0];
			}
			if(min>series.get(i).getYValues()[0]){
				min  = series.get(i).getYValues()[0];
			}
		}
		float n = com.alpine.utility.tools.AlpineMath.adjustUnits(min, max);
		for (int j = 0; j < series.size(); j++) {
			float temp = series.get(j).getYValues()[0];
			series.get(j).setYValues(new float[]{temp/n});
		}
	 	//	
		
		List<String[]> xLabels= new ArrayList<String[]> (); 
		xLabels.add(new String[]{"1"," "});
		
		VisualizationModelBarChart barChartModel=new VisualizationModelBarChart(
				key, "", series);
		barChartModel.setxAxisTitle("");
		barChartModel.setxLabels(xLabels) ;
		if(n==1){
			barChartModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.VALUE,locale)) ;
		}else{
			barChartModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.VALUE,locale)+" ("+VisualUtils.getScientificNumber(n)+")") ;
		}
		//only one x
		barChartModel.setWidth(400);
		barChartModel.setHeight(300);
		
		 
		return barChartModel;
	}

	private float parseValue(String string) {
		if(string.equals(N_A)){
			string="0";
		}
		return Float.parseFloat(string);
	}
 


	private VisualizationModelLayered generateValueBarCharts( 
			String[] columns, List<String[]> rows, DataSourceType  dataSource, Locale locale) {
		
		
		List<String> allkeys = VisualUtils.getColumnValue(rows,COLUMN_INDEX_NAME);
		List<String> keys = new ArrayList<String>();
	
		List<String[]> filtedRows = new ArrayList<String[]> ();
		for (int i = 0; i < allkeys.size(); i++) {
			String columnName= allkeys.get(i) ;
			
			String type = getType(rows,columnName) ;
			if(type!=null&&dataSource.isNumberColumnType(type.toUpperCase())){
				keys.add(columnName) ;
				filtedRows.add(rows.get(i)) ;
			}
		}
	 
	 
		
		
		 
		HashMap<String, VisualizationModel> modelMap =createValueBarCharModelMap(keys,filtedRows,locale);
		if(modelMap==null||modelMap.keySet().size()==0){
			return null;
		}
		VisualizationModelLayered visualizationModel= new VisualizationModelLayered(VisualNLS.getMessage(VisualNLS.VALUE_SHAPE_ANALYSIS,locale),
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale),	 keys, modelMap);
 	
		return visualizationModel;
	}
	 
	/**
	 * @param rows
	 * @param columnName
	 * @return
	 */
	private String getType(List<String[]> rows, String columnName) {
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			String[] row = (String[]) iterator.next();
			if (row[COLUMN_INDEX_NAME].equals(columnName)) {
				return row[COLUMN_INDEX_TYPE];
			}

		}
		// TODO Auto-generated method stub
		return null;
	}

	private HashMap<String, VisualizationModel> createValueBarCharModelMap(
			List<String> keys, List<String[]> rows, Locale locale) {
		HashMap<String, VisualizationModel> modelMap= new HashMap<String, VisualizationModel>();
		if(keys!=null){
			int i=0;
			for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
				String key =  iterator.next(); 
				VisualizationModelBarChart model=createValueBarCharModel(key,rows.get(i),locale) ; 
				modelMap.put(key, model);
				i++;
			}
		}
		return modelMap;
	}
 
	private VisualizationModelBarChart createValueBarCharModel(String key,
			String[] strings, Locale locale) {
		List<BarchartSeries> series=new ArrayList<BarchartSeries>();
 
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.MIN_VALUE,locale), 
				new float[]{parseValue( strings[COLUMN_INDEX_MIN])	}));
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.AVERAGE,locale), 
				new float[]{parseValue( strings[COLUMN_INDEX_AVG])	}));
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.STANDARD_DEVIATION,locale), 
				new float[]{parseValue( strings[COLUMN_INDEX_STD])	}));
		series.add( new BarchartSeries(VisualNLS.getMessage(VisualNLS.MAX_VALUE,locale), 
				new float[]{parseValue( strings[COLUMN_INDEX_MAX])	}));
		//Addy by Will for precision
		float max = series.get(0).getYValues()[0];
		float min = series.get(0).getYValues()[0];
		for (int i = 0; i < series.size(); i++) {
			if(max<series.get(i).getYValues()[0]){
				max = series.get(i).getYValues()[0];
			}
			if(min>series.get(i).getYValues()[0]){
				min  = series.get(i).getYValues()[0];
			}
		}
		float n = com.alpine.utility.tools.AlpineMath.adjustUnits(min, max);
		for (int j = 0; j < series.size(); j++) {
			float temp = series.get(j).getYValues()[0];
			series.get(j).setYValues(new float[]{temp/n});
		}
	 	//	
		List<String[]> xLabels= new ArrayList<String[]> (); 
		xLabels.add(new String[]{"1"," "});
		VisualizationModelBarChart barChartModel=new VisualizationModelBarChart(
				key, "", series);
		barChartModel.setxAxisTitle("");
		barChartModel.setxLabels(xLabels);
		if(n==1){
			barChartModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.VALUE,locale));
		}else{
			barChartModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.VALUE,locale)+" ("+VisualUtils.getScientificNumber(n)+")");
		}
		
		//only one x
		barChartModel.setWidth(400);
		barChartModel.setHeight(300);

		return barChartModel;
	}



	private String[] generateColumns(Locale locale) {
		String[] columns = new String[] { VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale),
				VisualNLS.getMessage(VisualNLS.DATA_TYPE,locale),
				VisualNLS.getMessage(VisualNLS.COUNT,locale),
				VisualNLS.getMessage(VisualNLS.UNIQUE_VALUE_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.NULL_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.EMPTY_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.ZERO_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.MIN_VALUE,locale),
				VisualNLS.getMessage(VisualNLS.Q1_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.MEDIAN_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.Q3_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.MAX_VALUE,locale),
				VisualNLS.getMessage(VisualNLS.STANDARD_DEVIATION,locale),
				VisualNLS.getMessage(VisualNLS.AVERAGE,locale),
				VisualNLS.getMessage(VisualNLS.POS_VALUE_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.NEG_VALUE_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.TOP_01_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_01_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_02_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_02_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_03_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_03_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_04_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_04_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_05_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_05_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_06_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_06_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_07_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_07_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_08_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_08_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_09_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_09_PERCENT, locale),
				VisualNLS.getMessage(VisualNLS.TOP_10_VALUE, locale),
				VisualNLS.getMessage(VisualNLS.TOP_10_PERCENT, locale)
		};
		return columns;
	}
 


	private List<String[]> createValueRows(List<ColumnValueAnalysisResult> list) {
		List<String[]> rows= new ArrayList<String[]>() ;
		for (Iterator<ColumnValueAnalysisResult> iterator = list.iterator(); iterator.hasNext();) {
			ColumnValueAnalysisResult dataRow = (ColumnValueAnalysisResult) iterator.next();
			
			String[] info = buildValueArray(dataRow);
			rows.add(info);
		}
		return rows;
	}


	private String[] buildValueArray(ColumnValueAnalysisResult res) {
		String[] info = new String[] {
				res.getColumnName(),
				res.getColumnType(),
				String.valueOf(res.isCountNA() ? N_A
						: res.getCount()),
				String
						.valueOf(res.isUniqueValueCountNA() ? N_A
								: res.getUniqueValueCount()),
				String.valueOf(res.isNullCountNA() ? N_A
						: res.getNullCount()),
				String.valueOf(res.isEmptyCountNA() ? N_A
						: res.getEmptyCount()),
				String.valueOf(res.isZeroCountNA() ? N_A
						: res.getZeroCount()),
				String.valueOf(res.isMinNA() ? N_A
						: AlpineMath.powExpression(res
								.getMin())),

				String.valueOf(
					res.isQ1NA()
					? N_A
					: AlpineMath.powExpression(res.getQ1())),
				String.valueOf(
					res.isMedianNA()
					? N_A
					: AlpineMath.powExpression(res.getMedian())),
				String.valueOf(
					res.isQ3NA()
					? N_A
					: AlpineMath.powExpression(res.getQ3())),

				String.valueOf(res.isMaxNA() ? N_A
						: AlpineMath.powExpression(res
								.getMax())),
				String.valueOf(res.isDeviationNA() ? N_A
						: AlpineMath.powExpression(res
								.getDeviation())),
				String.valueOf(res.isAvgNA() ? N_A
						: AlpineMath.powExpression(res
								.getAvg())),
				String.valueOf(res.isPositiveValueCountNA() ? N_A
								: res.getPositiveValueCount()),
				String.valueOf(res.isNegativeValueCountNA() ? N_A
								: res.getNegativeValueCount()),

				String.valueOf(
					res.isTop01_valNA() || res.isTop01_countNA()
					? N_A
					: res.getTop01Value()),
				String.valueOf(
					res.isTop01_valNA() || res.isTop01_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop01Count() / res.getCount()))),
				String.valueOf(
					res.isTop02_valNA() || res.isTop02_countNA()
					? N_A
					: res.getTop02Value()),
				String.valueOf(
					res.isTop02_valNA() || res.isTop02_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop02Count() / res.getCount()))),
				String.valueOf(
					res.isTop03_valNA() || res.isTop03_countNA()
					? N_A
					: res.getTop03Value()),
				String.valueOf(
					res.isTop03_valNA() || res.isTop03_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop03Count() / res.getCount()))),
				String.valueOf(
					res.isTop04_valNA() || res.isTop04_countNA()
					? N_A
					: res.getTop04Value()),
				String.valueOf(
					res.isTop04_valNA() || res.isTop04_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop04Count() / res.getCount()))),
				String.valueOf(
					res.isTop05_valNA() || res.isTop05_countNA()
					? N_A
					: res.getTop05Value()),
				String.valueOf(
					res.isTop05_valNA() || res.isTop05_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop05Count() / res.getCount()))),
				String.valueOf(
					res.isTop06_valNA() || res.isTop06_countNA()
					? N_A
					: res.getTop06Value()),
				String.valueOf(
					res.isTop06_valNA() || res.isTop06_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop06Count() / res.getCount()))),
				String.valueOf(
					res.isTop07_valNA() || res.isTop07_countNA()
					? N_A
					: res.getTop07Value()),
				String.valueOf(
					res.isTop07_valNA() || res.isTop07_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop07Count() / res.getCount()))),
				String.valueOf(
					res.isTop08_valNA() || res.isTop08_countNA()
					? N_A
					: res.getTop08Value()),
				String.valueOf(
					res.isTop08_valNA() || res.isTop08_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop08Count() / res.getCount()))),
				String.valueOf(
					res.isTop09_valNA() || res.isTop09_countNA()
					? N_A
					: res.getTop09Value()),
				String.valueOf(
					res.isTop09_valNA() || res.isTop09_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop09Count() / res.getCount()))),
				String.valueOf(
					res.isTop10_valNA() || res.isTop10_countNA()
					? N_A
					: res.getTop10Value()),
				String.valueOf(
					res.isTop10_valNA() || res.isTop10_countNA() || res.isCountNA() || res.getCount() == 0
					? N_A
					: (100 * ((double) res.getTop10Count() / res.getCount())))
		};
	return info;
	}
}