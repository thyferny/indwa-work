/**
 * ClassName AbstractFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-26
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop.filestructure;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;


/**
 * @author Jeff Dong
 *
 */
public abstract class AbstractFileStructureModel implements
		AnalysisFileStructureModel {


	
	protected String isFirstLineHeader = Resources.FalseOpt; 
	protected List<String> columnNameList;
	protected List<String> columnTypeList;
 
	@Override
	public String getIsFirstLineHeader() {
		return isFirstLineHeader;
	}

	@Override
	public void setIsFirstLineHeader(String isFirstLineHeader) {
		this.isFirstLineHeader = isFirstLineHeader;
		
	}

	@Override
	public List<String> getColumnNameList() {
		return columnNameList;
	}

	@Override
	public void setColumnNameList(List<String> columnNameList) {
		this.columnNameList=columnNameList;
		
	}

	@Override
	public List<String> getColumnTypeList() {
		return columnTypeList;
	}

	@Override
	public void setColumnTypeList(List<String> columnTypeList) {
		this.columnTypeList=columnTypeList;
		
	}
	
	protected void cloneCommonField(AnalysisFileStructureModel model)
			throws CloneNotSupportedException {
		List<String> clone = ListUtility.cloneStringList(getColumnNameList());
		model.setColumnNameList(clone);
		clone = ListUtility.cloneStringList(getColumnTypeList());
		model.setColumnTypeList(clone);
		model.setIsFirstLineHeader(isFirstLineHeader);
	}
	
	protected boolean equals(AnalysisFileStructureModel obj){
		return ListUtility.equalsFocusOrder(columnNameList,
				(( AnalysisFileStructureModel)obj).getColumnNameList())
				&&ListUtility.equalsFocusOrder(columnTypeList,
						((AnalysisFileStructureModel)obj).getColumnTypeList())
						&&StringUtil.safeEquals(isFirstLineHeader, ((AnalysisFileStructureModel)obj).getIsFirstLineHeader() ) ;
	} 
	
	protected static void fillColumnElements(Element element,
			AnalysisFileStructureModel model) {
		NodeList columnNameItemList = element.getElementsByTagName(COLUMNNAMES_TAG_NAME);
		List<String> columnNameList=new ArrayList<String>();
		for (int i = 0; i < columnNameItemList.getLength(); i++) {
			if (columnNameItemList.item(i) instanceof Element ) {
				String columnName=((Element)columnNameItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
				columnNameList.add(columnName);
			}
		}
		
		NodeList columnTypeItemList = element.getElementsByTagName(COLUMNTYPES_TAG_NAME);
		List<String> columnTypeList=new ArrayList<String>();
		for (int i = 0; i < columnTypeItemList.getLength(); i++) {
			if (columnTypeItemList.item(i) instanceof Element ) {
				String columnType=((Element)columnTypeItemList.item(i)).getAttribute(ATTR_COLUMNTYPE);
				columnType =HadoopDataType.getTransferDataType(columnType) ;
				columnTypeList.add(columnType);
			}
		}
		model.setColumnNameList(columnNameList);
		model.setColumnTypeList(columnTypeList);
	}
	
	public 	abstract AnalysisFileStructureModel clone() throws CloneNotSupportedException;

}
