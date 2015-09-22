/**
 * ClassName FileStructureModelUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-26
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;

/**
 * @author Jeff Dong
 *
 */
public class FileStructureModelUtility {

 
	public static void fillCommonXMLElement(FileStructureModel source,Document xmlDoc, Element element) {
		element.setAttribute(FileStructureModel.ATTR_INCLUDEHEADER, source.getIsFirstLineHeader());
		
		if(source.getColumnNameList()!=null){
			for(String s:source.getColumnNameList()){
				Element columnNameEle=xmlDoc.createElement(FileStructureModel.COLUMNNAMES_TAG_NAME);
				columnNameEle.setAttribute(FileStructureModel.ATTR_COLUMNNAME, s);
				element.appendChild(columnNameEle);
			}	
		}
		
		if(source.getColumnTypeList()!=null){
			for(String s:source.getColumnTypeList()){
				Element columnTypeEle=xmlDoc.createElement(FileStructureModel.COLUMNTYPES_TAG_NAME);
				columnTypeEle.setAttribute(FileStructureModel.ATTR_COLUMNTYPE, s);
				element.appendChild(columnTypeEle);
			}	
		}
	}

	/**
	 * @param operatorInputFileInfo
	 */
	public static void switchFileStructureModel(
			OperatorInputFileInfo operatorInputFileInfo) {
		FileStructureModel fileStructureModel = operatorInputFileInfo.getColumnInfo(); 
		//all types need to be transformed to standard CSV structure.
		CSVFileStructureModel newFileStructureModel = new CSVFileStructureModel();
		newFileStructureModel.setDelimiter(AnalysisCSVFileStructureModel.DELIMITER[1]);
		newFileStructureModel.setOther("");
		newFileStructureModel.setEscapChar(AnalysisCSVFileStructureModel.ESCAP_VALUE);
		newFileStructureModel.setQuoteChar(AnalysisCSVFileStructureModel.QUOTE_VALUE);
		if(fileStructureModel!=null){
			newFileStructureModel.setColumnNameList(fileStructureModel.getColumnNameList());
			newFileStructureModel.setColumnTypeList(fileStructureModel.getColumnTypeList()) ;
		}
		operatorInputFileInfo.setColumnInfo(newFileStructureModel) ;	
	}

	 
}
