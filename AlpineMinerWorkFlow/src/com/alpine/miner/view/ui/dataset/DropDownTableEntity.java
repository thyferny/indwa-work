package com.alpine.miner.view.ui.dataset;

import java.util.HashMap;


public class DropDownTableEntity {
	private String text;
	private String[] columnNames;
	private Object obj;
	private HashMap<String,TableEntity> tableEntityMap=new HashMap<String,TableEntity>();
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	private TableEntity entity = null;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public TableEntity getEntity() {
		return entity;
	}
	public void setEntity(TableEntity entity) {
		this.entity = entity;
	}

	public void addEntity(String columnName,TableEntity entity){
		tableEntityMap.put(columnName, entity);
	}
	
	public TableEntity getEntityByColumn(String columnName) {
		return tableEntityMap.get(columnName);
	}
	public HashMap<String, TableEntity> getTableEntityMap() {
		return tableEntityMap;
	}
	public void setTableEntityMap(HashMap<String, TableEntity> tableEntityMap) {
		this.tableEntityMap = tableEntityMap;
	}
	
}
