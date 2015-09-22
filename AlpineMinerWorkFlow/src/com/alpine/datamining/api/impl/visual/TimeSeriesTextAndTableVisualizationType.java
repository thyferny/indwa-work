package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.timeseries.SingleARIMAModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.view.ui.dataset.MultiTextAndTableListEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.file.StringUtil;

public class TimeSeriesTextAndTableVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		EngineModel emodel = null;
		ARIMAModel model = null;
		if(!(analyzerOutPut instanceof AnalyzerOutPutTrainModel))return null;
		
		emodel = (EngineModel)((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		if(emodel==null||!(emodel.getModel() instanceof ARIMAModel)) return null;
				
		model=(ARIMAModel)emodel.getModel();
		
		List<TextAndTableListEntity> textAndTableListEntityList=new ArrayList<TextAndTableListEntity>();
		List<DataTextAndTableListVisualizationOutPut> textAndTableListOutput
		=new ArrayList<DataTextAndTableListVisualizationOutPut>();
		
		Map<String,TextAndTableListEntity> nameEntityMap=new HashMap<String,TextAndTableListEntity>();
		
		VisualizationOutPut outputTable=null;
		
		List<SingleARIMAModel> modelList=model.getModels();
		if(StringUtil.isEmpty(model.getGroupColumnName())){//no groupBY
			TextTable table=getVTextTable(modelList.get(0));
			TableEntity te = new TableEntity();
			
			generateTableEntity(table, te);
			
			TextAndTableListEntity textAndTableListEntity=new TextAndTableListEntity();
			textAndTableListEntity.addTableEntity(te);
			textAndTableListEntity.setText(getVTextText(modelList.get(0)));
			
			outputTable=new DataTextAndTableListVisualizationOutPut(textAndTableListEntity);
			outputTable.setName(analyzerOutPut.getAnalyticNode().getName());
			
		}else{
			String[] availableValue=new String[modelList.size()];
			int k=0;
			                                   
			for(SingleARIMAModel singleModel:modelList){
				TextTable table=getVTextTable(singleModel);//default value is the first
				TableEntity te = new TableEntity();
				
				generateTableEntity(table, te);
				
				TextAndTableListEntity textAndTableListEntity=new TextAndTableListEntity();
				textAndTableListEntity.addTableEntity(te);
				textAndTableListEntity.setText(getVTextText(singleModel));
				
				textAndTableListEntityList.add(textAndTableListEntity);
				DataTextAndTableListVisualizationOutPut output=new DataTextAndTableListVisualizationOutPut(textAndTableListEntity);
				textAndTableListOutput.add(output);
				
				availableValue[k]=singleModel.getGroupColumnValue();
				
				nameEntityMap.put(availableValue[k], textAndTableListEntity);
				k++;
			}
			
			MultiTextAndTableListEntity tableList = new MultiTextAndTableListEntity();
			tableList.setTextAndTableListEntityList(textAndTableListEntityList);
			tableList.setAvaiableValue(availableValue);
			tableList.setNameEntityMap(nameEntityMap);

			outputTable = new MultiDataTextAndTableListVisualizationOutPut(tableList);
			outputTable.setName(analyzerOutPut.getAnalyticNode().getName());
			((MultiDataTextAndTableListVisualizationOutPut)outputTable).setTextAndTableListOutput(textAndTableListOutput);
		}
		return outputTable;
	}
	private void generateTableEntity(TextTable table, TableEntity te) {
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
				te.addItem(table.getLines().get(i));
			}
				
		}
	}
	public static  TextTable getVTextTable(SingleARIMAModel model) {
		TextTable table= new TextTable();
		double[] phi = model.getPhi();
		double[] standardError = model.getSe();
		double[] theta = model.getTheta();
		double intercept = model.getIntercept();
		int  ncxreg = model.getNcxreg();
		
		table.addLine(new String[]{"Column","Coefficient","SE"});
		
    	if(ncxreg > 0){
    		table.addLine(new String[]{
					"Intercept",AlpineMath.powExpression(intercept),AlpineMath.powExpression(standardError[standardError.length - 1])
			});
    	}
		for(int i=0;i<phi.length;i++){
			table.addLine(new String[]{
					"AR"+i,AlpineMath.powExpression(phi[i]),AlpineMath.powExpression(standardError[i])
			});
		}
    	for(int i=0;i<theta.length;i++){
    		table.addLine(new String[]{
					"MA"+i,AlpineMath.powExpression(theta[i]),AlpineMath.powExpression(standardError[i+phi.length])
			});
    	}

		return table;
	}
	private String getVTextText(SingleARIMAModel model) {
		double p=model.getP();
		double q=model.getQ();
		double d=model.getD();
		double sigma=model.getSigma2();
		double likelihood=model.getLikelihood();
		String str = "arima("+p+","+d+","+q+"):sigma^2 estimated as "+com.alpine.utility.tools.AlpineMath.doubleExpression(sigma)+
		":  part log likelihood = "+com.alpine.utility.tools.AlpineMath.doubleExpression(likelihood);

		StringBuffer result = new StringBuffer();  
		result.append(Tools.getLineSeparator() + "P: " + String.valueOf(p) + Tools.getLineSeparator());
		result.append(Tools.getLineSeparator() + "Q: " + String.valueOf(q) + Tools.getLineSeparator());
		result.append(Tools.getLineSeparator() + "D: " + String.valueOf(d) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + "Summary: " + str + Tools.getLineSeparator());
		return result.toString();
	}
}
