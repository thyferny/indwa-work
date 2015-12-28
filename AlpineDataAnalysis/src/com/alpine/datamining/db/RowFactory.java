
package com.alpine.datamining.db;




public class RowFactory {
	
	public RowFactory() {
	}

	
	public Row create(int size) {
		Row row = null;
		row = new DoubleRow(new double[size]);
		return row;
	}

}
