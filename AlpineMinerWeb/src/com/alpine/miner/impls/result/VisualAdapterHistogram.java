/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterHistogram.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.impls.result;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopHistogramAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;

import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.util.VisualUtils;

public class VisualAdapterHistogram extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
 
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterHistogram();
	private static final int COLUMN_INDEX_NAME=0;
	private static final int COLUMN_INDEX_COUNT = 4;
	private static final int COLUMN_INDEX_BIN = 1;
	private static final int COLUMN_INDEX_ACCU_COUNT=6;
	private static final int COLUMN_INDEX_PERCENTAGE = 5;
	//begin and end
	private static final int COLUMN_INDEX_BEGINE = 2;
	private static final int COLUMN_INDEX_END = 3;
 
 
	private VisualAdapterHistogram(){
	}
 
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
		 		List<BinHistogramAnalysisResult>  list = null;
			if (outPut instanceof AnalyzerOutPutObject) {
				Object obj = ((AnalyzerOutPutObject) outPut).getOutPutObject();
				if (obj instanceof HistogramAnalysisResult) {

					list = ((HistogramAnalysisResult) obj).getResult();
				}
			}

 
		String[] columnHeads = generateColumns(  locale); 
		String[] columnTypes =new String[] {DBUtil.TYPE_CATE,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_CATE,
				DBUtil.TYPE_NUMBER,
				DBUtil.TYPE_CATE
				 };
		
		List<String[]> rows=createValueRows(list);
	 	
		List<VisualizationModel> models= new ArrayList<VisualizationModel>();


        //list of barchart
//        int column_index_bin = COLUMN_INDEX_BIN;
        boolean isHadoop = false;
        if(outPut.getDataAnalyzer() instanceof HadoopHistogramAnalyzer){
            isHadoop = true;
        }

        models.add(VisualUtils.generateValueCountBarChars4Histogram(columnHeads,  rows ,COLUMN_INDEX_NAME,COLUMN_INDEX_BIN,COLUMN_INDEX_COUNT,
                COLUMN_INDEX_BEGINE,COLUMN_INDEX_END,COLUMN_INDEX_BIN,isHadoop,locale)) ;

		//list of accumlate count  line
		models.add(VisualUtils.generateValueCountLineChars(VisualNLS.getMessage(VisualNLS.ACC_COUNT,locale) ,columnHeads,  
				rows ,COLUMN_INDEX_NAME,COLUMN_INDEX_BIN,COLUMN_INDEX_ACCU_COUNT,locale)) ;
		//list of percentage  line  
		models.add(VisualUtils.generateValueCountLineChars(VisualNLS.getMessage(VisualNLS.PERCENTAGE,locale),columnHeads,  rows ,COLUMN_INDEX_NAME,COLUMN_INDEX_BIN,COLUMN_INDEX_PERCENTAGE,locale)) ;

        //here's the data panel
        models.add(VisualUtils.generateLayeredTableModel(  columnHeads,  rows,COLUMN_INDEX_NAME,locale,columnTypes));

        VisualizationModelComposite  visualModel= new VisualizationModelComposite(outPut.getDataAnalyzer().getName()
				,models);
	 
		return visualModel;
	}
 	 
	private String[] generateColumns(Locale locale) {
		String[] columns = new String[] {
				VisualNLS.getMessage(VisualNLS.COLUMN_NAME,locale),
				VisualNLS.getMessage(VisualNLS.BIN,locale),
				VisualNLS.getMessage(VisualNLS.BEGIN,locale),
				VisualNLS.getMessage(VisualNLS.END,locale),
				VisualNLS.getMessage(VisualNLS.COUNT,locale),
				VisualNLS.getMessage(VisualNLS.PERCENTAGE,locale),
				VisualNLS.getMessage(VisualNLS.ACC_COUNT,locale),
				VisualNLS.getMessage(VisualNLS.ACC_PERCENTAGE,locale)
		};
		return columns;
	}
  

	private List<String[]> createValueRows(List<BinHistogramAnalysisResult> list) {
		List<String[]> rows= new ArrayList<String[]>() ;
		for (Iterator<BinHistogramAnalysisResult> iterator = list.iterator(); iterator.hasNext();) {
			BinHistogramAnalysisResult dataRow =  iterator.next();
			String[] info = buildValueArray(dataRow);
			rows.add(info);
		}
		return rows;
	}


	private String[] buildValueArray(BinHistogramAnalysisResult histogramResult) {
		String[] info = new String[] {
				histogramResult.getColumnName(),
				String.valueOf(histogramResult.getBin()),
				String.valueOf(histogramResult.getBegin()),
				String.valueOf(histogramResult.getEnd()),
				String.valueOf(histogramResult.getCount()),
				Float.isNaN(histogramResult.getPercentage()) ? String.valueOf(Float.NaN) : MessageFormat.format("{0,number,#.###%}", histogramResult.getPercentage()),
				String.valueOf(histogramResult.getAccumCount()),
				Float.isNaN(histogramResult.getAccumPercentage()) ? String.valueOf(Float.NaN) : MessageFormat.format("{0,number,#.###%}", histogramResult.getAccumPercentage())
		};
		
		return info;
	}
}