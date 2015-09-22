/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.univariate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 *
 */

 

public class UnivariateModel extends AbstractParameterObject{
	public static final String TAG_NAME="UnivariateModel";
	
	public static final String REFERENCE_COLUMN_TAG_NAME="ReferenceColumn";
	
	public static final String ANALYSIS_COLUMN_TAG_NAME="AnalysisColumns";
	
	public static final String ATTR_COLUMNNAME = "columnName";
	
	private String referenceColumn;
	
	private List<String> analysisColumns;
	 
	 
	public UnivariateModel(String referenceColumn,List<String> analysisColumns){ 
		this.referenceColumn=referenceColumn;
		this.analysisColumns=analysisColumns;
	}
	
	public UnivariateModel( ){

	}
		
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof UnivariateModel){	 
				return  ListUtility.equalsIgnoreOrder(analysisColumns,
						((UnivariateModel)obj).getAnalysisColumns())&&
						((UnivariateModel)obj).getReferenceColumn().equals(referenceColumn);
		}else{
			return false;
		}
	
	}
  
	/** 
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getAnalysisColumns()!=null){
			for (Iterator<String> iterator = getAnalysisColumns().iterator(); iterator.hasNext();) {
				String item = iterator.next();
				if(item!=null){
					Element analysisColumnsEle=xmlDoc.createElement(ANALYSIS_COLUMN_TAG_NAME);
					analysisColumnsEle.setAttribute(ATTR_COLUMNNAME, item);
					element.appendChild(analysisColumnsEle);
				}			
			}
		}
		if(!StringUtil.isEmpty(getReferenceColumn())){
			Element referenceColumnsEle=xmlDoc.createElement(REFERENCE_COLUMN_TAG_NAME);
			referenceColumnsEle.setAttribute(ATTR_COLUMNNAME, getReferenceColumn());
			element.appendChild(referenceColumnsEle);
		}
		return element;
	}

	 
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public UnivariateModel clone()  throws CloneNotSupportedException {
		UnivariateModel model= new UnivariateModel();
		if(this.getAnalysisColumns()!=null){
			model.setAnalysisColumns(ParameterUtility.cloneObjectList(analysisColumns));
		}
		if(!StringUtil.isEmpty(getReferenceColumn())){
			model.setReferenceColumn(getReferenceColumn());
		}
	 return model;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public static UnivariateModel fromXMLElement(Element element) {
		List<String> analysisColumnsItems = new ArrayList<String>();
 		NodeList analysisColumnItemList = element.getElementsByTagName(ANALYSIS_COLUMN_TAG_NAME);
		for (int i = 0; i < analysisColumnItemList.getLength(); i++) {
			if (analysisColumnItemList.item(i) instanceof Element ) {
				String analysisColumn=((Element)analysisColumnItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
				analysisColumnsItems.add(analysisColumn);
			}
		}
		String referenceColumn=null;
		NodeList referenceColumnItemList = element.getElementsByTagName(REFERENCE_COLUMN_TAG_NAME);
		for (int i = 0; i < referenceColumnItemList.getLength(); i++) {
			if (referenceColumnItemList.item(i) instanceof Element ) {
				referenceColumn=((Element)referenceColumnItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
			}
		}
		UnivariateModel model=new UnivariateModel(referenceColumn,analysisColumnsItems);
		return model;
	}

	public String getReferenceColumn() {
		return referenceColumn;
	}

	public void setReferenceColumn(String referenceColumn) {
		this.referenceColumn = referenceColumn;
	}

	public List<String> getAnalysisColumns() {
		return analysisColumns;
	}

	public void setAnalysisColumns(List<String> analysisColumns) {
		this.analysisColumns = analysisColumns;
	}
	
	public void addAnalysisColumn(String analysisColumn){
		if(analysisColumns==null){
			analysisColumns=new ArrayList<String>();
		}
		analysisColumns.add(analysisColumn);
	}
}
