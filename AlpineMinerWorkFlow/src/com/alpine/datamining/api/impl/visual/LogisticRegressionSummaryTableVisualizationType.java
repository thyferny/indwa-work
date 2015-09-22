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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.AlpineMath;

public class LogisticRegressionSummaryTableVisualizationType extends
		TableVisualizationType {

	
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		}
		EngineModel engineModel = (EngineModel) obj;
		
		LogisticRegressionGroupModel model = (LogisticRegressionGroupModel)engineModel.getModel();
		
		TableEntity tableEntity = new TableEntity();
		if(analyzerOutPut.getAnalyticNode().getSource()!=null){
			tableEntity.setSystem((analyzerOutPut).getAnalyticNode().getSource().getDataSourceType());
		}
		
		String[] tableColumn=new String[6];
		tableColumn[0]=VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale);
		tableColumn[1]="Deviance";
		tableColumn[2]="Chi Square";
		tableColumn[3]="Null Deviance";
		tableColumn[4]=VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_ITERATION,locale);
		tableColumn[5]=VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_IS_CONVERGE,locale);
		tableEntity.setColumn(tableColumn);
		tableEntity.addSortColumn(tableColumn[0],DataTypeConverterUtil.textType);
		tableEntity.addSortColumn(tableColumn[1],DataTypeConverterUtil.numberType);
		tableEntity.addSortColumn(tableColumn[2],DataTypeConverterUtil.numberType);
		tableEntity.addSortColumn(tableColumn[3],DataTypeConverterUtil.numberType);
		tableEntity.addSortColumn(tableColumn[4],DataTypeConverterUtil.numberType);
		tableEntity.addSortColumn(tableColumn[5],DataTypeConverterUtil.textType);
		
		if(model!=null&&model.getModelList()!=null){
			 List<TableEntity> tableEntityList=new ArrayList<TableEntity>();
			 Iterator<Entry<String, LogisticRegressionModelDB>> iter = model.getModelList().entrySet().iterator();
			 while(iter.hasNext()){
				 Entry<String, LogisticRegressionModelDB> entry = iter.next();
				 String distinctValue=entry.getKey();
				 LoRModelIfc singleModel = entry.getValue();
				 String[] values = new String[6];
				 values[0]=distinctValue;
				 values[1]=AlpineMath.doubleExpression(singleModel.getModelDeviance());
				 values[2]=AlpineMath.doubleExpression(singleModel.getChiSquare());
				 values[3]=AlpineMath.doubleExpression(singleModel.getNullDeviance());
				 values[4]=String.valueOf(singleModel.getIteration());
				 values[5]=singleModel.isImprovementStop()==true?Resources.TrueOpt:Resources.FalseOpt;
				 tableEntity.addItem(values);
				 tableEntityList.add(createTable(singleModel,distinctValue));
			 }
			
			tableEntity.setTableEntityList(tableEntityList);
		}
		
		tableEntity.setStyle(TableEntity.ADD_SELECTION_LINSTENER_FOR_SPLITMODEL);
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_SUMMARY,locale));
 
		return output;
	}
	
	private TableEntity createTable(LoRModelIfc model, String distinctValue) {
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
		te.setTableName(distinctValue);
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
			table.addLine(new String[]{attributeNames[j],powExpression(beta[j]),
					powExpression(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(Math.exp(beta[j])))),
					powExpression(standardError[j]),powExpression(zValue[j]),
					powExpression(pValue[j]),powExpression(waldStatistic[j])});
				
		}
		return table;
	}
	
	private String[] getBias(LoRModelIfc model){
		double[] beta = model.getBeta();
		double[] standardError = model.getStandardError();
		double[] zValue = model.getzValue();
		double[] pValue = model.getpValue();
		double[] waldStatistic = model.getWaldStatistic();
		String[] bias = new String[7];
		//if (model.isInterceptAdded()==true) {
			bias[0] = "Bias (offset)";
			bias[1] = powExpression(beta[beta.length - 1]); 
			bias[2] = "";                 
			bias[3] = powExpression(standardError[standardError.length - 1]);
			bias[4] = powExpression(zValue[standardError.length - 1]);       
			bias[5] = powExpression(pValue[standardError.length - 1]);       
			bias[6] = powExpression(waldStatistic[waldStatistic.length - 1]);
			return bias;
    	//}
//		return null;
	}
}