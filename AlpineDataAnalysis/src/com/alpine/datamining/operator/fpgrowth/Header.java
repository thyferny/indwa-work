/**
 * ClassName Header.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/** 
 * An entry in the header table.
 * 
 * @author Eason
 */
public class Header {

	FrequencyStack frequencies;

	List<FPTreeNode> siblingChain;

	public Header() {
		frequencies = new ListFrequencyStack();
		siblingChain = new LinkedList<FPTreeNode>();
	}

	public void addSibling(FPTreeNode node) {
		siblingChain.add(node);
	}

	public Collection<FPTreeNode> getSiblingChain() {
		return siblingChain;
	}

	public FrequencyStack getFrequencies() {
		return frequencies;
	}
}
