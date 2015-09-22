package com.alpine.miner.view.ui.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextAndTableListEntity {

	private String text;
	private List<TableEntity> list = new ArrayList<TableEntity>();
	private HashMap<String, HashMap<String, String>> transformMap =new HashMap<String, HashMap<String, String>>();
	public HashMap<String, HashMap<String, String>> getTransformMap() {
		return transformMap;
	}
	public void setTransformMap(HashMap<String, HashMap<String, String>> transformMap) {
		this.transformMap = transformMap;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public void addTableEntity(TableEntity te){
		list.add(te);
	}
	public List<TableEntity> getTableEntityList(){
		return list;
	}
	
	public void setTableEntityList(List<TableEntity> tableList){
		this.list = tableList;
	}
}
