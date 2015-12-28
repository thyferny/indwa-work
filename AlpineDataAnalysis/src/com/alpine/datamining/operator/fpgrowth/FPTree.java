
package com.alpine.datamining.operator.fpgrowth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



public class FPTree extends FPTreeNode {

	private Map<Item, Header> headerTable;

	public FPTree() {
		super();
		headerTable = new HashMap<Item, Header>();
		children = new HashMap<Item, FPTreeNode>();
	}

	
	public void addItemSet(Collection<Item> itemSet, long weight) {
		super.addItemSet(itemSet, headerTable, weight);
	}

	public Map<Item, Header> getHeaderTable() {
		return headerTable;
	}
 
}
