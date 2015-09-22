/**
 * ClassName UnivariateTextAndTableVisualizationType.java
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
import java.util.Iterator;
import java.util.Map;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.variableOptimization.UnivariateVariableOutput;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class UnivariateTextAndTableVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		UnivariateVariableOutput obj = null;
		if(analyzerOutPut instanceof UnivariateVariableOutput){
			obj = (UnivariateVariableOutput)analyzerOutPut;
		}
		if(obj == null){
			return null;
		}

		TextTable table = getVTextTable(obj);
		TableEntity te = new TableEntity();
		TextAndTableListEntity tableList = new TextAndTableListEntity();
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				te.setColumn(table.getLines().get(i));	
				for(int j=0;j<te.getColumn().length;j++){
					if(j==0){
						te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
					}else{
						te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
					}
				}
			}else{
				String attribute = table.getLines().get(i)[0];
				table.getLines().get(i)[0] = attribute;
				te.addItem(table.getLines().get(i));
			}
		}
		tableList.addTableEntity(te);
		DataTextAndTableListVisualizationOutPut outputTable = new DataTextAndTableListVisualizationOutPut(tableList);
		outputTable.setName(analyzerOutPut.getAnalyticNode().getName());
		return outputTable;
	}

	/**
	 * @param model
	 * @return
	 */
	public  TextTable getVTextTable(UnivariateVariableOutput output) {
		TextTable table= new TextTable();
	
		Iterator it = output.getpValueMap().entrySet().iterator();
		table.addLine(new String[]{"Column","P-value"});
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        table.addLine(new String[]{pairs.getKey().toString(),
	        		AlpineMath.powExpression((Double)pairs.getValue())});
	    }

		return table;
	}
	
}
