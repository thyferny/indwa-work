/**
 * ClassName LinearRegressionTextVisualizationType.java
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.miner.view.ui.dataset.InterActionTextAndTableListEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class LinearRegressionTableVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		if(obj == null)return null;
		EngineModel model = (EngineModel) obj;
		TextAndTableListEntity te=null;
		boolean isGroupBy=false;
		String ditinctValue="";
		if(model.getModel() instanceof LinearRegressionGroupGPModel){
			Map<String, LinearRegressionModelDB> modelList = ((LinearRegressionGroupGPModel)model.getModel()).getModelList();
			Iterator<Entry<String, LinearRegressionModelDB>> iter = modelList.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, LinearRegressionModelDB> entry = iter.next();
				ditinctValue=entry.getKey();
				te = getVTextTable(entry.getValue());
				break;
			}
			isGroupBy=true;
		}else{
			te = getVTextTable((LinearRegressionModelDB)model.getModel());
		}

		HashMap<String, HashMap<String, String>> transformMap=((LinearRegressionModelDB)model.getModel()).getAllTransformMap_valueKey();
		te.setTransformMap(transformMap);
		((InterActionTextAndTableListEntity)te).setOpName(analyzerOutPut.getAnalyticNode().getName());
		DataTextAndTableListVisualizationOutPut output = new DataTextAndTableListVisualizationOutPut(te);
		if(isGroupBy){
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+ditinctValue
					+":"+VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale));
		}else{
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale));
		}	
		return output;
	}

	/**
	 * @param model
	 * @return
	 */
	public static TextAndTableListEntity getVTextTable(LinearRegressionModelDB model) {
		String[] attributeNames = model.getColumnNames();
		Double[] coefficients = model.getCoefficients();
		double[] se = model.getSe();
		double[] t = model.getT();
		double[] p = model.getP();
		InterActionTextAndTableListEntity textAndTable = new InterActionTextAndTableListEntity();
//		textAndTable.setText(getVtextText(model).toString());

		HashMap<String, String[]> ccMap=model.getInteractionColumnColumnMap();
		textAndTable.setColumnColumnMap(ccMap);
		TableEntity table = new TableEntity();
//		table.setSystem(system);
		table.setColumn(new String[] { "Column", "Coefficient", "SE",
				"T-statistics", "P-value" });
		table.addSortColumn(table.getColumn()[0],DataTypeConverterUtil.textType);
		table.addSortColumn(table.getColumn()[1],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[2],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[3],DataTypeConverterUtil.numberType);
		table.addSortColumn(table.getColumn()[4],DataTypeConverterUtil.numberType);
		String[] headItems ={"Intercept",AlpineMath.powExpression(coefficients[attributeNames.length]),AlpineMath.powExpression(se[attributeNames.length]),AlpineMath.powExpression(t[attributeNames.length]) ,AlpineMath.powExpression(p[attributeNames.length])};
		table.addItem(headItems);
		for (int i = 0; i < attributeNames.length; i++) {
			table.addItem(new String[] { attributeNames[i],
					AlpineMath.powExpression(coefficients[i]),
					AlpineMath.powExpression(se[i]),
					AlpineMath.powExpression(t[i]),
					AlpineMath.powExpression(p[i]) });
		}
		textAndTable.addTableEntity(table);
		return textAndTable;
	}
 
}
