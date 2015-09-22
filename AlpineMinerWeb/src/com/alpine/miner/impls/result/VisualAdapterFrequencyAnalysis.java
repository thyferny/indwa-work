/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterFrequencyAnalysis.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.text.MessageFormat;
import java.util.*;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.operator.attributeanalysisresult.FrequencyAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.util.VisualUtils;

public class VisualAdapterFrequencyAnalysis extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterFrequencyAnalysis();
	private static final int COLUMN_INDEX_NAME=0;
	private static final int COLUMN_INDEX_VALUE=1;
	private static final int COLUMN_INDEX_COUNT=2;

    class ValueFrequencyAnalysisResultComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            String colName1 = ((ValueFrequencyAnalysisResult)o1).getColumnName();
            String colName2 = ((ValueFrequencyAnalysisResult)o2).getColumnName();
            if(colName1.equals(colName2)==true){
                String colValue1 = ((ValueFrequencyAnalysisResult)o1).getColumnValue();
                String colValue2 = ((ValueFrequencyAnalysisResult)o2).getColumnValue();
                try{
                    float f1 = Float.parseFloat(colValue1);
                    float f2 = Float.parseFloat(colValue2);
                    if(f1>f2){
                        return 1;
                    }else if(f1==f2){
                        return 0;
                    }else{
                        return -1;
                    }
                }catch (NumberFormatException e){
                    return 0;
                }
            }else if(colName1.compareTo(colName2)>0){
                return 1;
            }else {
               return -1;
            }
        }
    }
 
	private VisualAdapterFrequencyAnalysis(){
	}
 
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
 
			List<ValueFrequencyAnalysisResult> initList = new ArrayList<ValueFrequencyAnalysisResult>();
			if (outPut instanceof AnalyzerOutPutObject) {
				Object resultObj = ((AnalyzerOutPutObject) outPut).getOutPutObject();
				if (resultObj instanceof FrequencyAnalysisResult) {
					initList = ((FrequencyAnalysisResult) resultObj).getFrequencyAnalysisResult();
				}
			}
			
			//For precision add by Will
			Map<String,String> precisionMap = new HashMap<String, String>();
			List<String> errorMessages = new ArrayList<String>(); 
			int zz=100;zz++;
            int maxCount=100;
            boolean addedErrorMessage=false;
            Map<String,Integer> groupCountByColumn = new HashMap<String,Integer>();
            List<ValueFrequencyAnalysisResult> list = new ArrayList<ValueFrequencyAnalysisResult>();
            for(ValueFrequencyAnalysisResult l:initList){
            	Integer cc=groupCountByColumn.get(l.getColumnName());
            	if(null==cc){
            		groupCountByColumn.put(l.getColumnName(), 1);
            		cc=1;
            	}else{
            		groupCountByColumn.put(l.getColumnName(), cc+1);
            	}
            	if(cc<maxCount){
            		list.add(l);
            	}else if(!addedErrorMessage){
            		errorMessages.add(VisualNLS.getMessage(VisualNLS.FREQUENCY_EXCEED_LIMIT, locale, maxCount));
            		addedErrorMessage=true;
            	}
            }
            
            
            Collections.sort(list,new ValueFrequencyAnalysisResultComparator());
            
            
            
            
            
			if(list.size()>0){
				
//				String columnName = list.get(0).getColumnName();
//				List<String> columnNames = new ArrayList<String>();
//				columnNames.add(columnName);
//				for (int i = 0; i < list.size(); i++) {
//
//					if(!columnName.equalsIgnoreCase(list.get(i).getColumnName())){
//						columnName = list.get(i).getColumnName();
//						columnNames.add(columnName);
//					}
//				}
//				float maxColVal[] = new float[columnNames.size()];
//				float minColVal[] = new float[columnNames.size()];
//
//				//init maxMin
//			    for (int i = 0; i < columnNames.size(); i++) {
//					String colName = columnNames.get(i);
//					for (int j = 0; j < list.size(); j++) {
//						if(list.get(j).getColumnName().equalsIgnoreCase(colName)){
//							try {
//								maxColVal[i] = Float.valueOf(list.get(j).getColumnValue());
//								minColVal[i] = Float.valueOf(list.get(j).getColumnValue());
//							} catch (NumberFormatException e) {
//							}
//				           break;
//						}
//					}
//				}
//			    //init maxMin end
//
//
//				for (int i = 0; i < list.size(); i++) {
//					float tmpVal = 0.0f;
//					ValueFrequencyAnalysisResult tmpresult = list.get(i);
//					try {
//						tmpVal = Float.valueOf(tmpresult.getColumnValue());
//					} catch (NumberFormatException e) {
//					}
//					int index = columnNames.indexOf(tmpresult.getColumnName());
//					if(maxColVal[index]<tmpVal){
//						maxColVal[index] = tmpVal;
//					}
//					if(minColVal[index]>tmpVal){
//						minColVal[index] = tmpVal;
//					}
//				}
//				for (int j = 0; j < list.size(); j++) {
//					float tmpVal = 0.0f;
//					ValueFrequencyAnalysisResult tmpresult = list.get(j);
//					int index = columnNames.indexOf(tmpresult.getColumnName());
//					float n = com.alpine.utility.tools.AlpineMath.adjustUnits(minColVal[index], maxColVal[index]);
//					if(n==0.0f){
//						n=1.0f;
//					}
//				 precisionMap.put(tmpresult.getColumnName(),VisualUtils.getScientificNumber(n));
//					try {
//						tmpVal = Float.valueOf(tmpresult.getColumnValue());
//						list.get(j).setColumnValue(String.valueOf(tmpVal/n));
//						//list.get(j).setColumnValue(VisualUtils.getScientificNumber(tmpVal)); //
//					} catch (NumberFormatException e) {
//					}
//
//				}
			}

 
		String[] columnHeads = generateColumns(  locale); 
		String[] columnTypes =new String[] {DBUtil.TYPE_CATE,
				DBUtil.TYPE_CATE,//value could be time, string,boolean...
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_CATE //11%, shoulbe be a string
				 };
		List<String[]> rows=createValueRows(list);
	 	
		List<VisualizationModel> models= new ArrayList<VisualizationModel>();
		
		
		//list of barchart
		models.add(VisualUtils.generateValueCountBarChars(precisionMap,columnHeads,  rows ,COLUMN_INDEX_NAME,COLUMN_INDEX_VALUE,COLUMN_INDEX_COUNT,locale)) ;

        models.add(VisualUtils.generateLayeredTableModel(  columnHeads,  rows,COLUMN_INDEX_NAME,locale, columnTypes));

        VisualizationModelComposite  visualModel= new VisualizationModelComposite(outPut.getDataAnalyzer().getName()
				,models);
        
        if(errorMessages.size()>0){
        	visualModel.setErrorMessage(errorMessages);
		}
        
		return visualModel;
	}
 

	private String[] generateColumns(Locale locale) {
		String[] columns = new String[] {
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale),
				VisualNLS.getMessage(VisualNLS.VALUE,locale),
				VisualNLS.getMessage(VisualNLS.COUNT,locale),
				VisualNLS.getMessage(VisualNLS.PERCENTAGE,locale)
				 };
		
		
		return columns;
	}
 


	private List<String[]> createValueRows(List<ValueFrequencyAnalysisResult> list) {
		List<String[]> rows= new ArrayList<String[]>() ;
		for (Iterator<ValueFrequencyAnalysisResult> iterator = list.iterator(); iterator.hasNext();) {
			ValueFrequencyAnalysisResult dataRow =  iterator.next();
			String[] info = buildValueArray(dataRow);
			rows.add(info);
		}
		return rows;
	}


	private String[] buildValueArray(ValueFrequencyAnalysisResult freqAnalysisResult) {
		String[] info = new String[] {
				freqAnalysisResult.isColumnNameNA()?VisualUtils.N_A:freqAnalysisResult.getColumnName(),
				freqAnalysisResult.isColumnValueNA()?VisualUtils.N_A:freqAnalysisResult.getColumnValue(),
				freqAnalysisResult.isCountNA()?VisualUtils.N_A:String.valueOf(freqAnalysisResult.getCount()),
				freqAnalysisResult.isPercentageNA()?VisualUtils.N_A:MessageFormat.format("{0,number,#.###%}", freqAnalysisResult.getPercentage())
		};
		return info;
	}
}