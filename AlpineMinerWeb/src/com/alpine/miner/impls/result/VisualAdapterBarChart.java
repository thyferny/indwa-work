/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterBarChart.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.BarchartSeries;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelBarChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelChart;
import com.alpine.util.VisualUtils;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterBarChart  extends AbstractOutPutVisualAdapter  implements OutPutVisualAdapter {
	
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterBarChart();
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale) {
		AnalyticConfiguration config = outPut.getDataAnalyzer().getAnalyticSource().getAnalyticConfig();
		
		AnalyzerOutPutTableObject tableOutPut=(AnalyzerOutPutTableObject)(outPut);
  
		String categoryName = ((BarChartAnalysisConfig) config).getCategoryType();
		String valueName= ((BarChartAnalysisConfig) config).getValueDomain();
		String scopeName=((BarChartAnalysisConfig) config).getScopeDomain();
		boolean isCategoryNameNull = false;
		
		String[] columns =tableOutPut.getDataTable().getColumnNameString();
        //if(scopeName==null || "".equals(scopeName)){
        //    scopeName = categoryName;
        //}
		int scopeIndex=findIndex(columns ,scopeName);
        int categoryIndex = -1;
        if(categoryName==null || "".equals(categoryName)){
            isCategoryNameNull = true;
            //categoryName = scopeName;
        }else{
            categoryIndex=findIndex(columns,categoryName);
        }
		final int valueIndex=findIndex(columns,valueName);
 		DataTable dataTable =tableOutPut.getDataTable();
 	 
 		List<String> errorMessages = new ArrayList<String>(); 
 		int maxCount = OutPutVisualAdapterFactory.getInstance().getMaxChartElements();
 		int total=dataTable.getRows().size();
        /*
        - PIVOTAL 36211095 - Bar chart results should be ordered by the value (descending)
        */
        maxCount=100;
        if(total>maxCount){
 			errorMessages.add(VisualNLS.getMessage(VisualNLS.BARS_EXCEED_LIMIT_SHOW_ORDER_DESCENDING, locale, maxCount));
            Comparator<DataRow> outputComparator = new Comparator<DataRow>() {
                @Override
                public int compare(DataRow dataRow, DataRow dataRow1) {
                    try
                    {
                        Double value0 = new Double(dataRow.getData(valueIndex));
                        Double value1 = new Double(dataRow1.getData(valueIndex));

                        return Double.compare(value1,value0);
                    } catch(NumberFormatException e)
                    {
                        return dataRow1.getData(0).compareTo(dataRow.getData(0));
                    } catch(Exception e)
                    {
                        return 0;
                    }


                }
            };
            Collections.sort(dataTable.getRows(),outputComparator);
            dataTable.setRows(dataTable.getRows().subList(0, 100));
//            int count=0;
// 			List<DataRow> newRows = new ArrayList<DataRow>();
//			for(DataRow dr:dataTable.getRows()){
//				if((count%(total/maxCount))<1){
//                    newRows.add(dr);
//				}
//				count++;
//			}
//
//			dataTable.setRows(newRows) ;
 		}
 		 
		List<String>  scopeValues=getValueList(dataTable ,scopeIndex);
        List<String> categoryValues = null;
        if(-1==categoryIndex){
            categoryValues = new ArrayList<String>();
            //categoryValues.add("@@@_no_category_");
            categoryValues.add("");
        }else{
           categoryValues=getValueList(dataTable,categoryIndex);
        }

		List<String> xValues=categoryValues;
//        if(categoryName==null || "".equals(categoryName)){
//             xValues=scopeValues;
//        }

		List<BarchartSeries> seriesList=new ArrayList<BarchartSeries>();

        if(null==scopeName || "".equals(scopeName)){
            //	int maxBarsPerSeries = 1 ;
          //  for (int i=0 ; i<scopeValues.size() ; i++) {
                //String seriesValue =  scopeValues.get(i);

                float[] yValues= new float[categoryValues.size()];
                //	int aMax=0;
                for (int j=0 ; j<categoryValues.size() ; j++) {
                    String  cateValue = categoryValues.get(j);
                    String dataValue=findValue(cateValue,dataTable,categoryIndex,valueIndex,"scope");
                    if(dataValue!=null&&dataValue.trim().length()>0){
                        yValues[j]=Float.parseFloat(dataValue);
                        //	aMax=aMax+1;
                    }else{///dojo chart have to do this
                        yValues[j]=0;
                    }

                }
                String seriesName = "@@@_no_series_"+categoryName;
                BarchartSeries series=new BarchartSeries(seriesName,yValues);
                seriesList.add(series);
          //  }
        }else if(isCategoryNameNull==true){
            for (int i=0 ; i<scopeValues.size() ; i++) {
                String seriesValue =  scopeValues.get(i);

                float[] yValues= new float[categoryValues.size()];
                //	int aMax=0;
                for (int j=0 ; j<categoryValues.size() ; j++) {
                    String dataValue=findValue(seriesValue,dataTable,scopeIndex,valueIndex,"category");
                    if(dataValue!=null&&dataValue.trim().length()>0){
                        yValues[j]=Float.parseFloat(dataValue);
                        //	aMax=aMax+1;
                    }else{///dojo chart have to do this
                        yValues[j]=0;
                    }

                }
                BarchartSeries series=new BarchartSeries(seriesValue,yValues);
                seriesList.add(series);
            }
        }else{
            //	int maxBarsPerSeries = 1 ;
            for (int i=0 ; i<scopeValues.size() ; i++) {
                String seriesValue =  scopeValues.get(i);

                float[] yValues= new float[categoryValues.size()];
                //	int aMax=0;
                for (int j=0 ; j<categoryValues.size() ; j++) {
                    String  cateValue = categoryValues.get(j);
                    String dataValue=findValue(cateValue,seriesValue,dataTable,scopeIndex,   categoryIndex,valueIndex);
                    if(dataValue!=null&&dataValue.trim().length()>0){
                        yValues[j]=Float.parseFloat(dataValue);
                        //	aMax=aMax+1;
                    }else{///dojo chart have to do this
                        yValues[j]=0;
                    }

                }
                BarchartSeries series=new BarchartSeries(seriesValue,yValues);
                seriesList.add(series);
            }
        }




		//refine seriesList for precision Add by Will
		String[] precision = new String[]{"",""};
		if(seriesList!=null && seriesList.size()>0){
			float maxSeriesValue= 0.0f;
			float minSeriesvalue = 0.0f;
			float maxYValue = 0.0f;
			float minYValue = 0.0f;
			boolean canPaseSeriesValue = true;
		
			try{
				maxSeriesValue = Float.valueOf(seriesList.get(0).getSeriesValue());
				minSeriesvalue = Float.valueOf(seriesList.get(0).getSeriesValue());
			}catch(NumberFormatException e){
				canPaseSeriesValue = false;
			}
			maxYValue = seriesList.get(0).getYValues()[0];
			minYValue = seriesList.get(0).getYValues()[0];
			
			for (int i = 0; i < seriesList.size(); i++) {
				if(canPaseSeriesValue==true){
					if(maxSeriesValue<Float.valueOf(seriesList.get(i).getSeriesValue())){
						maxSeriesValue = Float.valueOf(seriesList.get(i).getSeriesValue());
					}
					if(minSeriesvalue>Float.valueOf(seriesList.get(i).getSeriesValue())){
						minSeriesvalue = Float.valueOf(seriesList.get(i).getSeriesValue());
					}
				}
				//
				for (int j = 0; j < seriesList.get(i).getYValues().length; j++) {
					if(maxYValue<seriesList.get(i).getYValues()[j]){
						maxYValue = seriesList.get(i).getYValues()[j];
					}
					if(minYValue>seriesList.get(i).getYValues()[j]){
						minYValue = seriesList.get(i).getYValues()[j];
					}
				}
				
			}
			
			float n = AlpineMath.adjustUnits(minSeriesvalue, maxSeriesValue);
			float m = AlpineMath.adjustUnits(minYValue, maxYValue);
		    if(n!=1){ //For scope
		    	NumberFormat  df = new DecimalFormat("0.0E0");
		    	scopeName = scopeName + " ("+df.format(n)+")";
		    }
		    if(m!=1){
		    	precision[1] = " ("+VisualUtils.getScientificNumber(m)+")";
		    }else{
		    	precision[1] = "";
		    }
			NumberFormat numformat4SeriesValue = new DecimalFormat("0.000");
			for (int j = 0; j < seriesList.size(); j++) {
				if(canPaseSeriesValue==true){
					
					Float fvalue = Float.valueOf(seriesList.get(j).getSeriesValue())/n ;
					
					seriesList.get(j).setSeriesValue(numformat4SeriesValue.format(fvalue));
				}
				float[] yvalues = seriesList.get(j).getYValues();
				for (int k = 0; k < yvalues.length; k++) {
					yvalues[k] = yvalues[k]/m;
				}
				//seriesList.get(j).setYValues(yvalues);
				
			}
			
		}
		
		//For x Value
		boolean isRotate = false; 
		if(null!=xValues && xValues.size()>0){
			boolean isNumber = true;
			float max=0.0f;
			float min=0.0f;
			try {
				max = Float.valueOf(xValues.get(0));
				min = Float.valueOf(xValues.get(0));
			} catch (NumberFormatException e) {
				isNumber = false;
				if(xValues.get(0).length()>=15){
					isRotate= true;
				}
			}
			if(isNumber==true){
				for (int i = 0; i < xValues.size(); i++) {
					if(max<Float.valueOf(xValues.get(i))){
						max = Float.valueOf(xValues.get(i));
					}
					if(min>Float.valueOf(xValues.get(i))){
						min = Float.valueOf(xValues.get(i));
					}
				}
				float n = AlpineMath.adjustUnits(min, max);
				if (n != 1) {
					precision[0] = " (" + VisualUtils.getScientificNumber(n) + ")";
					NumberFormat numformat4SeriesValue = new DecimalFormat("0.000");
					for (int j = 0; j < xValues.size(); j++) {
						float newValue = Float.valueOf(xValues.get(j)) / n;
						xValues.set(j, numformat4SeriesValue.format(newValue));
					}
				}else{
					precision[0] = "";
					NumberFormat numformat4SeriesValue = new DecimalFormat("0.000");
					for (int j = 0; j < xValues.size(); j++) {
						float newValue = Float.valueOf(xValues.get(j));
						xValues.set(j, numformat4SeriesValue.format(newValue));
					}
				}
			}else{
				precision[0] = "";
			}
			
		}
		//end add by will
		
		String name= "";
		//from popup menu is null
		if(tableOutPut.getAnalyticNode()!=null){
			name=tableOutPut.getAnalyticNode().getName();
		}
		VisualizationModelBarChart visualizationModel = new VisualizationModelBarChart(
				name, 
				scopeName, 
				seriesList );
		visualizationModel.setxAxisTitle(categoryName + precision[0]);
        if(categoryName==null || "".equals(categoryName)){
            visualizationModel.setxAxisTitle(scopeName + precision[0]);
        }
		visualizationModel.setyAxisTitle(valueName + precision[1]);
		//set x label rotate
		if(isRotate==true){
			visualizationModel.setxLableRotation(OutPutVisualAdapter.DEFAULT_XLABEL_ROTATION);
		}
		
		initXLabels(xValues ,visualizationModel); 
		
		int width = VisualUtils.caculateBarchartWidth( dataTable.getRows().size(),xValues.size());
		visualizationModel.setWidth(width);
		visualizationModel.setHeight(550);
		
		if(errorMessages.size()>0){
			visualizationModel.setErrorMessage(errorMessages);
		}
		return visualizationModel;
	}

	private void initXLabels(List<String> xValues, VisualizationModelChart visualizationModel) {
		 List<String[]> labels= new ArrayList<String[]> (); 	
		 int i=1;
		 boolean needRotation=false;
		 for (Iterator<String> iterator = xValues.iterator(); iterator.hasNext();) {
			String value = (String) iterator.next();
			if(value.length()>MAX_LENGTH_ROTATION_DEFAULT){
				needRotation= true;
			}
			labels.add(new String[]{String.valueOf(i),value});
			i++;
			
		}
		visualizationModel.setxLabels(labels); 
		if(needRotation==true){
			visualizationModel.setxLableRotation(-30);
		} 
	}

	private List<String> getValueList(DataTable dataTable,
			int index) {
		List<String> values=new ArrayList<String>();
		List<DataRow> rows = dataTable.getRows();
		if(rows!=null){
			for (Iterator<DataRow> iterator = rows.iterator(); iterator.hasNext();) {
				DataRow dataRow = iterator.next();
				//duplicated will be recovered
				if(values.contains(dataRow.getData(index))==false){
					values.add(dataRow.getData(index));
				}
				
			}
		}
		return values;
	}
	
	private int findIndex(String[] columns, String col) {
		for (int i = 0; i < columns.length; i++) {
			String column=columns[i];
			if(column.startsWith("\"")){
				column=column.replace("\"", "");
			}
			if(column.equals(col)){
				return i;
			}
			 
		}
		return 0;
	}
	
	private String findValue(String cateValue, String scopeValue,
			DataTable dataTable,int scopeIndex, int categoryIndex, int valueIndex) {
		List<DataRow> rows = dataTable.getRows();
		if(rows!=null){
			for (Iterator<DataRow> iterator = rows.iterator(); iterator.hasNext();) {
				DataRow dataRow = iterator.next();
				if(scopeValue.equals(dataRow.getData(scopeIndex))
						&&cateValue.equals(dataRow.getData(categoryIndex))){
					return dataRow.getData(valueIndex);
					}
				}
			}
		return "";
	}
    private String findValue(String value,
                             DataTable dataTable,int index, int valueIndex,String type) {
        List<DataRow> rows = dataTable.getRows();
        if(rows!=null && type == "category"){
            for (Iterator<DataRow> iterator = rows.iterator(); iterator.hasNext();) {
                DataRow dataRow = iterator.next();
                if(value.equals(dataRow.getData(index))){
                    return dataRow.getData(valueIndex);
                }
            }
        }else if(rows!=null && type == "scope"){
            for (Iterator<DataRow> iterator = rows.iterator(); iterator.hasNext();) {
                DataRow dataRow = iterator.next();
                if(value.equals(dataRow.getData(index))){
                    return dataRow.getData(valueIndex);
                }
            }
        }
        return "";
    }
}