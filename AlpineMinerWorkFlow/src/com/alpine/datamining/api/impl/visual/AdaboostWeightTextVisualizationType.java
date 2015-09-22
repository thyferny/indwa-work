/**
 * ClassName AdaboostWeightTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.adboost.AdaboostModel;
import com.alpine.datamining.operator.adboost.AdaboostSingleModel;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class AdaboostWeightTextVisualizationType extends TextVisualizationType {
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {	
		if(!(analyzerOutPut instanceof AnalyzerOutPutTrainModel))return null;
		AnalyzerOutPutTrainModel trainModel=(AnalyzerOutPutTrainModel)analyzerOutPut;
		Model model=trainModel.getEngineModel().getModel();
		if(!(model instanceof AdaboostModel))return null;
		
		AdaboostModel adaboostModel=(AdaboostModel)model;
		String system =null;
		if(((AnalyzerOutPutTrainModel)analyzerOutPut).getDataAnalyzer()!=null){
			system = ((AnalyzerOutPutTrainModel)analyzerOutPut).getDataAnalyzer().getAnalyticSource().getDataSourceType();
		}
		List<TableEntity> list = getTableEntity(adaboostModel,system);	
		TextAndTableListEntity textAndTable = new TextAndTableListEntity();
		textAndTable.setTableEntityList(list);
		DataTextAndTableListVisualizationOutPut output = new DataTextAndTableListVisualizationOutPut(textAndTable);
		output.setName(  VisualLanguagePack.getMessage(VisualLanguagePack.ADABOOST_SUMMARY,locale));
		return output;
	}

	private List<TableEntity> getTableEntity(AdaboostModel adaboostModel,
			String system) {
		List<TableEntity> list = new ArrayList<TableEntity>();
		
		double[] weights=new double[adaboostModel.getModelNum()];
		String[] names=new String[adaboostModel.getModelNum()];
		for(int i=0;i<adaboostModel.getModelNum();i++){
			AdaboostSingleModel singleModel=adaboostModel.getModel(i);
			weights[i]=singleModel.getPeoso();
			names[i]=singleModel.getName();
		}
			
		String[] header =new String[]{VisualLanguagePack.getMessage(VisualLanguagePack.ADABOOST_NAME,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.ADABOOST_WEIGHT,locale)};
		TableEntity te = new TableEntity();
		te.setSystem(system);
		te.setColumn(header);
		
		for(int i=0;i<names.length;i++){	
			for(int j=0;j<te.getColumn().length;j++){
				if(j==0){
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
				}else{
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
				}
			}
		}
		for(int j = 0; j < names.length; j++){
			String[] items=new String[2];
			items[0]=names[j];
			items[1]=String.valueOf(AlpineMath.powExpression(weights[j]));
			te.addItem(items);
		}
		list.add(te);
		return list;
	}
	
}
