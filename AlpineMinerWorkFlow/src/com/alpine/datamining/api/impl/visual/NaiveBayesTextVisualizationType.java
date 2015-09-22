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
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.tools.AlpineMath;

public class NaiveBayesTextVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		EngineModel model = (EngineModel) obj;
		if(model != null){
			
			NBModel sdModel=(NBModel)model.getModel();
			TextVisualizationOutPut output = new TextVisualizationOutPut(toVText(sdModel));
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.MESSAGE_TITLE,locale));
			output.setVisualizationType(this);
			return output;
		}
		else{
			return null;
		}
		
	}
	
	public String toVText(NBModel model){
		
		StringBuffer buffer= new StringBuffer();
		String[] classValues = model.getClassValues();
		
		int numberOfClasses=model.getNumberOfClasses();
	 
		
		double[] priors=model.getPriors();
		buffer.append("class priors").append(Tools.getLineSeparator());
	for (int i = 0; i <numberOfClasses; i++) {
			
			buffer.append("priors("+classValues[i]+"):").append(AlpineMath.doubleExpression(Math.exp(priors[i]))).append(Tools.getLineSeparator());
		}
		printProperty(model,buffer,numberOfClasses);
 		
		if (model.isCalculateDeviance()  )
		{
			double nullDeviance=model.getNullDeviance();
			double deviance=model.getDeviance();
			buffer.append("null Deviance:").append(AlpineMath.doubleExpression(nullDeviance)).append(Tools.getLineSeparator()).append(
					"deviance:").append(AlpineMath.doubleExpression(deviance)).append(Tools.getLineSeparator());
		}
		return buffer.toString();
		
	}

	/**
	 * @param model
	 * @param buffer
	 * @param classValues
	 * @param numberOfClasses
	 */
	private void printProperty(NBModel model,
			StringBuffer buffer,   int numberOfClasses) {
		String[] classValues = model.getClassValues();
		double[][][] distributionProperties=model.getDistributionProperties();
		String[] attributeNames=model.getColumneNames();
		boolean[] nominal = model.getNominal();
		String[][] attributeValues = model.getColumnValues();
		double[][][] weightSums = model.getWeightSums();
		buffer.append(Tools.getLineSeparators(1));
		for (int i = 0; i < model.getNumberOfColumns(); i++) {
			TextTable table= new TextTable();
			buffer.append("attribute \"").append(attributeNames[i]).append("\" distributionProperty:").append(Tools.getLineSeparator());
			
			//class---        attributeValues[i][k]----
			//classValues[j]       (Math.exp(distributionProperties[i][j][k]))--
 			if (nominal[i]) {
 			
 				//class  weightSums[i][j].length
 				//
 				int colNo=weightSums[i][0].length;
 				String[] header =new String[colNo];
 				header[0]="Class";
 				for (int k = 0; k < weightSums[i][0].length - 1; k++) {
 					header[k+1]=attributeValues[i][k];
 				}
 				//====================
 				table.addLine(header);
 				
 				for (int j = 0; j < numberOfClasses; j++) {
 					String []line=new String[colNo];
 					line[0]= classValues[j] ;
					for (int k = 0; k < weightSums[i][j].length - 1; k++) {
						line[k+1]= 	String.valueOf( Math.exp(distributionProperties[i][j][k]));
					}
					table.addLine(line);
//					buffer.append(dataTools.getLineSeparator());
				}
 			
 				
 				/**
				for (int j = 0; j < numberOfClasses; j++) {
					buffer.append("\tclass: "+classValues[j]).append(dataTools.getLineSeparator());//.append("\t");
					for (int k = 0; k < weightSums[i][j].length - 1; k++) {
						buffer.append("\tdistributionProperty(").append(attributeValues[i][k]).append("): ").append(
								dataTools.formatNumber(Math.exp(distributionProperties[i][j][k])));
					}
					buffer.append(dataTools.getLineSeparator());
				}*/
			} else {
				
				String[] header =new String[]{"Class","Mean","Standard Deviantion"};
				table.addLine(header);
				for (int j = 0; j < numberOfClasses; j++) {
					String [] line=new String[3];
					table.addLine(line);
					line[0]=classValues[j];
					line[1]= 
							String.valueOf(distributionProperties[i][j][model.INDEX_MEAN]) ;
					line[2]= 
						String.valueOf(distributionProperties[i][j][model.INDEX_STANDARD_DEVIATION]);
				 
				}
			
			}
 			buffer.append(table.toTableString());
 			buffer.append(Tools.getLineSeparator());
	 
		}
	}
}
