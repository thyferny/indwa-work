/**
 * ClassName NaiveBayesTextVisualizationType.java
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
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguageConfig;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.StringHandler;

public class NaiveBayesTableVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		EngineModel model = (EngineModel) obj;
		if(model != null){
			NBModel sdModel=(NBModel)model.getModel();
//			String system = ((AnalyzerOutPutTrainModel)analyzerOutPut).getDataAnalyzer().getAnalyticSource().getDataSourceType();
			List<TableEntity> list = getTableEntity(sdModel);
			String text = toVText(sdModel,list);
			
			TextAndTableListEntity textAndTable = new TextAndTableListEntity();
			textAndTable.setText(text);
			textAndTable.setTableEntityList(list);
			DataTextAndTableListVisualizationOutPut output = new DataTextAndTableListVisualizationOutPut(textAndTable);
			output.setName(analyzerOutPut.getAnalyticNode().getName());
			return output;
		}
		else{
			return null;
		}
		
	}


	private List<TableEntity> getTableEntity(NBModel model) {
		List<TableEntity> list = new ArrayList<TableEntity>();
		int numberOfClasses=model.getNumberOfClasses();
		String[] classValues = model.getClassValues();
		double[][][] distributionProperties=model.getDistributionProperties();
		String[] attributeNames=model.getColumneNames();
		boolean[] nominal = model.getNominal();
		String[][] attributeValues = model.getColumnValues();
		double[][][] weightSums = model.getWeightSums();
		
		TableEntity te1 = new TableEntity();
//		te1.setSystem(system);
		String[] header1 =new String[]{"Column","Class","Mean","Standard Deviantion"};
		te1.setColumn(header1);
		for(int j=0;j<te1.getColumn().length;j++){
			if(j==0||j==1){
				te1.addSortColumn(te1.getColumn()[j],DataTypeConverterUtil.textType);
			}else{
				te1.addSortColumn(te1.getColumn()[j],DataTypeConverterUtil.numberType);
			}
		}
		for (int i = 0; i < model.getNumberOfColumns(); i++) {
 			if (nominal[i]) {
 				TableEntity te = new TableEntity();
// 				te.setSystem(system);
 				int colNo=weightSums[i][0].length;
 				String[] header = null;
				if(weightSums[i][0][weightSums[i][0].length-1]==0){
	 				header =new String[colNo+1];
				}
				else
				{
	 				header =new String[colNo+2];
				}


 				header[0]="Column";
 				header[1]="Class";
 				for (int k = 0; k < weightSums[i][0].length; k++) {
 					if(k==weightSums[i][0].length-1){
 						if(weightSums[i][0][k]==0){
 							continue;
 						}
 					}
 					header[k+2]=attributeValues[i][k];
 				}
 				
 				
 				te.setColumn(header);
 				for(int j=0;j<te.getColumn().length;j++){
					if(j==0||j==1){
						te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.textType);
					}else{
						te.addSortColumn(te.getColumn()[j],DataTypeConverterUtil.numberType);
					}
				}
 				te.setTitle(attributeNames[i]);
 				for (int j = 0; j < numberOfClasses; j++) {
 					String []items=null;
// 					if(weightSums[i][j][weightSums[i][j].length-1]==0){
// 						items=new String[colNo+1];
// 					}
// 					else
// 					{
 						items=new String[colNo+2];
// 					}
 					items[0] = attributeNames[i];
 					items[1]= classValues[j] ;
					for (int k = 0; k < weightSums[i][j].length; k++) {
						if(k==weightSums[i][j].length-1){
	 						if(weightSums[i][j][k]==0){
	 							continue;
	 						}
	 					}
						items[k+2]=AlpineMath.doubleExpression(Math.exp(distributionProperties[i][j][k]));
					}
					te.addItem(items);
				}
 				list.add(te);
			} else {
				for (int j = 0; j < numberOfClasses; j++) {
					String [] items=new String[4];
					items[0]=attributeNames[i];
					items[1]=classValues[j];
					items[2]= AlpineMath.doubleExpression(distributionProperties[i][j][NBModel.INDEX_MEAN]) ;
					items[3]= AlpineMath.doubleExpression(distributionProperties[i][j][NBModel.INDEX_STANDARD_DEVIATION]);
					te1.addItem(items);
				}
			}
		}
		if(te1.getRowNumbers()!=0){
			list.add(te1);
		}	
		return list;
	}
	
	
public String toVText(NBModel model, List<TableEntity> list){
		
		StringBuffer buffer= new StringBuffer();
		String[] classValues = model.getClassValues();
		
		int numberOfClasses=model.getNumberOfClasses();
	 
		
		double[] priors=model.getPriors();
		buffer.append("class priors").append(Tools.getLineSeparator());
	for (int i = 0; i <numberOfClasses; i++) {
			
			buffer.append("priors("+classValues[i]+"):").append(AlpineMath.doubleExpression(Math.exp(priors[i]))).append(Tools.getLineSeparator());
		}
		
		if (model.isCalculateDeviance()  )
		{
			double nullDeviance=model.getNullDeviance();
			double deviance=model.getDeviance();
			buffer.append("null Deviance:").append(AlpineMath.doubleExpression(nullDeviance)).append(Tools.getLineSeparator()).append(
					"deviance:").append(AlpineMath.doubleExpression(deviance)).append(Tools.getLineSeparator());
		}
		
		boolean warning=false;
		StringBuffer warningMessage=new StringBuffer();
		
		for(TableEntity tableEntity:list){
			if(tableEntity.getColumn().length>Integer.parseInt(VisualLanguageConfig.NB_TABLE_COLUMN_LIMIT)){
				warningMessage.append(StringHandler.doubleQ(tableEntity.getTitle())).append(",");
			}
		}
		if(warningMessage.length()>0){
			warningMessage=warningMessage.deleteCharAt(warningMessage.length()-1);
			warning=true;
		}

		warningMessage.append(": ").append(VisualLanguagePack.getMessage(VisualLanguagePack.NB_TOOMANY_DISTINCT,locale));
		warningMessage.append(VisualLanguageConfig.NB_TABLE_COLUMN_LIMIT).append(".");
		
		if(warning){
			buffer.append(Tools.getLineSeparator()).append(warningMessage);
		}
		return buffer.toString();
		
	}
}
