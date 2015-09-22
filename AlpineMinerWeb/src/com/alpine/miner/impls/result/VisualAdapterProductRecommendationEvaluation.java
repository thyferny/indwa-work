/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterProductRecommendationEvaluation.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.recommendation.RecommendationEvaluationOutPut;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterProductRecommendationEvaluation  extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {

	private static final String NULL = "null";
	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterProductRecommendationEvaluation();
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
		if(!(outPut instanceof RecommendationEvaluationOutPut)){
			return null;
		}
		Double[] result=((RecommendationEvaluationOutPut)outPut).getResult();
		if(result==null){
			return null;
		}
		 
	 
		DataTable dataTable = new DataTable();
		List<TableColumnMetaInfo> columns= new ArrayList<TableColumnMetaInfo>();
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_TRN,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_TRAPEV,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_TRAPOV,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_TRIVP,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_NTRN,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_NTRAPEV,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_NTRAPOV,locale), "")) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.PRE_NTRIVP,locale), "")) ;
		 
		
		dataTable.setColumns(columns);
		List<DataRow> rows = new ArrayList<DataRow> (); 
		String[] resultArray=new String[8];
		for(int i=0;i<result.length;i++){
			if(result[i]==null){
				resultArray[i]=NULL;
			}else{
				resultArray[i]=AlpineMath.powExpression(result[i]);
			}
		}
		DataRow row = new DataRow();
		row.setData(resultArray) ;
		rows.add(row) ;
		dataTable.setRows(rows) ;
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(outPut.getDataAnalyzer().getName(),
				dataTable);
		return visualModel;
	}

	 

}
