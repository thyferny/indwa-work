/**
 * ClassName HadoopJoinModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopjoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class HadoopJoinModel extends AbstractParameterObject {

	public static final String TAG_NAME = "HadoopJoinDefinition";
	
	public static final String JOIN_TYPE_TAG_NAME = "joinType";
	
	public static final String JOIN_TYPE_VALUE = "joinTypeValue";
	
	private List<HadoopJoinFile> joinTables = new ArrayList<HadoopJoinFile>();
	
	private List<HadoopJoinColumn> joinColumns = new ArrayList<HadoopJoinColumn>();
	
	private List<HadoopJoinCondition> joinConditions = new ArrayList<HadoopJoinCondition>();
	
	private String joinType;
	
	/**
	 * @param joinTabls
	 * @param joinColumns
	 * @param joinConditions
	 */
	public HadoopJoinModel(List<HadoopJoinFile> joinTables,
			List<HadoopJoinColumn> joinColumns, 
			List<HadoopJoinCondition> joinConditions,String joinType) {
		this.joinTables = joinTables;
		this.joinColumns = joinColumns;
		this.joinConditions = joinConditions;
		this.joinType=joinType;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		List<HadoopJoinFile> newJoinTables = new ArrayList<HadoopJoinFile>();
		for (HadoopJoinFile joinTable : joinTables) {
			newJoinTables.add((HadoopJoinFile) joinTable.clone());
		}
		List<HadoopJoinColumn> newJoinColumns = new ArrayList<HadoopJoinColumn>();
		for (HadoopJoinColumn joinColumn : joinColumns) {
			newJoinColumns.add((HadoopJoinColumn) joinColumn.clone());
		}
		List<HadoopJoinCondition> newJoinConditions = new ArrayList<HadoopJoinCondition>();
		for (HadoopJoinCondition joinCondition : joinConditions) {
			newJoinConditions.add((HadoopJoinCondition) joinCondition.clone());
		}
		return new HadoopJoinModel(newJoinTables, newJoinColumns,
				newJoinConditions,joinType);
	}
	
	public Element toXMLElement(Document xmlDoc) {

		Element ele = xmlDoc.createElement(TAG_NAME);
		if (getJoinTables() != null) {
			for (Iterator<HadoopJoinFile> iterator = getJoinTables().iterator(); iterator
					.hasNext();) {
				HadoopJoinFile joinTable = iterator.next();
				if (joinTable != null) {
					Element joinTableElement = joinTable.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}

		if (getJoinColumns() != null) {
			for (Iterator<HadoopJoinColumn> iterator = getJoinColumns().iterator(); iterator
					.hasNext();) {
				HadoopJoinColumn joinColumn = iterator.next();
				if (joinColumn != null) {
					Element joinTableElement = joinColumn.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}

		if (getJoinConditions() != null) {
			for (Iterator<HadoopJoinCondition> iterator = getJoinConditions()
					.iterator(); iterator.hasNext();) {
				HadoopJoinCondition joinCondition = iterator.next();
				if (joinCondition != null) {
					Element joinTableElement = joinCondition
							.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}
		
		if(StringUtil.isEmpty(getJoinType())==false){
			Element element=xmlDoc.createElement(JOIN_TYPE_TAG_NAME);
			element.setAttribute(JOIN_TYPE_VALUE, getJoinType());
			ele.appendChild(element);
		}
		return ele;
	}
	
	public static HadoopJoinModel fromXMLElement(Element element) {
		List<HadoopJoinFile> joinTables = new ArrayList<HadoopJoinFile>();

		List<HadoopJoinColumn> joinColumns = new ArrayList<HadoopJoinColumn>();

		List<HadoopJoinCondition> joinConditions = new ArrayList<HadoopJoinCondition>();

		NodeList joinTableList = element
				.getElementsByTagName(HadoopJoinFile.TAG_NAME);

		for (int i = 0; i < joinTableList.getLength(); i++) {
			if (joinTableList.item(i) instanceof Element) {
				HadoopJoinFile joinTable = HadoopJoinFile
						.fromXMLElement((Element) joinTableList.item(i));
				joinTables.add(joinTable);

			}
		}

		NodeList joinColumnList = element
				.getElementsByTagName(HadoopJoinColumn.TAG_NAME);

		for (int i = 0; i < joinColumnList.getLength(); i++) {
			if (joinColumnList.item(i) instanceof Element) {
				HadoopJoinColumn joinColumn = HadoopJoinColumn
						.fromXMLElement((Element) joinColumnList.item(i));
				joinColumns.add(joinColumn);

			}
		}

		NodeList joinConditionList = element
				.getElementsByTagName(HadoopJoinCondition.TAG_NAME);

		for (int i = 0; i < joinConditionList.getLength(); i++) {
			if (joinConditionList.item(i) instanceof Element) {
				HadoopJoinCondition joinCondition = HadoopJoinCondition
						.fromXMLElement((Element) joinConditionList.item(i));
				joinConditions.add(joinCondition);

			}
		}
		String joinType = null;
		NodeList joinTypeList = element
				.getElementsByTagName(JOIN_TYPE_TAG_NAME);
		for (int i = 0; i < joinTypeList.getLength(); i++) {
			if (joinTypeList.item(i) instanceof Element) {
				joinType = ((Element)joinTypeList.item(i)).getAttribute(JOIN_TYPE_VALUE);
			}
		}
		HadoopJoinModel tableJoinDef = new HadoopJoinModel(joinTables,
				joinColumns, joinConditions,joinType);
		return tableJoinDef;

	}
	
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public boolean equals(Object obj) {
		HadoopJoinModel target = (HadoopJoinModel) obj;
		if(StringUtil.isEmpty(joinType)==true||joinType.equals(target.getJoinType())==false){
			return false;
		}else
		if ((joinTables == null && target.getJoinTables() != null)
				|| (target.getJoinTables() == null && joinTables != null)
				|| (joinColumns == null && target.getJoinColumns() != null)
				|| (target.getJoinColumns() == null && joinColumns != null)
				|| (joinConditions == null && target.getJoinConditions() != null)
				|| (target.getJoinConditions() == null && joinConditions != null)) {
			return false;

		} else if (joinTables.size() != target.getJoinTables().size()
				|| joinColumns.size() != target.getJoinColumns().size()
				|| joinConditions.size() != target.getJoinConditions().size()) {
			return false;
		} else if (ListUtility.equalsIgnoreOrder(joinTables, target
				.getJoinTables()) == false
				|| ListUtility.equalsIgnoreOrder(joinColumns, target
						.getJoinColumns()) == false
				|| ListUtility.equalsIgnoreOrder(joinConditions, target
						.getJoinConditions()) == false) {
			return false;
		}
		return true;

	}
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(JOIN_TYPE_TAG_NAME).append(":").append(getJoinType()).append(";");
		for (Iterator<HadoopJoinFile> iterator = getJoinTables().iterator(); iterator
				.hasNext();) {
			HadoopJoinFile item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<HadoopJoinColumn> iterator = getJoinColumns().iterator(); iterator
				.hasNext();) {
			HadoopJoinColumn item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<HadoopJoinCondition> iterator = getJoinConditions().iterator(); iterator
				.hasNext();) {
			HadoopJoinCondition item = iterator.next();
			out.append(item.toString());
		}
		return out.toString();
	}
	public List<HadoopJoinFile> getJoinTables() {
		return joinTables;
	}
	public void setJoinTables(List<HadoopJoinFile> joinTables) {
		this.joinTables = joinTables;
	}
	public List<HadoopJoinColumn> getJoinColumns() {
		return joinColumns;
	}
	public void setJoinColumns(List<HadoopJoinColumn> joinColumns) {
		this.joinColumns = joinColumns;
	}
	public List<HadoopJoinCondition> getJoinConditions() {
		return joinConditions;
	}
	public void setJoinConditions(List<HadoopJoinCondition> joinConditions) {
		this.joinConditions = joinConditions;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	public HadoopJoinFile getJoinFile(String fileId){
		if(joinTables!=null){
			for(HadoopJoinFile joinFile:joinTables){
				if(fileId.equals(joinFile.getOperatorModelID())){
					return joinFile;
				}
			}
		}
		return null;
	}
	public HadoopJoinColumn getJoinColumn(String fileId, String columnName){
		if(joinColumns!=null){
			for(HadoopJoinColumn joinColumn:joinColumns){
				if(fileId.equals(joinColumn.getFileId())
						&&columnName.equals(joinColumn.getColumnName())){
					return joinColumn;
				}
			}
		}
		return null;
	}
}
