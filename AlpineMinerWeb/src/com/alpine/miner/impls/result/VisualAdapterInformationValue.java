/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterInformationValue.java
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
import com.alpine.datamining.api.impl.db.attribute.InformationValueResult;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterInformationValue extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 

	
	public static final VisualAdapterInformationValue INSTANCE = new VisualAdapterInformationValue();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
		if(!(outPut instanceof AnalyzerOutPutObject)){
			return null;
		}
		Object objR=((AnalyzerOutPutObject)outPut).getOutPutObject();
		
		if(!(objR instanceof InformationValueResult)){
			return null;
		}
		InformationValueResult result=(InformationValueResult)objR;
	 	 
		VisualizationModelComposite visualModel = 
			getDataTableCompModel(outPut.getDataAnalyzer().getName(),result,  locale);
		return visualModel;
	}
  
	/**
	 * @param result
	 * @return
	 */
	private VisualizationModelComposite getDataTableCompModel(String name,InformationValueResult result,Locale locale) {
 
		double[] infoValueArray=result.getInformationValue();
		double[][] woeArray=result.getWeightOfEvidence();
		String[][] attributeValueArray=result.getColumnValues();
		String[] attributeNameArray=result.getColumnNames();
		List<VisualizationModel> models= new ArrayList<VisualizationModel> (); 
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		columns.add(new TableColumnMetaInfo(INFORMATION_VALUE_WIGHT_OF_EVIDENCE, "")) ;
		columns.add(new TableColumnMetaInfo(VALUE, "")) ;
		
		for(int i=0;i<attributeNameArray.length;i++){
			List<DataRow> rows = new ArrayList<DataRow>();
			DataRow row= new DataRow();
			row.setData(new String[]{IV +
					"("+attributeNameArray[i]+")",AlpineMath.powExpression(infoValueArray[i])});
			rows.add(row) ; 
 
			for(int j = 0; j < attributeValueArray[i].length; j++){
		 
				  row= new DataRow();
				row.setData(new String[]{" " +	WOE +
						"("+attributeValueArray[i][j]+")"
						,String.valueOf(AlpineMath.powExpression(woeArray[i][j]))});
				rows.add(row) ; 
			}
			DataTable dataTable = new DataTable();
			
			dataTable.setColumns(columns) ;
			
			dataTable.setRows(rows) ;
			VisualizationModelDataTable tableModel=new VisualizationModelDataTable(attributeNameArray[i], dataTable) ;
			
			models.add(tableModel);
		}
		 
		VisualizationModelComposite model= new VisualizationModelComposite(name,models);
		return model;
	}
 	 
}
