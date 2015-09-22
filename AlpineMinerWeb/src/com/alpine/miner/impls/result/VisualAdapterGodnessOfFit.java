/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterGodnessOfFit.java
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
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitOutPut;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.ValueGoodnessOfFit;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.tools.AlpineMath;

/**
 * roc will draw a line from 0,0 to 1,1 
 * so it may contains 100 points for the cliet draw API like dojo, jFreechart
 * this axis value is from 0 to 1 ,but we get the units =100
 * so for the client ,it is easy to draw a line from 0,0 to 100,100
 * even you only have 2 points, the client will compute the others...
 * */

public class VisualAdapterGodnessOfFit  extends AbstractOutPutVisualAdapter  implements OutPutVisualAdapter {
	

	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterGodnessOfFit();
 @Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale) {

		VisualizationModelDataTable dataTable = null;
		if (analyzerOutPut instanceof GoodnessOfFitOutPut) {
			List<GoodnessOfFit> resultList = ((GoodnessOfFitOutPut) analyzerOutPut)
					.getResultList();
			DataTable table = toTableOutPut(resultList,   locale);
			dataTable = new VisualizationModelDataTable(analyzerOutPut
					.getAnalyticNode().getName(), table);
		}

		return dataTable;
	}
	
	public DataTable toTableOutPut(List<GoodnessOfFit> list,Locale locale){
		
		DataTable te = new DataTable();
		
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		columns.add(new  TableColumnMetaInfo(NN_NODE,"")) ;
		columns.add(new  TableColumnMetaInfo(ACCURACY,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(ERROR2,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(STATS,"")) ;
		columns.add(new  TableColumnMetaInfo(RECALL,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(PRECISION,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(F1,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(SPECIFICITY,DBUtil.TYPE_NUMBER)) ;
		columns.add(new  TableColumnMetaInfo(SENSITIVITY,DBUtil.TYPE_NUMBER)) ;
		te.setColumns(columns );
		List<DataRow> rows = new ArrayList<DataRow>();
 
		for(int i =0;i<list.size();i++){
			GoodnessOfFit gft=list.get(i);
			TextTable textTable=new TextTable();
			String name=gft.getSourceName();
			double accu = gft.getAccuracy();
			double error = gft.getError();
			ArrayList<ValueGoodnessOfFit> gds = gft. getGoodness();
			textTable.addLine(new String[]{STATS,RECALL,PRECISION,F1,SPECIFICITY,SENSITIVITY});		 
			for(int j=0;j<gds.size();j++){
				String[] items = new String[columns.size()];
				items[0] = name;
				items[1] = AlpineMath.doubleExpression(accu);
				items[2] = AlpineMath.doubleExpression(error);
				
				ValueGoodnessOfFit vof=gds.get(j);
				items[3]=vof.getValue();
				items[4]=AlpineMath.doubleExpression(vof.getRecall());
				items[5]=AlpineMath.doubleExpression(vof.getPrecision());
				items[6]=AlpineMath.doubleExpression(vof.getF1());
				items[7]=AlpineMath.doubleExpression(vof.getSpecificity());		
				items[8]=AlpineMath.doubleExpression(vof.getSensitivity());	
				
				DataRow row = new DataRow();
				row.setData(items);
				rows.add(row) ;
			}
		}
 		te.setRows(rows);
		return te;
	}
 
 
}