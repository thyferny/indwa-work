/**
 * ClassName LinearRegressionTextVisualizationType.java
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
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.utility.Tools;

public class LinearRegressionTextVisualizationType extends
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
			message = toVText((LinearRegressionModelDB)model.getModel());
		}
		//toVText((LinearRegressionModelDB)model.getModel())
		TextVisualizationOutPut output = new TextVisualizationOutPut(message);//message);
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
		
		return output;
	}

	/**
	 * @param model
	 * @return
	 */
	private String toVText(LinearRegressionModelDB model) {
		StringBuffer result = getVtextText(model);
		
		
//		TextTable table = getVTextTable(model);
//		result.append(table.toTableString() ) ;
//		for (int i = 0; i < attributeNames.length; i++) {
//			result.append("coefficient("+attributeNames[i]+"): "
//					+String.valueOf(coefficients[i])+"\tSE: "
//					+String.valueOf(se[i]) +"\tT-statistics: "
//					+ String.valueOf(t[i]) +"\tP-value: "
//					+ String.valueOf(p[i])+dataTools.getLineSeparator());
//			
//		}
		return result.toString();
		 
	}

	/**
	 * @param model
	 * @return
	 */
	public static TextTable getVTextTable(LinearRegressionModelDB model) {
		String[] attributeNames=model.getColumnNames();
		 Double[] coefficients = model.getCoefficients();
		 	double[] se = model.getSe();
	    	double[] t = model.getT();
	    	double[] p = model.getP();
	    	
		TextTable table= new TextTable();
		
		table.addLine(new String[]{"Column","Coefficient","SE","T-statistics","P-value"});
		for (int i = 0; i < attributeNames.length; i++) {
	 
				table.addLine(new String[]{attributeNames[i],
						String.valueOf(coefficients[i]),String.valueOf(se[i])
						,String.valueOf(t[i]),String.valueOf(p[i])});
 
		
	}
		return table;
	}

	/**
	 * @param model
	 * @return
	 */
	public static StringBuffer getVtextText(LinearRegressionModelDB model) {
		String[] attributeNames=model.getColumnNames();
		 Double[] coefficients = model.getCoefficients();
	    	
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		result.append(model.getLabel().getName()+ " = ");
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
			return result ;
		}
		result.append("Standard Error: "+com.alpine.utility.tools.AlpineMath.doubleExpression(model.getS()));
		return result;
	}
 
}
