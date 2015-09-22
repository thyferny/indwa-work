/**
 * ClassName HadoopLinearRegressionTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-21
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.utility.Tools;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLinearRegressionTextVisualizationType extends
		TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		if(obj == null)return null;
		EngineModel model = (EngineModel) obj;
		String message = "";
		if(model != null){
			message = toVText((LinearRegressionHadoopModel)model.getModel());
		}
		
		TextVisualizationOutPut output = new TextVisualizationOutPut(message);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
		
		return output;
	}

	private String toVText(LinearRegressionHadoopModel model) {
		String[] attributeNames=model.getColumnNames();
//		List<String> realColumnNames = model.getRealColumnNames();
//
//		String[] realAttributeNames = HadoopUtil.transferColumnIndexToColumnName(attributeNames, realColumnNames);
		
		Double[] coefficients = model.getCoefficients();
	    	
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		result.append(model.getName()+ " = ");
		result.append(Tools.getLineSeparator());
		
		for (int i = 0; i < attributeNames.length; i++) {
				result.append(model.getCoefficientString(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(coefficients[index])), first) + " * " + attributeNames[i]);
				index++;
				first = false;
				result.append(Tools.getLineSeparator());
		}
		result.append(model.getCoefficientString(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(coefficients[coefficients.length - 1])), first)+Tools.getLineSeparator());
		result.append(Tools.getLineSeparator());
		result.append("R2: "+com.alpine.utility.tools.AlpineMath.doubleExpression(model.getR2()));
		result.append(Tools.getLineSeparator());

		if (Double.isNaN(model.getS()))
		{
			result.append(Tools.getLineSeparator());
			result.append("data size too small!");
			result.append(Tools.getLineSeparator());
			return result.toString();
		}
		result.append("Standard Error: "+com.alpine.utility.tools.AlpineMath.doubleExpression(model.getS()));
		return result.toString();
	}
	
}
