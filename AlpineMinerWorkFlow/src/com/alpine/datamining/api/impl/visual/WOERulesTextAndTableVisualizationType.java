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
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.datamining.operator.woe.WOEModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.view.ui.dataset.MultiTextAndTableListEntity;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class WOERulesTextAndTableVisualizationType extends
		TextVisualizationType {
	
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		EngineModel emodel = null;
		AnalysisWOETable model = null;
		if(!(analyzerOutPut instanceof AnalyzerOutPutTrainModel))return null;
		
		emodel = (EngineModel)((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		if(emodel==null||!(emodel.getModel() instanceof WOEModel)) return null;
		
		model=((WOEModel)emodel.getModel()).getWOEInfoTable();
		
		List<TextAndTableListEntity> textAndTableListEntityList=new ArrayList<TextAndTableListEntity>();
		List<DataTextAndTableListVisualizationOutPut> textAndTableListOutput
		=new ArrayList<DataTextAndTableListVisualizationOutPut>();
		
		Map<String,TextAndTableListEntity> nameEntityMap=new HashMap<String,TextAndTableListEntity>();
		
		VisualizationOutPut outputTable=null;
		
		List<AnalysisWOEColumnInfo> modelList = model.getDataTableWOE();
		
		String[] availableValue=new String[modelList.size()];
		int k=0;
		
		for(AnalysisWOEColumnInfo singleModel:modelList){
			TableEntity te = new TableEntity();
			getVTextTable(singleModel,te);//default value is the first
			
			TextAndTableListEntity textAndTableListEntity=new TextAndTableListEntity();
			textAndTableListEntity.addTableEntity(te);
			textAndTableListEntity.setText(getVTextText(singleModel));
			
			textAndTableListEntityList.add(textAndTableListEntity);
			DataTextAndTableListVisualizationOutPut output=new DataTextAndTableListVisualizationOutPut(textAndTableListEntity);
			textAndTableListOutput.add(output);
			
			availableValue[k]=singleModel.getColumnName();
			
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
		
		return outputTable;
	}

	private String getVTextText(AnalysisWOEColumnInfo singleModel) {
		double gini = singleModel.getGini();
		double infoValue = singleModel.getInforValue();
		
		StringBuffer result = new StringBuffer();  
		result.append(Tools.getLineSeparator() + "Gini: " + AlpineMath.powExpression(gini) + Tools.getLineSeparator());
		result.append(Tools.getLineSeparator() + "InfoValue: " + AlpineMath.powExpression(infoValue) + Tools.getLineSeparator());
		
		return result.toString();
	}

	private TextTable getVTextTable(AnalysisWOEColumnInfo model,TableEntity te) {
		TextTable table= new TextTable();
		List<AnalysisWOENode> woeNodeList = model.getInforList();
		if(woeNodeList!=null){
			AnalysisWOENode first = woeNodeList.get(0);
			if(first instanceof AnalysisWOENominalNode){
				table.addLine(new String[]{"ID","Values","WOE Value"});
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
		    		table.addLine(new String[]{
		    				id,sb.toString(),AlpineMath.powExpression(woeValue)
					});
				}
				
				for(int i=0;i<table.getLines().size();i++){
					if(i==0){
						te.setColumn(table.getLines().get(i));
						for(int j=0;j<te.getColumn().length;j++){
						if(j==0){
							te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
						}else if(j==2){
							te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
						}
					}
					}else{
						te.addItem(table.getLines().get(i));
					}
				}
			}else if(first instanceof AnalysisWOENumericNode){
				table.addLine(new String[]{"ID","Bottom","Upper","WOE Value"});
				for(AnalysisWOENode node:woeNodeList){
					String id=((AnalysisWOENumericNode)node).getGroupInfror();
					double bottom = ((AnalysisWOENumericNode)node).getBottom();
					double upper = ((AnalysisWOENumericNode)node).getUpper();
					double woeValue=((AnalysisWOENumericNode)node).getWOEValue();
		    		table.addLine(new String[]{
		    				id,AlpineMath.powExpression(bottom),AlpineMath.powExpression(upper)
		    				,AlpineMath.powExpression(woeValue)
					});
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
						te.addItem(table.getLines().get(i));
					}
				}
			}
		}
		return table;
	}
	
}
