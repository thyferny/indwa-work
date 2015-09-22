/**
 * ClassName LogisticRegressionTextVisualizationType.java
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
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.miner.view.ui.dataset.InterActionTextAndTableListEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.file.StringUtil;

public class LogisticRegressionTextAndTableVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		}
		if(obj == null){
			return null;
		}
		EngineModel model = (EngineModel) obj;
		
		boolean isSplitModel=false;
		TableEntity te = null;
		String ditinctValue="";
		if(model.getModel() instanceof LogisticRegressionGroupModel){
			isSplitModel=true;
			Map<String, LogisticRegressionModelDB> modelListMap = ((LogisticRegressionGroupModel)model.getModel()).getModelList();
			Iterator<Entry<String, LogisticRegressionModelDB>> iter = modelListMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, LogisticRegressionModelDB> entry = iter.next();
				ditinctValue = entry.getKey();
				te = createTable(entry.getValue());
				break;
			}
		}else{
			te = createTable((LoRModelIfc)model.getModel());
		}
		
		
		InterActionTextAndTableListEntity tableList = createInteractionTable(
				analyzerOutPut.getAnalyticNode().getName(), (LoRModelIfc)model.getModel(), te);
		
		DataTextAndTableListVisualizationOutPut outputTable = new DataTextAndTableListVisualizationOutPut(tableList);
		String name=null;
		if(null!=analyzerOutPut.getAnalyticNode().getStatus()
				&&analyzerOutPut.getAnalyticNode().getAdaBoost() ){
			name=analyzerOutPut.getAnalyticNode().getName()+":"+
			VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale);
			tableList.setCheck(false);		
		}else{
			name=VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale);
		}

		outputTable.setName(name);
		//for split model
		if(isSplitModel==true){
			tableList.setCheck(false);	
			outputTable.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+ditinctValue);
		}
		return outputTable;
	}

//	private VisualizationOutPut generateOutput4CompositeModel(
//			LogisticRegressionGroupModel model,String opName) {
//		CompositeVisualizationOutPut compositeOutPut  = new CompositeVisualizationOutPut();
//		if(model!=null&&model.getModelList()!=null){
//			Set<String> keys = model.getModelList().keySet();
//			for(String key:keys){
//				TableEntity te = createTable((LoRModelIfc)model.getModelList().get(key));
//				InterActionTextAndTableListEntity tableList = createInteractionTable(
//						opName, (LoRModelIfc)model.getModelList().get(key), te);
//				
//				DataTextAndTableListVisualizationOutPut outputTable = new DataTextAndTableListVisualizationOutPut(tableList);
//				
//				String name = VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale);
//				name = key+":" +name;
//				outputTable.setName(name) ;
//				compositeOutPut.addChildOutPut(outputTable);
//			}
//			
//		}
//
//		return compositeOutPut ;
//	}

	private InterActionTextAndTableListEntity createInteractionTable(
			String opName,  LoRModelIfc model, TableEntity te) {
		InterActionTextAndTableListEntity tableList = new InterActionTextAndTableListEntity();
		HashMap<String, String[]> ccMap=(model).getInteractionColumnColumnMap();
		tableList.setColumnColumnMap(ccMap);
		tableList.addTableEntity(te);
		HashMap<String, HashMap<String, String>> transformMap=(model).getAllTransformMap_valueKey();
		tableList.setTransformMap(transformMap);
		tableList.setOpName(opName);
		return tableList;
	}

	public static TableEntity createTable(LoRModelIfc model) {
		TextTable table = getVTextTable(model);
		TableEntity te = new TableEntity();
		String bias[] = getBias(model);
		if(bias != null){
			te.addItem(bias);
		}
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
				table.getLines().get(i)[0] = "beta("+attribute+")";
				te.addItem(table.getLines().get(i));
			}
		}
		return te;
	}

	/**
	 * @param model
	 * @return
	 */
	public static  TextTable getVTextTable(LoRModelIfc model) {
		TextTable table= new TextTable();
		double[] beta = model.getBeta();
		String[]attributeNames=model.getColumnNames();
		double[] standardError = model.getStandardError();
		double[] zValue = model.getzValue();
		double[] pValue = model.getpValue();
		double[] waldStatistic = model.getWaldStatistic();
		
		table.addLine(new String[]{"Column","Beta","Odds Ratio","SE","Z-value","P-value","Wald"});
    	
		for (int j = 0; j < beta.length - 1; j++) {
			table.addLine(new String[]{attributeNames[j],
					AlpineMath.powExpression(beta[j]),
					AlpineMath.powExpression(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(Math.exp(beta[j])))),
					AlpineMath.powExpression(standardError[j]),
					AlpineMath.powExpression(zValue[j]),
					AlpineMath.powExpression(pValue[j]),
					AlpineMath.powExpression(waldStatistic[j])});
				
		}
		return table;
	}
	
	private static String[] getBias(LoRModelIfc model){
		double[] beta = model.getBeta();
		double[] standardError = model.getStandardError();
		double[] zValue = model.getzValue();
		double[] pValue = model.getpValue();
		double[] waldStatistic = model.getWaldStatistic();
		String[] bias = new String[7];
		//if (model.isInterceptAdded()==true) {
			bias[0] = "Bias (offset)";
			bias[1] = AlpineMath.powExpression(beta[beta.length - 1]); 
			bias[2] = "";                 
			bias[3] = AlpineMath.powExpression(standardError[standardError.length - 1]);
			bias[4] = AlpineMath.powExpression(zValue[standardError.length - 1]);       
			bias[5] = AlpineMath.powExpression(pValue[standardError.length - 1]);       
			bias[6] = AlpineMath.powExpression(waldStatistic[waldStatistic.length - 1]);
			return bias;
    	//}
		//return null;
	}
}
