package com.alpine.datamining.api.impl.visual;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;
import com.alpine.datamining.operator.timeseries.SingleARIMARPredictResult;
import com.alpine.miner.view.ui.dataset.DropDownTableEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class TimeSeriesPredictTableVisualizationType extends
		TextVisualizationType {
	
	private HashMap<String,TableEntity> tableEntityMap=new HashMap<String,TableEntity>();
	private static DropDownTableEntity entity;

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		if(!(analyzerOutPut instanceof AnalyzerOutPutARIMARPredict))return null;
		AnalyzerOutPutARIMARPredict model=(AnalyzerOutPutARIMARPredict)analyzerOutPut;
		ARIMARPredictResult aResult=model.getRet();
		
		String[] columnNames=new String[aResult.getResults().size()];
		int i=0;
		List<SingleARIMARPredictResult>  results=aResult.getResults();
		
		VisualizationOutPut vout=null;
		if(results.size()==1){//no groupby
			TableEntity te = generateTable(model, results.get(0).getIDData(), results.get(0).getPredict(),
					results.get(0).getSe());
			vout = new DataTableVisualizationOutPut(te);
			vout.setName(analyzerOutPut.getAnalyticNode().getName());	
		}else{
			for(SingleARIMARPredictResult result:results){
				Object[] idDataArray=result.getIDData();
				double[] predictArray=result.getPredict();
				double[] seArray=result.getSe();
				TableEntity te = generateTable(model, idDataArray, predictArray,
						seArray);
				columnNames[i]=result.getGroupByValue();
				tableEntityMap.put(result.getGroupByValue(), te);
				i++;
			}
			Arrays.sort(columnNames);
			entity=new DropDownTableEntity();
			entity.setColumnNames(columnNames);
			entity.setEntity(tableEntityMap.get(columnNames[0]));
			entity.setObj(aResult);
			entity.setTableEntityMap(tableEntityMap);
			vout=new DropDownAndTableListVisualizationOutput();
			vout.setName(analyzerOutPut.getAnalyticNode().getName());
			((DropDownAndTableListVisualizationOutput)vout).setEntity(entity);
			((DropDownAndTableListVisualizationOutput)vout).setObj(aResult);
		}
		return vout;
	}
	private TableEntity generateTable(AnalyzerOutPutARIMARPredict model,
			Object[] idDataArray, double[] predictArray, double[] seArray) {
		TextTable table=generateTable(idDataArray,predictArray,seArray);
		TableEntity te = new TableEntity();
		te.setSystem(model.getDataAnalyzer().getAnalyticSource().getDataSourceType());
		
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				te.setColumn(table.getLines().get(i));
				for(int j=0;j<te.getColumn().length;j++){
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
				}
			}else{
				te.addItem(table.getLines().get(i));
			}
				
		}
		return te;
	}
	public static DropDownTableEntity getEntity() {
		return entity;
	}

	private TextTable generateTable(Object[] idDataArray,double[] predictArray,double[] seArray) {
		TextTable table= new TextTable();
		String[] titleArray=new String[]{"ID","Result","SE"};
		table.addLine(titleArray);

		for(int i=0;i<predictArray.length;i++){
			table.addLine(new String[]{idDataArray[i].toString(),AlpineMath.powExpression(predictArray[i]),//String.valueOf(i+1)
					AlpineMath.powExpression(seArray[i])});
		}
		
		return table;
	}

}
