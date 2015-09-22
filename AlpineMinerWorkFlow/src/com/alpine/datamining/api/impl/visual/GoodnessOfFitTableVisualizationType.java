/**
 * ClassName GoodnessOfFitTableVisualizationType.java
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
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitOutPut;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.ValueGoodnessOfFit;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.tools.AlpineMath;

public class GoodnessOfFitTableVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		TableEntity te = null;
		if (analyzerOutPut instanceof GoodnessOfFitOutPut) {
			obj = ((GoodnessOfFitOutPut)analyzerOutPut).getResultList();
			if (obj instanceof List ){
				te = toTableOutPut((List<GoodnessOfFit>)obj);
				te.setSystem((analyzerOutPut).getAnalyticNode().getSource().getDataSourceType());
			}
		}
		if(te == null)return null;
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(te);
		output.setVisualizationType(this);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
	
	public TableEntity toTableOutPut(List<GoodnessOfFit> list){
		TableEntity te = new TableEntity();
		te.setColumn(new String[]{"NNNode","Accuracy","Error","Stats","Recall","Precision","F1","Specificity","Sensitivity"});
		for(int j=0;j<te.getColumn().length;j++){
			if(j==0||j==3){
				te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
			}else{
				te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
			}
		}
		for(int i =0;i<list.size();i++){
			GoodnessOfFit gft=list.get(i);
			TextTable textTable=new TextTable();
			String name=gft.getSourceName();
			double accu = gft.getAccuracy();
			double error = gft.getError();
			ArrayList<ValueGoodnessOfFit> gds = gft. getGoodness();
			textTable.addLine(new String[]{"Stats","Recall","Precision","F1","Specificity","Sensitivity"});		 
			for(int j=0;j<gds.size();j++){
				String[] items = new String[te.getColumn().length];
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
				te.addItem(items);
			}
		}
		return te;
	}
	
}
