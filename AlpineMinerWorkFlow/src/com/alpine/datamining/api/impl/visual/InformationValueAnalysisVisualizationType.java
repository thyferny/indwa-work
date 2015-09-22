package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.attribute.InformationValueResult;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class InformationValueAnalysisVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = analyzerOutPut;
		
		if(!(obj instanceof AnalyzerOutPutObject))return null;
		Object objR=((AnalyzerOutPutObject)obj).getOutPutObject();
		if(!(objR instanceof InformationValueResult))return null;
		InformationValueResult result=(InformationValueResult)objR;
		String system = ((AnalyzerOutPutObject)analyzerOutPut).getDataAnalyzer().getAnalyticSource().getDataSourceType();
		List<TableEntity> list = getTableEntity(result,system);	
		TextAndTableListEntity textAndTable = new TextAndTableListEntity();
		textAndTable.setTableEntityList(list);
		DataTextAndTableListVisualizationOutPut output = new DataTextAndTableListVisualizationOutPut(textAndTable);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}

	private List<TableEntity> getTableEntity(InformationValueResult result,String system) {
		List<TableEntity> list = new ArrayList<TableEntity>();
		
		double[] infoValueArray=result.getInformationValue();
		double[][] woeArray=result.getWeightOfEvidence();
		String[][] attributeValueArray=result.getColumnValues();
		String[] attributeNameArray=result.getColumnNames();
		
		TableEntity te1 = new TableEntity();
		te1.setSystem(system);
		String[] header =new String[]{"InformationValue/Wight of Evidence","Value"};
		for(int i=0;i<attributeNameArray.length;i++){
			TableEntity te = new TableEntity();
			te.setSystem(system);
			te.setColumn(header);
			for(int j=0;j<te.getColumn().length;j++){
				if(j==0){
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
				}else{
					te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
				}
			}
			String[] firstitems=new String[2];
			firstitems[0]="IV("+attributeNameArray[i]+")";
			firstitems[1]=String.valueOf(AlpineMath.powExpression(infoValueArray[i]));
			te.addItem(firstitems);
			for(int j = 0; j < attributeValueArray[i].length; j++){
				String[] items=new String[2];
				items[0]=" WOE("+attributeValueArray[i][j]+")";
				items[1]=String.valueOf(AlpineMath.powExpression(woeArray[i][j]));
				te.addItem(items);
			}
			list.add(te);
		}
		return list;
	}
}
