package com.alpine.datamining.operator.timeseries;

import com.alpine.datamining.operator.Parameter;

public class ARIMARParameter implements Parameter {
	private String idColumn;
	private String valueColumn;
	private String groupColumn;
	private int p = 1;
	private int q = 1;
	private int d = 0;
//	private int cycle;
	private int threshold = 1000;
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	public String getValueColumn() {
		return valueColumn;
	}
	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}
	public String getGroupColumn() {
		return groupColumn;
	}
	public void setGroupColumn(String groupColumn) {
		this.groupColumn = groupColumn;
	}
	public int getP() {
		return p;
	}
	public void setP(int p) {
		this.p = p;
	}
	public int getQ() {
		return q;
	}
	public void setQ(int q) {
		this.q = q;
	}
	public int getD() {
		return d;
	}
	public void setD(int d) {
		this.d = d;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
