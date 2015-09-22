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
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.file.StringUtil;

public class LogisticRegressionTextVisualizationType extends
		TextVisualizationType {
 
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel();
		}
		EngineModel model = (EngineModel) obj;
		
		if(model.getModel() instanceof LogisticRegressionGroupModel){
			return generateOutput4CompositeModel((LogisticRegressionGroupModel)model.getModel(),analyzerOutPut.getDataAnalyzer());
		}

		String message = "";
		if(model != null){
			message = toVText((LoRModelIfc)model.getModel());
		}

		TextVisualizationOutPut output = new TextVisualizationOutPut(message);//);
		String name=null;
		if(null!=analyzerOutPut.getAnalyticNode().getStatus()
				&&analyzerOutPut.getAnalyticNode().getAdaBoost()){
			name=analyzerOutPut.getAnalyticNode().getName()+":"
			+VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale);
		}else{
			name=VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale);
		}
		output.setName(name);
		return output;
	}

	private CompositeVisualizationOutPut generateOutput4CompositeModel(
			LogisticRegressionGroupModel model, DataAnalyzer dataAnalyzer) {
		CompositeVisualizationOutPut compositeOutPut  = new CompositeVisualizationOutPut();
		if(model!=null&&model.getModelList()!=null){
			Set<String> keys = model.getModelList().keySet();
			for(String key:keys){
				String message = toVText((LoRModelIfc)model.getModelList().get(key));
				TextVisualizationOutPut output = new TextVisualizationOutPut(message);//);
				output.setAnalyzer(dataAnalyzer);
				String name = VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale);
				name = key+":" +name;
				output.setName(name) ;
				compositeOutPut.addChildOutPut(output);
			}
			
		}

		return compositeOutPut ;
	}

	/**
	 * @param model
	 * @return
	 */
	private String toVText(LoRModelIfc model) {
		StringBuffer result = getVTextText(model);
//		TextTable table = getVTextTable(model);
//		result.append(table.toTableString() ) ;
		
//		result.append("Coefficients:" + dataTools.getLineSeparator());
//    	
//		for (int j = 0; j < beta.length - 1; j++) {
//			result.append("beta(" + attributeNames[j] + ") = " + String.valueOf(beta[j]));
//			result.append(" \t\t(SE: " + String.valueOf(standardError[j]));
//			result.append(", z-value: " + String.valueOf(zValue[j]));
//			result.append(", p-value: " + String.valueOf(pValue[j]));
//			result.append(", Wald: " + String.valueOf(waldStatistic[j]) + ")" + dataTools.getLineSeparator());
//		}
		return result.toString();
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
		
		table.addLine(new String[]{"Column","Beta","SE","Z-value","P-value","Wald"});
    	
		for (int j = 0; j < beta.length - 1; j++) {
			table.addLine(new String[]{attributeNames[j],String.valueOf(beta[j]),
					String.valueOf(standardError[j]),String.valueOf(zValue[j]),
					String.valueOf(pValue[j]),String.valueOf(waldStatistic[j]) });
		}
		return table;
	}

	/**
	 * @param model
	 * @return
	 */
	public   StringBuffer getVTextText(LoRModelIfc model) {
		StringBuffer result = new StringBuffer();
		if (model.isImprovementStop()==false)
    	{
    		result.append(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.ALGORITHM_DID_NOT_CONVERGE,locale)).append(Tools.getLineSeparators(2));
    	}

    	result.append(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.ITERATION, locale)+": "
    				+model.getIteration()+Tools.getLineSeparator());
    	   	
    	result.append(Tools.getLineSeparator() + "Deviance: " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getModelDeviance()) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + "nullDeviance: " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getNullDeviance()) + Tools.getLineSeparator());
    	result.append(Tools.getLineSeparator() + "chiSquare: " + com.alpine.utility.tools.AlpineMath.doubleExpression(model.getChiSquare()) + Tools.getLineSeparator());
		return result;
	}
}
