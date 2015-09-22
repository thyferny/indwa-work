/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterRecommendationEvaluation.java
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

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.impl.AnalyticNodeImpl;
import com.alpine.datamining.api.impl.output.AdaBoostAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;

import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.adboost.AdaboostModel;
import com.alpine.datamining.operator.adboost.AdaboostSingleModel;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterAdaboostTrainer extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterAdaboostTrainer INSTANCE = new VisualAdapterAdaboostTrainer();

	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws  Exception {
		if(!(analyzerOutPut instanceof AnalyzerOutPutTrainModel)){
			return null;
		}
		AnalyzerOutPutTrainModel trainModel=(AnalyzerOutPutTrainModel)analyzerOutPut;
		Model model=trainModel.getEngineModel().getModel();
		if(!(model instanceof AdaboostModel)){
			return null;
		}
		
		AdaboostModel adaboostModel=(AdaboostModel)model;
 
		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		models.add( getVisualizationModelDataTables(adaboostModel ,  locale ));	
		
		List<VisualizationModel> subModels=generateaSubVisualModel(analyzerOutPut,adaboostModel,  locale) ;
		if(subModels!=null){
			models.addAll(subModels);
		}
		VisualizationModelComposite vmodel= new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName(),models);
		return vmodel;
	}

	/**
	 * @param analyzerOutPut
	 * @param adaboostModel
	 * @return
	 * @throws Exception 
	 * @throws  
	 */
	private List<VisualizationModel> generateaSubVisualModel(
			AnalyticOutPut analyzerOutPut, AdaboostModel adaboostModel,Locale locale) throws  Exception {
		 List<VisualizationModel> vModels = new ArrayList<VisualizationModel> ();
		for(int i=0;i<adaboostModel.getModelNum();i++){
			AdaboostSingleModel singleModel=adaboostModel.getModel(i);
			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(singleModel.getModel());
			analyzerOutPutModel.getEngineModel().setName(singleModel.getName());
			AnalyticNode analyticNode=new AnalyticNodeImpl();
			analyticNode.setName(singleModel.getName());
			analyticNode.setAdaBoost(true); //.setStatus("adaboost");
			analyzerOutPutModel.setAnalyticNode(analyticNode);
			AdaBoostAnalyzerOutPutTrainModel adaBoostAnalyzerOutPutTrainModel=(AdaBoostAnalyzerOutPutTrainModel)analyzerOutPut;
			DataAnalyzer  analyzer=adaBoostAnalyzerOutPutTrainModel.getDataAnalyzer(singleModel.getName());
			
			if(analyzer!=null){
				analyzer.setName(singleModel.getName()) ;
				analyzer.setOutPut(analyzerOutPutModel);
				analyzerOutPutModel.setDataAnalyzer(analyzer);
			}
			AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
			analyzerOutPutModel.setAnalyticNodeMetaInfo(nodeMetaInfo);
			OutPutVisualAdapter adapter = OutPutVisualAdapterFactory.getInstance().getAdapter(analyzerOutPutModel);
			if(adapter!=null){
				VisualizationModel model = adapter.toVisualModel(analyzerOutPutModel,  locale) ;
				vModels.add(model) ;
			}
 
		}	
		return vModels;
	}

	private VisualizationModel getVisualizationModelDataTables(AdaboostModel adaboostModel ,Locale locale ) {
	
		
		double[] weights=new double[adaboostModel.getModelNum()];
		String[] names=new String[adaboostModel.getModelNum()];
		for(int i=0;i<adaboostModel.getModelNum();i++){
			AdaboostSingleModel singleModel=adaboostModel.getModel(i);
			weights[i]=singleModel.getPeoso();
			names[i]=singleModel.getName();
		}
		List<DataRow> rows = new ArrayList<DataRow> ();	
  
		for(int j = 0; j < names.length; j++){
			String[] items=new String[2];
			items[0]=names[j];
			items[1]=String.valueOf(AlpineMath.powExpression(weights[j]));
			DataRow row = new DataRow();
			row.setData(items);
			rows.add(row) ;
		}
		DataTable table = new DataTable();
		List<TableColumnMetaInfo>  columns = new ArrayList<TableColumnMetaInfo>() ; 
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.ADABOOST_NAME,locale), DBUtil.TYPE_CATE)); 
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.ADABOOST_WEIGHT,locale) , DBUtil.TYPE_NUMBER));
		table.setColumns(columns);
		
		table.setRows(rows) ;
		VisualizationModelDataTable model = new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.ADABOOST_SUMMARY,locale) ,table);
		return model;
	}
	
	 
 	 
}
