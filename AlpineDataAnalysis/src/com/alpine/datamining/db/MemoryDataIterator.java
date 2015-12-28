
package com.alpine.datamining.db;



public class MemoryDataIterator extends AbstractDataIterator {

	
	private RowIterator rowIterator;
	
	
	private DataSet dataSet;
	
	
	public MemoryDataIterator(RowIterator drr, DataSet dataSet) {
		this.rowIterator = drr;
		this.dataSet = dataSet;
	}

	
	public boolean hasNext() {
		if (rowIterator == null)
		{
			return false;
		}
		return rowIterator.hasNext();
	}

	
	public Data next() {
		if (!hasNext())
			return null;
		Row data = rowIterator.next();
		if (data == null)
			return null;
		return new Data(data, dataSet.getColumns());
	}
}
