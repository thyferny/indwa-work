
package com.alpine.datamining.db;



public class DoubleRow extends Row {

	private static final long serialVersionUID = -6917570408548407625L;
	
	private double[] data;

	
	public DoubleRow(double[] data) {
		this.data = data;
	}
	
	protected double get(int index, double defaultValue) {
		return data[index];
	}

	
	protected void set(int index, double value, double defaultValue) {
		data[index] = value;
	}

	
	protected void ensureNumberOfColumns(int numberOfColumns) {
		if (data.length >= numberOfColumns)
			return;
		double[] newData = new double[numberOfColumns];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < data.length; i++)
			result.append((i == 0 ? "" : ",") + data[i]);
		return result.toString();
	}
}
