/**
 * ClassName HadoopPRTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.hadoop.ext.CSVRecordParser;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Jeff Dong
 *
 */
public class HadoopMultiOutputTableVisualizationType extends TableVisualizationType implements
VisualizationType {

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationType#generateOutPut(com.alpine.datamining.api.AnalyticOutPut)
	 */
	private int index=0;
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalysisFileStructureModel hdfsm = null;
		String [] outputFileNames = null;
		List<String[]> outputFileSampleContents = null;
		if(analyzerOutPut instanceof HadoopMultiAnalyticFileOutPut){
			hdfsm = ((HadoopMultiAnalyticFileOutPut)analyzerOutPut).getHadoopFileStructureModel();
			outputFileNames=((HadoopMultiAnalyticFileOutPut)analyzerOutPut).getOutputFileNames();
		 outputFileSampleContents = ((HadoopMultiAnalyticFileOutPut)analyzerOutPut).getOutputFileSampleContents();
		}
		

		String[] lines = new String[]{};
		if(outputFileSampleContents!=null&&outputFileSampleContents.size()>0){
			lines = outputFileSampleContents.get(index); 
		}
		String delimiter = HadoopUtility.getDelimiterValue(hdfsm);
		
		if(delimiter.equals(AnalysisCSVFileStructureModel.DELIMITER_VALUE[0])
				||StringUtil.isEmpty(delimiter)==false){
			return genereateTableOutput(((HadoopMultiAnalyticFileOutPut)analyzerOutPut), hdfsm, outputFileNames,
					lines, delimiter);
		}else{
			return generateTextOutput(analyzerOutPut,lines,outputFileNames);
		}
	}

	private VisualizationOutPut generateTextOutput(AnalyticOutPut analyzerOutPut,String[] lines,
			String[] outputFileNames) {
		
		TableEntity te = new TableEntity(); 
		 
		te.setColumn(new String[]{"Column"}) ;
		
		for (int i = 0; i < lines.length; i++) {
			te.addItem(new String[]{lines[i]}) ;
		}
		
		
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(te);
		
		if(StringUtil.isEmpty(outputFileNames[index])){
			output.setName(analyzerOutPut.getAnalyticNode().getName());
		}else{
			output.setName(analyzerOutPut.getAnalyticNode().getName()+"/"+outputFileNames[index]);
		}
		
		
		return output;
	}
	
	private VisualizationOutPut genereateTableOutput(
			HadoopMultiAnalyticFileOutPut analyzerOutPut,
			AnalysisFileStructureModel hdfsm, String[] outputFileNames,
			String[] lines, String delimiter) {
		TableEntity te = new TableEntity(); 
		 
		te.setColumn(hdfsm.getColumnNameList().toArray(new String[hdfsm.getColumnNameList().size()])) ;
		for(int i=0;i<hdfsm.getColumnNameList().size();i++){
			te.addSortColumn(hdfsm.getColumnNameList().get(i), hdfsm.getColumnTypeList().get(i));
		}
		
		if(lines!=null&&lines.length>0){
			int start =(analyzerOutPut).getStartIndex();
			//JIRA MINER-2102:show result need not think about the include header
//			if(hdfsm.getIncludeHeader().equals(Resources.TrueOpt)){
//				start =1;
//			}
			CSVRecordParser csvParser = createCSVParser(analyzerOutPut);
			
			if(csvParser==null){
				for(int i =start;i<lines.length;i++){
					te.addItem(lines[i].split(delimiter)) ;
				}
			}else{//need use csv parser...
				for(int i =start;i<lines.length;i++){
					try{
						
						te.addItem(csvParser.parseLine(lines[i])) ;
					
					}catch(Exception e){
						//just ignore this line
					}
				}
			}
		}
		te.setSystem(HadoopDataType.HADOOP);
		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(te);
		
		if(StringUtil.isEmpty(outputFileNames[index])){
			output.setName(analyzerOutPut.getAnalyticNode().getName());
		}else{
			output.setName(analyzerOutPut.getAnalyticNode().getName()+"/"+outputFileNames[index]);
		}
		
		return output;
	}

	private CSVRecordParser createCSVParser(
			HadoopMultiAnalyticFileOutPut analyzerOutPut) {
		CSVRecordParser csvParser = null;
		AnalysisCSVFileStructureModel fsModel = (AnalysisCSVFileStructureModel)analyzerOutPut.getHadoopFileStructureModel();
		if(fsModel==null){
			return null;
		}
		
		if(fsModel.getQuoteChar()!=null&&StringUtil.isEmpty(fsModel.getDelimiter())==false&&fsModel.getEscapChar() !=null){
			String quoteChar = fsModel.getQuoteChar();
			if(quoteChar==""){
				quoteChar="\"" ;
			}
			String escChar = fsModel.getEscapChar();
			if(escChar==""){
				escChar="\\" ;
			}
			csvParser= new CSVRecordParser(HadoopUtility.getDelimiterValue(fsModel).charAt(0), 
					quoteChar.charAt(0), escChar.charAt(0));
		}
		return csvParser;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	
}
