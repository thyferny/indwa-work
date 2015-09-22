/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterLogisticRegression.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.WOEModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterWOEModel extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
  

	public static final VisualAdapterWOEModel INSTANCE = new VisualAdapterWOEModel();

	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut , Locale locale)
			throws RuntimeException {

		EngineModel model = null;
		if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
			model = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
		}
		if (model == null) {
			return null;
		}
		WOEModel woeModel = (WOEModel) model.getModel();

		 
		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		models.add(createTextModel(woeModel,locale));
		
	 
		models.add(createLayeredTableModel(woeModel,locale));
		String name = analyzerOutPut.getAnalyticNode().getName();
		VisualizationModelComposite visualModel = new VisualizationModelComposite(name, models);
		return visualModel;

	}
  
	/**
	 * @param woeModel
	 * @param locale
	 * @return
	 */
	private VisualizationModel createLayeredTableModel(WOEModel woeModel,
			Locale locale) {
		List<AnalysisWOEColumnInfo> modelList = woeModel.getWOEInfoTable().getDataTableWOE();
		List<String> keys = new ArrayList<String>();
		HashMap<String, VisualizationModel> modelMap = new HashMap<String, VisualizationModel> ();
		for(AnalysisWOEColumnInfo singleModel:modelList){
			keys.add(singleModel.getColumnName()) ;
			modelMap.put(singleModel.getColumnName(),  getTableVisualModel(singleModel));  
		}
		VisualizationModelLayered model = new VisualizationModelLayered(VisualNLS.getMessage(VisualNLS.DATA_TITLE, locale),
				VisualNLS.getMessage(VisualNLS.Column_Name, locale), keys, modelMap);
		return model;
	}

	/**
	 * @param singleModel
	 * @return
	 */
	private VisualizationModel getTableVisualModel(
			AnalysisWOEColumnInfo singleModel) {
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo> (); 
		List<DataRow> rows = new ArrayList<DataRow> ();
		List<AnalysisWOENode> woeNodeList = singleModel.getInforList();
		if(woeNodeList!=null){
			AnalysisWOENode first = woeNodeList.get(0);
			if(first instanceof AnalysisWOENominalNode){
				columns.add(new TableColumnMetaInfo("ID", "")) ;
				columns.add(new TableColumnMetaInfo("Values", "")) ;
				columns.add(new TableColumnMetaInfo("WOE Value", "")) ;
			 
				for(AnalysisWOENode node:woeNodeList){
					String id=((AnalysisWOENominalNode)node).getGroupInfror();
					List<String> values = ((AnalysisWOENominalNode)node).getChoosedList();
					StringBuilder sb=new StringBuilder();
					for(String s:values){
						sb.append(s).append(",");
					}
					if(sb.length()>0){
						sb=sb.deleteCharAt(sb.length()-1);
					}
					double woeValue=((AnalysisWOENominalNode)node).getWOEValue();
					
		    		rows.add(new DataRow(new String[]{
		    				id,sb.toString(),AlpineMath.powExpression(woeValue)
					}));
				}
				
 
			}else if(first instanceof AnalysisWOENumericNode){
				columns.add(new TableColumnMetaInfo("ID", "")) ;
				columns.add(new TableColumnMetaInfo("Bottom", "")) ;
				columns.add(new TableColumnMetaInfo("Upper", "")) ;
				
				columns.add(new TableColumnMetaInfo("WOE Value", "")) ;
				
				for(AnalysisWOENode node:woeNodeList){
					String id=((AnalysisWOENumericNode)node).getGroupInfror();
					double bottom = ((AnalysisWOENumericNode)node).getBottom();
					double upper = ((AnalysisWOENumericNode)node).getUpper();
					double woeValue=((AnalysisWOENumericNode)node).getWOEValue();
					
					rows.add(new DataRow(new String[]{
		    				id,AlpineMath.powExpression(bottom),AlpineMath.powExpression(upper)
		    				,AlpineMath.powExpression(woeValue)
					}));
				}
				
				 
			}
		}
		 
		DataTable dataTable = new DataTable();  

		dataTable.setColumns(columns);
	
		dataTable.setRows(rows) ;
		VisualizationModel vModel = new VisualizationModelDataTable(singleModel.getColumnName(), dataTable); 
		return vModel;
	}

	/**
	 * @param woeModel
	 * @param locale 
	 * @return
	 */
	private VisualizationModel createTextModel(WOEModel woeModel, Locale locale) { 
	 
		List<AnalysisWOEColumnInfo> modelList = woeModel.getWOEInfoTable().getDataTableWOE();
		
	 
		String text ="";
		StringBuffer result = new StringBuffer();  
		for(AnalysisWOEColumnInfo singleModel:modelList){
			List<AnalysisWOENode> woeNodeList = singleModel.getInforList();
		
			if (woeNodeList != null) {
				double gini = singleModel.getGini();
				double infoValue = singleModel.getInforValue();
				result.append(Tools.getLineSeparator() + singleModel.getColumnName()+": \n" );
				result.append("\tGini: " + AlpineMath.powExpression(gini)  );
				result.append("\tInfoValue: " + AlpineMath.powExpression(infoValue) + Tools.getLineSeparator());

			}
			
		}
		text= result.toString();
		
		VisualizationModel vmodel = new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale), text);
	 	return vmodel;
	}

//	 
//	
//	/**
//	 * @param singleModel
//	 * @return
//	 */
//	private VisualizationModel getTextVisualModel(AnalysisWOEColumnInfo singleModel) {
//
//		List<AnalysisWOENode> woeNodeList = singleModel.getInforList();
//		String text ="";
//		if (woeNodeList != null) {
//			double gini = singleModel.getGini();
//			double infoValue = singleModel.getInforValue();
//			
//			StringBuffer result = new StringBuffer();  
//			result.append(Tools.getLineSeparator() + "Gini: " + AlpineMath.powExpression(gini) + Tools.getLineSeparator());
//			result.append(Tools.getLineSeparator() + "InfoValue: " + AlpineMath.powExpression(infoValue) + Tools.getLineSeparator());
//			
//			text= result.toString();
//
//		}
//		VisualizationModel vmodel = new VisualizationModelText(singleModel.getColumnName(), text);
//		return vmodel;
//	}
}
