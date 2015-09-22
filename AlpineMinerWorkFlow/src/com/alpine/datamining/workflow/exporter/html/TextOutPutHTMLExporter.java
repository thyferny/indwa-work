/**
 * ClassName  DataTableHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.GoodnessOfFitTextVisualizationType;
import com.alpine.datamining.api.impl.visual.LinearRegressionTextVisualizationType;
import com.alpine.datamining.api.impl.visual.LogisticRegressionTextVisualizationType;
import com.alpine.datamining.api.impl.visual.NaiveBayesTextVisualizationType;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.impl.visual.TextVisualizationOutPut;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.datamining.operator.evaluator.ValueGoodnessOfFit;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.operator.regressions.LoRModelIfc;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.utility.Tools;
import com.alpine.datamining.workflow.util.ToHtmlWriter;


/**
 * @author John Zhao
 *
 */
public class TextOutPutHTMLExporter implements VisualOutPutHTMLExporter {
	

	private static final int MAX_COL_NUM = 6;
	private static LogisticRegressionTextVisualizationType logisticRegressionTextVisualizationType = new LogisticRegressionTextVisualizationType();

	/* (non-Javadoc)
	 * @see com.alpine.datamining.exporter.VisualOutPutExporter#export(com.alpine.datamining.api.VisualizationOutPut)
	 */
	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut, List<String> tempFileList,
			String rootPath) throws  Exception {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		AnalyticOutPut realout = visualizationOutPut.getAnalyzer().getOutPut();
		if(visualizationOutPut.getVisualizationType() 
				instanceof GoodnessOfFitTextVisualizationType){
			List<GoodnessOfFit> list=(	List<GoodnessOfFit>)( (GoodnessOfFitOutPut)realout).getResultList();
			
			createGoFTable(list, htmlWriter);
	 
		}else if (visualizationOutPut.getVisualizationType() 
				instanceof NaiveBayesTextVisualizationType){
			addNBResult(htmlWriter,visualizationOutPut.getAnalyzer().getOutPut());
		}
		else if (visualizationOutPut.getVisualizationType() 
				instanceof LinearRegressionTextVisualizationType){
			addLinearResult(htmlWriter,visualizationOutPut.getAnalyzer().getOutPut());
		}
		else if (visualizationOutPut.getVisualizationType() 
				instanceof LogisticRegressionTextVisualizationType){
			addLRResult(htmlWriter,visualizationOutPut.getAnalyzer().getOutPut());
		}
		else{
			TextVisualizationOutPut out= (TextVisualizationOutPut)visualizationOutPut;
			htmlWriter.writeH2(out.getVisualizationObject().toString());
//			result.append("<h2>").append(out.getVisualizationObject()).append("</h2>");

			
		}	
	
		return htmlWriter.toStringBuffer();
	}

	/**
	 * @param result
	 * @param outPut
	 * @throws Exception 
	 */
	private void addLRResult(ToHtmlWriter htmlWriter, AnalyticOutPut outPut) throws Exception {

		
		if(((AnalyzerOutPutTrainModel)outPut).getEngineModel().getModel() instanceof LogisticRegressionGroupModel){
			Map<String, LogisticRegressionModelDB> modelListMap = ((LogisticRegressionGroupModel)((AnalyzerOutPutTrainModel)outPut).getEngineModel().getModel() ).getModelList();
			if(modelListMap!=null){
				Iterator<Entry<String, LogisticRegressionModelDB>> iter = modelListMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, LogisticRegressionModelDB> entry = iter.next();
					StringBuffer buffer=logisticRegressionTextVisualizationType .getVTextText(entry.getValue());
					htmlWriter.writeH2(buffer.toString());
				}
			}
		}else{
			 LoRModelIfc model=(LoRModelIfc) 
						((AnalyzerOutPutTrainModel)outPut).getEngineModel().getModel();
			StringBuffer buffer=logisticRegressionTextVisualizationType .getVTextText(model);
			htmlWriter.writeH2(buffer.toString());
		}
		
	}

	/**
	 * @param result 
	 * @param outPut
	 * @throws Exception 
	 */
	private void addLinearResult(ToHtmlWriter htmlWriter, AnalyticOutPut outPut) throws Exception {
 		LinearRegressionModelDB model=(LinearRegressionModelDB) 
		((AnalyzerOutPutTrainModel)outPut).getEngineModel().getModel();
		
		StringBuffer buffer=LinearRegressionTextVisualizationType.getVtextText(model) ;
		htmlWriter.writeH2(buffer.toString());
//		result.append("<h2>").append(buffer).append("</h2>");
		
//		TextTable table = LinearRegressionTextVisualizationType.getVTextTable(model);
//		drawTextTable(table,htmlWriter);
		
	}

	/**
	 * @param result
	 * @param outPut
	 */
	private void addNBResult(ToHtmlWriter htmlWriter, AnalyticOutPut outPut)   throws  Exception {
		
		StringBuffer buffer= new StringBuffer();
		NBModel model=(NBModel) 
		((AnalyzerOutPutTrainModel)outPut).getEngineModel().getModel();
		String[] classValues = model.getClassValues();
		
		
	
		

		double[] priors=model.getPriors();
		buffer.append(Tools.getLineSeparator());
		buffer.append("class priors").append(Tools.getLineSeparator());
		int numberOfClasses=model.getNumberOfClasses();
		for (int i = 0; i <numberOfClasses; i++) {
			
			buffer.append("      priors("+classValues[i]+"):").append(String.valueOf(Math.exp(priors[i]))).append(Tools.getLineSeparator());
		}
	//here the table........
//		result.append("<h2>").append(buffer.toString()).append("</h2>");
		htmlWriter.writeH2(buffer.toString());
	
		 addNBProperty(model,htmlWriter);
 
			if (model.isCalculateDeviance()   )
			{	buffer=new StringBuffer();
				double nullDeviance=model.getNullDeviance();
				double deviance=model.getDeviance();
				buffer.append(Tools.getLineSeparator());
				buffer.append("null Deviance:").append(String.valueOf(nullDeviance)).append(Tools.getLineSeparator()).append(
						"deviance:").append(String.valueOf(deviance)).append(Tools.getLineSeparator());
			}
//		result.append("<h2>").append(buffer.toString()).append("</h2>");
		htmlWriter.writeH2(buffer.toString());
		
	}

	/**
	 * @param model
	 * @param result
	 * @throws Exception 
	 */
	private void addNBProperty(NBModel model,
			ToHtmlWriter htmlWriter) throws Exception {
		int numberOfClasses = model.getNumberOfClasses();
	
		String[] classValues = model.getClassValues();
		double[][][] distributionProperties=model.getDistributionProperties();
		String[] attributeNames=model.getColumneNames();
		boolean[] nominal = model.getNominal();
		String[][] attributeValues = model.getColumnValues();
		double[][][] weightSums = model.getWeightSums();
	
		for (int i = 0; i < model.getNumberOfColumns(); i++) {
		
			StringBuffer buffer = new StringBuffer();
			buffer.append(Tools.getLineSeparator());
			buffer.append("attribute ").append(attributeNames[i]).append(
					" distributionProperty:").append(Tools.getLineSeparator());
			htmlWriter.writeH2(buffer.toString());
//			result.append("<h2>").append(buffer.toString()).append("</h2>");
			
			TextTable table= new TextTable();
			if (nominal[i]) {
  			
 				//class  weightSums[i][j].length
 				//
 				int colNo=weightSums[i][0].length;
 				String [] line=new String[colNo];
 				line[0]="Class";
 				 
 		  
 				for (int k = 0; k < weightSums[i][0].length - 1; k++) {
 					line[k+1]=attributeValues[i][k];
 				}
 				table.addLine(line);
 		 		//====================
  				
 				for (int j = 0; j < numberOfClasses; j++) {
 					 line=new String[colNo];
 					line[0]=classValues[j];
 					
					for (int k = 0; k < weightSums[i][j].length - 1; k++) {
						line[k+1]=String.valueOf(Math.exp(distributionProperties[i][j][k]));
						
					}
					table.addLine(line);
				}
 			 
 				
 			 
			} else {
				table.addLine(new String[]{"Class","Mean","Standard Deviantion"});
				for (int j = 0; j < numberOfClasses; j++) {
					String [] line=new String[3];
				 
					line[0]=classValues[j];
					line[1]=String.valueOf(
							distributionProperties[i][j][NBModel.INDEX_MEAN]);
					line[2]=String.valueOf(
							distributionProperties[i][j][NBModel.INDEX_STANDARD_DEVIATION]);
					table.addLine(new String[]{line[0],line[1],line[2]});
				}
			}
 
 			drawTextTable(table,htmlWriter);
	 
		}
		
	}

	/**
	 * @param table
	 * @param result
	 * @throws  Exception 
	 */
	private void drawTextTable(TextTable table, ToHtmlWriter htmlWriter) throws  Exception {
		int colNumbers=table.getColumnNumbers();
		if(colNumbers>MAX_COL_NUM){
			List<TextTable> tables=splitTable(table);
			for(int i=0;i<tables.size();i++){
				drawTextTable(tables.get(i),htmlWriter);
			}
		}else{
			//here draw a table...
			
			int rowNumbers=table.getLines().size();
			//zy:avoid the bad table exception (no columns,no rows)
			if(rowNumbers>0&&colNumbers>0){
				ToHtmlWriter tablWriter=new ToHtmlWriter();
//				t.append("<table>");
				addColumnHeader(table.getLines().get(0),tablWriter);
				for(int i=1;i<rowNumbers;i++){
					addRow(table.getLines().get(i),tablWriter);
				}
//				t.append("</table>");
				htmlWriter.writeTable(tablWriter.toString());
			}	
		}
	}
	
	/**
	 * @param strings
	 * @param t
	 * @throws BadElementException 
	 */
	private void addRow(String[] row, ToHtmlWriter htmlWriter)  {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<tr>");
		 for (int i = 0; i < row.length; i++) {
			 if (row[i] != null)
//				t.append("<td>").append(row[i]).append("</td>)");	
			 	tdWriter.writeTD(row[i]);
			 else 
//				t.append("<td>").append("").append("</td>)");
				 tdWriter.writeTD("");

		 }
//		 t.append("</tr>");
		 htmlWriter.writeTR(tdWriter.toString());
		
	}

	private void addColumnHeader(String[] columns, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<tr>");
		for (int i = 0; i < columns.length; i++) {
//			t.append("<td>").append(columns[i]).append("</td>)");		
			tdWriter.writeTD(columns[i]);
		}
//		t.append("</tr>");
		htmlWriter.writeTR(tdWriter.toString());
		
	}

	/**
	 * @param table
	 * @return
	 */
	private List<TextTable> splitTable(TextTable table) {
		List<TextTable>  tabls=new ArrayList<TextTable>();
		int colNum=table.getColumnNumbers();
		if(colNum>MAX_COL_NUM){
			int steps=1+(colNum/MAX_COL_NUM);
			for(int i=0;i<steps;i++){
				TextTable subTable=getTable(table,i);
				tabls.add(subTable);
			}
				
		}else{
			tabls.add(table);
		}
		return tabls;
	}

	/**
	 * @param table
	 * @param numbers
	 * @return
	 */
	private TextTable getTable(TextTable table, int step) {
		TextTable subTable= new TextTable();
		List<String[]> lines= table.getLines();
		int startIndex=step*MAX_COL_NUM;
		int colMum=MAX_COL_NUM;
		if(	table.getColumnNumbers()<startIndex+MAX_COL_NUM){
			colMum=table.getColumnNumbers()-startIndex;
		}
		for(int j=0;j<lines.size();j++){
			
			String[] line= new String[colMum];
			for(int i=0; i<colMum;i++){	
				line[i]=lines.get(j)[i+startIndex];
				
			}
			subTable.addLine(line);
		}	
	
		return subTable;
	}

	/**
	 * @param list
	 * @return
	 * @throws BadElementException 
	 */
	private StringBuffer createGoFTable(List<GoodnessOfFit> list, ToHtmlWriter htmlWriter) {

		for(int i =0;i<list.size();i++){

			GoodnessOfFit gft=list.get(i);
			StringBuffer sb= new StringBuffer();
			
			String name=gft.getSourceName();
			sb.append(name);
			 double accu = gft.getAccuracy();
			 sb.append("  Accuracy:"+String.valueOf(accu));
			double error = gft.getError();
			sb.append("  Error:"+String.valueOf(error)+"\n");
			htmlWriter.writeH2(sb.toString());
//			table.append("<h2>").append(sb).append("</h2>");

			ArrayList<ValueGoodnessOfFit> gds = gft.getGoodness();
			createGoFTable(gds, htmlWriter);

			 
			
		}
		return htmlWriter.toStringBuffer();
	}

	/**
	 * @param gds
	 * @return
	 * @throws BadElementException 
	 */
	private void createGoFTable(ArrayList<ValueGoodnessOfFit> gds, ToHtmlWriter htmlWriter) {
		
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		tdWriter.writeTD("Stats");
		tdWriter.writeTD("Recall");
		tdWriter.writeTD("Precision");
		tdWriter.writeTD("F1");
		tdWriter.writeTD("Specificity:");
		tdWriter.writeTD("Sensitivity:");
		ToHtmlWriter trWriter=new ToHtmlWriter();
		trWriter.writeTR(tdWriter.toString());
		htmlWriter.writeTable(trWriter.toString());
		
//		table.append("<table border=3>");
//		table.append("<tr>");
//		table.append("<td>").append("Stats").append("</td>");
//		table.append("<td>").append("Recall").append("</td>");
//		table.append("<td>").append("Precision").append("</td>");
//		table.append("<td>").append("F1").append("</td>");
//		table.append("<td>").append("Specificity:").append("</td>");
//		table.append("<td>").append("Sensitivity:").append("</td>");
//		table.append("</tr>");
		
		for (int i = 0; i < gds.size(); i++) {
			addRow(gds.get(i),htmlWriter);
		}
		
//		table.append("</table>");
	}

	/**
	 * @param valueGoodnessOfFit
	 * @param t
	 */
	private void addRow(ValueGoodnessOfFit vof, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		tdWriter.writeTD(vof.getValue());
		tdWriter.writeTD(String.valueOf( vof.getRecall()));
		tdWriter.writeTD(String.valueOf( vof.getPrecision()));
		tdWriter.writeTD(String.valueOf( vof.getF1()));
		tdWriter.writeTD(String.valueOf( vof.getSpecificity()));
		tdWriter.writeTD(String.valueOf( vof.getSensitivity()));
		htmlWriter.writeTR(tdWriter.toString());
//		t.append("<tr>");
//		t.append("<td>").append(vof.getValue()).append("</td>");
//		t.append("<td>").append(String.valueOf( vof.getRecall())).append("</td>");
//		t.append("<td>").append(String.valueOf( vof.getPrecision())).append("</td>");
//		t.append("<td>").append(String.valueOf( vof.getF1())).append("</td>");
//		t.append("<td>").append(String.valueOf( vof.getSpecificity())).append("</td>");
//		t.append("<td>").append(String.valueOf( vof.getSensitivity())).append("</td>");
//		t.append("</tr>");
		
	}

}
