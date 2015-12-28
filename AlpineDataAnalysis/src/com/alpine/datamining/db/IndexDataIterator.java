
package com.alpine.datamining.db;




public class IndexDataIterator extends AbstractDataIterator {

	
	private int current;

	
	private DataSet parent;
	
	
	private Data next;

	
	private long size;
	
	public IndexDataIterator(DataSet parent) {
		this.parent = parent;
		this.size = parent.size();
		current = -1;
		hasNext();
	}

	public boolean hasNext() {
		while (next == null) {
			current++;
			
			if (current >= size)
				return false;
			
			next = parent.getRow(current);
		}
		return true;
	}

	public Data next() {
		if (!hasNext()) {
			return null;
		} else {
			Data dummy = next;
			next = null;
			return dummy;
		}
	}

}
