/**
 * ClassName HadoopLinearRegressionFileVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-21
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.HashMap;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.view.ui.dataset.InterActionTextAndTableListEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLinearRegressionFileVisualizationType extends
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

		te = getVTextTable((LinearRegressionHadoopModel)model.getModel());
		
		((InterActionTextAndTableListEntity)te).setOpName(analyzerOutPut.getAnalyticNode().getName());
		DataTextAndTableListVisualizationOutPut output = new DataTextAndTableListVisualizationOutPut(te);

		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale));
		
		return output;
	}

	private TextAndTableListEntity getVTextTable(
			LinearRegressionHadoopModel model) {
		String[] attributeNames = model.getColumnNames();
//		List<String> realColumnNames = model.getRealColumnNames();
//		String[] realAttributeNames = HadoopUtil.transferColumnIndexToColumnName(attributeNames, realColumnNames);
		
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
