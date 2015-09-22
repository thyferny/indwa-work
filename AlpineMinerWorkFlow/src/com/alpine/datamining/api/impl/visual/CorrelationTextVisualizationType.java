/**
 * ClassName CorrelationTextVisualizationType.java
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

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class CorrelationTextVisualizationType extends TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
		TableEntity te = new TableEntity();
		String[] columns = new String[0];
		String system =(analyzerOutPut).getAnalyticNode().getSource().getDataSourceType();
		if(obj instanceof Object[]){
			if(((Object[])obj)[0] instanceof String[]){
				columns = (String[])((Object[])obj)[0];
				String[] showColumns = new String[columns.length+1];
				for(int i=0;i<showColumns.length;i++){
					if(i==0){
						showColumns[i] = "";
						te.addSortColumn(showColumns[i],DataTypeConverterUtil.textType);
					}else{
						showColumns[i] = columns[i-1];
						te.addSortColumn(showColumns[i],DataTypeConverterUtil.numberType);
					}
				}
				te.setColumn(showColumns);
				te.setSystem(system);
			}
			if(((Object[])obj)[1] instanceof HashMap){
				HashMap<String,Double> ht = (HashMap<String, Double>) ((Object[])obj)[1];
					for(int j=0;j<columns.length;j++){
						String[] item = new String[columns.length+1];
						item[0] = columns[j];
						for(int i=0;i<columns.length;i++){
							Double dd = ht.get(columns[i]+"/"+columns[j]);
							item[i+1] =String.valueOf(dd==null?"":dd);
							if(item[i+1].equals("")){
								dd = ht.get(columns[j]+"/"+columns[i]);
								item[i+1] =String.valueOf(dd==null?"":dd);
							}
							
							if(!item[i+1].equals("")){
								item[i+1] = AlpineMath.powExpression(Double.parseDouble(item[i+1]));
							}
						}
						te.addItem(item);
					}
			}
		}
		
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(te);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
}
