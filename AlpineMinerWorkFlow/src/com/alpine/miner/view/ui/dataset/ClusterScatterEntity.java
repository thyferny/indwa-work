package com.alpine.miner.view.ui.dataset;

import java.awt.Color;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClusterScatterEntity {

	private Map<String,List<Double>> columnDataHt = new LinkedHashMap<String, List<Double>>();
	private Map<String,Double> columnCenterHt = new LinkedHashMap<String, Double>();
	private Object jfreechart;
	private String[] defaultColumn = new String[2];
	private Color color;
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public String[] getDefaultColumn() {
		return defaultColumn;
	}
	public void setDefaultColumn(String[] defaultColumn) {
		this.defaultColumn = defaultColumn;
	}
	public Object getJfreechart() {
		return jfreechart;
	}
	public void setJfreechart(Object jfreechart) {
		this.jfreechart = jfreechart;
	}
	public Map<String,Double> getCenterHt(){
		return columnCenterHt;
	}
	public void addCenterPoint(String key,Double value){
		columnCenterHt.put(key, value);
	}
	public Map<String,List<Double>> getDataHt(){
		return columnDataHt;
	}
	public void addDataHt(String key,List<Double> list){
		columnDataHt.put(key, list);
	}
	
}
