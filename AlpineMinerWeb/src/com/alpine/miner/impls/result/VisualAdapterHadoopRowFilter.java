/**
 * 
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.hadoop.ext.CSVRecordParser;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelEmpty;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;

/**
 * ClassName: VisualAdapterHadoopRowFilter.java
 * <p/>
 * Data: 2012-6-13
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class VisualAdapterHadoopRowFilter extends AbstractOutPutVisualAdapter
		implements OutPutVisualAdapter {

	public static final VisualAdapterHadoopRowFilter INSTANCE = new VisualAdapterHadoopRowFilter();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.impls.result.OutPutVisualAdapter#toVisualModel(com.alpine
	 * .datamining.api.AnalyticOutPut, java.util.Locale)
	 */
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws Exception {
		HadoopMultiAnalyticFileOutPut hadoopOutput = (HadoopMultiAnalyticFileOutPut) analyzerOutPut;

		String[] fileNameArray = hadoopOutput.getOutputFileNames();
		AnalysisFileStructureModel fileStructure = hadoopOutput.getHadoopFileStructureModel();
		List<String[]> fileContentArray = hadoopOutput.getOutputFileSampleContents();
		String nodeName = analyzerOutPut.getAnalyticNode().getName();

		List<VisualizationModel> models = new ArrayList<VisualizationModel>();

		String spliter = HadoopUtility.getDelimiterValue(fileStructure);// get spliter from structure.
        if(null!=fileNameArray){
            for(int i = 0;i < fileNameArray.length;i++){
                String mashupFileName = fileNameArray[i];
                String[] sampleContent = fileContentArray.get(i);
                VisualizationModel vm = null;
                if(null!=spliter && "".equals(spliter)){
                    vm = generateTextModel(mashupFileName, sampleContent);
                }else{
                    vm = generateTableModel(mashupFileName, sampleContent, fileStructure, spliter,hadoopOutput);
                }
                models.add(vm);
            }
            return new VisualizationModelComposite(nodeName, models);
        }

        return new VisualizationModelEmpty(nodeName);
	}

	private VisualizationModel generateTextModel(String mashupFileName, String[] content) {
		StringBuilder buffer = new StringBuilder();
		for(String line : content){
			buffer.append(line)
					.append("\n");
		}
		return new VisualizationModelText(mashupFileName, buffer.toString());
	}
	
	private VisualizationModel generateTableModel(String mashupFileName, 
													String[] content, 
													AnalysisFileStructureModel structure,
													String spliter,HadoopMultiAnalyticFileOutPut hadoopOutput){
		DataTable dataTable = new DataTable();
		dataTable.setColumns(transformTableMetadata(structure));
		dataTable.setRows(transformTableDataRow(content, spliter,  hadoopOutput));
		
		
		return new VisualizationModelDataTable(mashupFileName, dataTable);
	}
	
	private List<TableColumnMetaInfo> transformTableMetadata(AnalysisFileStructureModel structure){
		List<String> columnNames = structure.getColumnNameList(),
					 columnTypes = structure.getColumnTypeList();
		List<TableColumnMetaInfo> metadataSet = new ArrayList<TableColumnMetaInfo> ();
		for(int i = 0;i < columnNames.size();i++){
			metadataSet.add(new TableColumnMetaInfo(columnNames.get(i), columnTypes.get(i)));
		}
		return metadataSet;
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
	
	
	private List<DataRow> transformTableDataRow(String[] rows, String spliter,  HadoopMultiAnalyticFileOutPut analyzerOutPut){
		List<DataRow> dataRows = new ArrayList<DataRow> ();
	 
        int startIdx = analyzerOutPut.getStartIndex();

        CSVRecordParser csvParser =  createCSVParser(analyzerOutPut );
		if(csvParser==null){//no quote char and escaper char
			while(startIdx < rows.length){
				dataRows.add(new DataRow(rows[startIdx].split(spliter)));
	            startIdx = startIdx + 1 ;
			}
		}else{
			while(startIdx < rows.length){
				try{
					dataRows.add(new DataRow(csvParser.parseLine(rows[startIdx]) ));
				}catch(Exception e){
						//just ignore this line
					 
				}
		        startIdx = startIdx + 1 ;
			}
			
		}
		
		
		return dataRows;
	}

}
