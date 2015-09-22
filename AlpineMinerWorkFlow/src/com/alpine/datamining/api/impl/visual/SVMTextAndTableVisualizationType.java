/**
 * ClassName SVMTableVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.Iterator;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.svm.SVMModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class SVMTextAndTableVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		EngineModel emodel = null;
		SVMModel model = null;
		if(!(analyzerOutPut instanceof AnalyzerOutPutTrainModel))return null;
		emodel = (EngineModel)((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		if(emodel==null||!(emodel.getModel() instanceof SVMModel)) return null;
		
		model=(SVMModel)emodel.getModel();
		
		TextTable table=getVTextTable(model);
		TableEntity te = new TableEntity();
//		te.setSystem(((AnalyzerOutPutTrainModel)analyzerOutPut).getDataAnalyzer().getAnalyticSource().getDataSourceType());
		
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				te.setColumn(table.getLines().get(i));
				for(int j=0;j<te.getColumn().length;j++){
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
			}
			}else{
				te.addItem(table.getLines().get(i));
			}
				
		}
		TextAndTableListEntity tableList = new TextAndTableListEntity();
		tableList.setText(getVTextText(model));
		tableList.addTableEntity(te);
		DataTextAndTableListVisualizationOutPut outputTable = new DataTextAndTableListVisualizationOutPut(tableList);
		outputTable.setName(analyzerOutPut.getAnalyticNode().getName());
		return outputTable;
	}

	private String getVTextText(SVMModel model) {
//		double b=model.getB();
//		double cumErr=model.getCumErr();
//		double epsilon=model.getEpsilon();
		int indDim=model.getIndDim();
//		int inds=model.getInds();
		int nsvs=model.getNsvs();
//		double rho=model.getRho();
		StringBuffer result = new StringBuffer();  
		
//		result.append(Tools.getLineSeparator() + "B: " + String.valueOf(b) + Tools.getLineSeparator());
//		result.append(Tools.getLineSeparator() + "CumErr: " + String.valueOf(cumErr) + Tools.getLineSeparator());
//		result.append(Tools.getLineSeparator() + "Epsilon: " + String.valueOf(epsilon) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + VisualLanguagePack.getMessage(VisualLanguagePack.SVM_IND_DIM,locale)+":" + String.valueOf(indDim) + Tools.getLineSeparator());
//    	result.append(Tools.getLineSeparator() + "Inds: " + String.valueOf(inds) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + VisualLanguagePack.getMessage(VisualLanguagePack.SVM_NSVS,locale)+":" + String.valueOf(nsvs) + Tools.getLineSeparator());
//    	result.append(Tools.getLineSeparator() + "Rho: " + String.valueOf(rho) + Tools.getLineSeparator());
		return result.toString();
	}

	private TextTable getVTextTable(SVMModel model) {
		TextTable table= new TextTable();
		Double[] weights=model.getWeights();
		Double[] individuals=model.getIndividuals();
		

		DataSet newDataSet=model.getNewDataSet();
		Columns atts=newDataSet.getColumns();
		int colSize=atts.size()+1;
		String[] header=new String[colSize];
		Iterator<Column> iter=atts.iterator();
		int count=1;
		while(iter.hasNext()){
			Column att=iter.next();
			header[count++]=att.getName();
		}
		header[0]="Weights";
		table.addLine(header);
		
		count=0;
		for(int i=0;i<weights.length;i++){
			String[] line=new String[colSize];
			line[0]=AlpineMath.powExpression(weights[i]);
			for(int j=0;j<atts.size();j++){
				line[j+1]=AlpineMath.powExpression(individuals[count++]);
			}
			table.addLine(line);
		}
		return table;
	}
}
