/**
 * ClassName LogisticRegressionSummaryTableVisualizationType
 *
 * Version information: 1.00
 *
 * Data: 2012-6-21
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import static com.alpine.datamining.api.utility.AlpineMath.powExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.miner.view.ui.dataset.SplitModelForLirEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.tools.AlpineMath;

public class LinearRegressionSummaryTableVisualizationType extends
		TableVisualizationType {

	
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		}
		EngineModel engineModel = (EngineModel) obj;
		
		LinearRegressionGroupGPModel model = (LinearRegressionGroupGPModel)engineModel.getModel();
		
		TableEntity tableEntity = new TableEntity();
		if(analyzerOutPut.getAnalyticNode().getSource()!=null){
			tableEntity.setSystem((analyzerOutPut).getAnalyticNode().getSource().getDataSourceType());
		}
		
		String[] tableColumn=new String[3];
		tableColumn[0]=VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale);
		tableColumn[1]="R^2";
		tableColumn[2]="S";
		tableEntity.setColumn(tableColumn);
		tableEntity.addSortColumn(tableColumn[0],DataTypeConverterUtil.textType);
		tableEntity.addSortColumn(tableColumn[1],DataTypeConverterUtil.numberType);
		tableEntity.addSortColumn(tableColumn[2],DataTypeConverterUtil.numberType);
		Map<String,List<double[]>> groupResiduals=new HashMap<String,List<double[]>>();
		List<TableEntity> tableEntityList=new ArrayList<TableEntity>();
		Map<String,Double> groupSValues=new HashMap<String,Double>();
		if(model!=null&&model.getModelList()!=null){		 
			 Iterator<Entry<String, LinearRegressionModelDB>> iter = model.getModelList().entrySet().iterator();
			 while(iter.hasNext()){
				 Entry<String, LinearRegressionModelDB> entry = iter.next();
				 String distinctValue=entry.getKey();
				 LinearRegressionModelDB singleModel = entry.getValue();
				 String[] values = new String[3];
				 values[0]=distinctValue;
				 values[1]=AlpineMath.doubleExpression(singleModel.getR2());
				 values[2]=AlpineMath.doubleExpression(singleModel.getS());
				 tableEntity.addItem(values);
				 tableEntityList.add(createTable(singleModel,distinctValue));
				 groupResiduals.put(distinctValue,singleModel.getResiduals());
				 groupSValues.put(distinctValue,singleModel.getS());
			 }	 
		}
		
		tableEntity.setStyle(TableEntity.ADD_SELECTION_LINSTENER_FOR_SPLITMODEL);
		
		SplitModelForLirEntity splitModelForLirEntity=new SplitModelForLirEntity();
		splitModelForLirEntity.setSummaryTable(tableEntity);
		splitModelForLirEntity.setDependentColumn(model.getTrainingHeader().getColumns().getLabel().getName());
		splitModelForLirEntity.setGroupResiduals(groupResiduals);
		splitModelForLirEntity.setGroupTable(tableEntityList);
		splitModelForLirEntity.setGroupSValues(groupSValues);
		
		SplitModelLirTableVisualizationOutPut output = new SplitModelLirTableVisualizationOutPut(splitModelForLirEntity);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_SUMMARY,locale));
 
		return output;
	}
	
	private TableEntity createTable(LinearRegressionModelDB model, String distinctValue) {
		String[] attributeNames = model.getColumnNames();
		Double[] coefficients = model.getCoefficients();
		double[] se = model.getSe();
		double[] t = model.getT();
		double[] p = model.getP();

		TableEntity table = new TableEntity();

		table.setColumn(new String[] { "Column", "Coefficient", "SE",
				"T-statistics", "P-value" });
		table.addSortColumn(table.getColumn()[0],DataTypeConverterUtil.textType);
		table.addSortColumn(table.getColumn()[1],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[2],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[3],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[4],DataTypeConverterUtil.numberType);
		String[] headItems ={"Intercept",powExpression(coefficients[attributeNames.length]),powExpression(se[attributeNames.length]),
				powExpression(t[attributeNames.length]) ,powExpression(p[attributeNames.length])};
		table.addItem(headItems);
		for (int i = 0; i < attributeNames.length; i++) {
			table.addItem(new String[] { attributeNames[i],
					powExpression(coefficients[i]),
					powExpression(se[i]),
					powExpression(t[i]),
					powExpression(p[i]) });
		}
		
		table.setTableName(distinctValue);
		return table;
	}

}