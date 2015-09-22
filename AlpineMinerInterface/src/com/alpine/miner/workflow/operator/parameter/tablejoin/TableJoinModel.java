/**
 * ClassName  SampleDataDefinition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-19
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.tablejoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 * 
 */
public class TableJoinModel extends AbstractParameterObject {

	// we keep this tag name to compaitable with old version's flow file
	public static final String TAG_NAME = "TableJoinDefinition";

	private List<JoinTable> joinTables = new ArrayList<JoinTable>();

	private List<JoinColumn> joinColumns = new ArrayList<JoinColumn>();

	private List<JoinCondition> joinConditions = new ArrayList<JoinCondition>();

	@Override
	public Object clone() throws CloneNotSupportedException {
		List<JoinTable> newJoinTables = new ArrayList<JoinTable>();
		for (JoinTable joinTable : joinTables) {
			newJoinTables.add((JoinTable) joinTable.clone());
		}
		List<JoinColumn> newJoinColumns = new ArrayList<JoinColumn>();
		for (JoinColumn joinColumn : joinColumns) {
			newJoinColumns.add((JoinColumn) joinColumn.clone());
		}
		List<JoinCondition> newJoinConditions = new ArrayList<JoinCondition>();
		for (JoinCondition joinCondition : joinConditions) {
			newJoinConditions.add((JoinCondition) joinCondition.clone());
		}
		return new TableJoinModel(newJoinTables, newJoinColumns,
				newJoinConditions);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (Iterator<JoinTable> iterator = getJoinTables().iterator(); iterator
				.hasNext();) {
			JoinTable item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<JoinColumn> iterator = getJoinColumns().iterator(); iterator
				.hasNext();) {
			JoinColumn item = iterator.next();
			out.append(item.toString());
		}
		for (Iterator<JoinCondition> iterator = getJoinConditions().iterator(); iterator
				.hasNext();) {
			JoinCondition item = iterator.next();
			out.append(item.toString());
		}
		return out.toString();
	}

	/**
	 * @param joinTabls
	 * @param joinColumns
	 * @param joinConditions
	 */
	public TableJoinModel(List<JoinTable> joinTabls,
			List<JoinColumn> joinColumns, List<JoinCondition> joinConditions) {
		this.joinTables = joinTabls;
		this.joinColumns = joinColumns;
		this.joinConditions = joinConditions;
	}

	/**
	 * 
	 */
	public TableJoinModel() {

	}

	public void addJoinTable(JoinTable joinTable) {
		getJoinTables().add(joinTable);
	}

	public void deleteJoinTable(JoinTable joinTable) {
		getJoinTables().remove(joinTable);
	}

	public void addJoinColumn(JoinColumn joinColumn) {
		getJoinColumns().add(joinColumn);
	}

	public void deleteJoinColumn(JoinColumn joinColumn) {
		getJoinColumns().remove(joinColumn);
	}

	public void addJoinCondition(JoinCondition joinCondition) {
		getJoinConditions().add(joinCondition);
	}

	public void deleteJoinCondition(JoinCondition joinCondition) {
		getJoinConditions().remove(joinCondition);
	}

	public void addNewTable(String schemaName, String tableName,
			String operatorMpdelID, String alias) {
		JoinTable joinTable = new JoinTable(schemaName, tableName, alias,
				operatorMpdelID);
		getJoinTables().add(joinTable);
		return;
	}

	public Element toXMLElement(Document xmlDoc) {

		Element ele = xmlDoc.createElement(TAG_NAME);
		if (getJoinTables() != null) {
			for (Iterator<JoinTable> iterator = getJoinTables().iterator(); iterator
					.hasNext();) {
				JoinTable joinTable = iterator.next();
				if (joinTable != null) {
					Element joinTableElement = joinTable.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}

		if (getJoinColumns() != null) {
			for (Iterator<JoinColumn> iterator = getJoinColumns().iterator(); iterator
					.hasNext();) {
				JoinColumn joinColumn = iterator.next();
				if (joinColumn != null) {
					Element joinTableElement = joinColumn.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}

		if (getJoinConditions() != null) {
			for (Iterator<JoinCondition> iterator = getJoinConditions()
					.iterator(); iterator.hasNext();) {
				JoinCondition joinCondition = iterator.next();
				if (joinCondition != null) {
					Element joinTableElement = joinCondition
							.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}
		return ele;
	}

	public static TableJoinModel fromXMLElement(Element element) {
		List<JoinTable> joinTables = new ArrayList<JoinTable>();

		List<JoinColumn> joinColumns = new ArrayList<JoinColumn>();

		List<JoinCondition> joinConditions = new ArrayList<JoinCondition>();

		NodeList joinTableList = element
				.getElementsByTagName(JoinTable.TAG_NAME);

		for (int i = 0; i < joinTableList.getLength(); i++) {
			if (joinTableList.item(i) instanceof Element) {
				JoinTable joinTable = JoinTable
						.fromXMLElement((Element) joinTableList.item(i));
				joinTables.add(joinTable);

			}
		}

		NodeList joinColumnList = element
				.getElementsByTagName(JoinColumn.TAG_NAME);

		for (int i = 0; i < joinColumnList.getLength(); i++) {
			if (joinColumnList.item(i) instanceof Element) {
				JoinColumn joinColumn = JoinColumn
						.fromXMLElement((Element) joinColumnList.item(i));
				joinColumns.add(joinColumn);

			}
		}

		NodeList joinConditionList = element
				.getElementsByTagName(JoinCondition.TAG_NAME);

		for (int i = 0; i < joinConditionList.getLength(); i++) {
			if (joinConditionList.item(i) instanceof Element) {
				JoinCondition joinCOndition = JoinCondition
						.fromXMLElement((Element) joinConditionList.item(i));
				joinConditions.add(joinCOndition);

			}
		}

		TableJoinModel tableJoinDef = new TableJoinModel(joinTables,
				joinColumns, joinConditions);
		return tableJoinDef;

	}

	public List<JoinTable> getJoinTables() {
		return joinTables;
	}

	public void setJoinTabls(List<JoinTable> joinTabls) {
		this.joinTables = joinTabls;
	}

	public List<JoinColumn> getJoinColumns() {
		return joinColumns;
	}

	public void setJoinColumns(List<JoinColumn> joinColumns) {
		this.joinColumns = joinColumns;
	}

	public List<JoinCondition> getJoinConditions() {
		return joinConditions;
	}

	public void setJoinConditions(List<JoinCondition> joinConditions) {
		this.joinConditions = joinConditions;
	}

	public boolean equals(Object obj) {
		TableJoinModel target = (TableJoinModel) obj;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {

		return TAG_NAME;
	}

}