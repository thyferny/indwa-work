/**
 * ClassName FPTree.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the fp-tree structure for {@link FPGrowth}.
 * 
 * @author Eason
 */
public class FPTree extends FPTreeNode {

	private Map<Item, Header> headerTable;

	public FPTree() {
		super();
		headerTable = new HashMap<Item, Header>();
		children = new HashMap<Item, FPTreeNode>();
	}

	/**
	 * This method adds a set of Items to the tree. 
	 * 
	 * @param itemSet
	 *            the sorted set of items
	 * @param weight
	 *            the frequency of the set of items
	 */
	public void addItemSet(Collection<Item> itemSet, long weight) {
		super.addItemSet(itemSet, headerTable, weight);
	}

	public Map<Item, Header> getHeaderTable() {
		return headerTable;
	}
 
}
