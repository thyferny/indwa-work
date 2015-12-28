
package com.alpine.datamining.operator.fpgrowth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



public class FPTreeNode {

	protected FrequencyStack frequencies;

	protected Item nodeItem;

	protected FPTreeNode sibling;

	protected FPTreeNode father;

	protected Map<Item, FPTreeNode> children;

	public FPTreeNode() {
		frequencies = new ListFrequencyStack();
		children = new LinkedHashMap<Item, FPTreeNode>();
	}

	public FPTreeNode(FPTreeNode father, Item nodeItem) {
		frequencies = new ListFrequencyStack();
		this.father = father;
		children = new HashMap<Item, FPTreeNode>();
		this.nodeItem = nodeItem;
	}

	
	public void addItemSet(Collection<Item> itemSet, Map<Item, Header> headerTable, long weight) {
		Iterator<Item> iterator = itemSet.iterator();
		if (iterator.hasNext()) {//only handle the first item
			Item firstItem = iterator.next();
			FPTreeNode childNode;
			if (!children.containsKey(firstItem)) {
				// if this node has no child for this item, create it
				childNode = createChildNode(firstItem);
				// and add it to childs of this node
				children.put(firstItem, childNode);
				// update header table:
				if (!headerTable.containsKey(firstItem)) {
					// if item unknown in headerTable, create new entry
					headerTable.put(firstItem, new Header());
				}
				// append new node to sibling chain of this item
				headerTable.get(firstItem).addSibling(childNode);
			} else {
				// select children for this item if allready existing
				childNode = children.get(firstItem);
			}
			// updating frequency in headerTable
			headerTable.get(firstItem).frequencies.increaseFrequency(0, weight);
			// updating frequency in this node
			childNode.increaseFrequency(0, weight);
			// remove added item and make recursiv call on child note
			itemSet.remove(firstItem);
			
			childNode.addItemSet(itemSet, headerTable, weight);
		}
	}

	
	public FPTreeNode getFather() {
		return father;
	}

	
	public boolean hasFather() {
		return (this.father != null);
	}

	
	public FPTreeNode getSibling() {
		return sibling;
	}

	
	public FPTreeNode getLastSibling() {
		FPTreeNode currentNode = this;
		while (currentNode.hasSibling()) {
			currentNode = currentNode.getSibling();
		}
		return currentNode;
	}

	
	public void setSibling(FPTreeNode sibling) {
		this.sibling = sibling;
	}

	
	public boolean hasSibling() {
		return (this.sibling != null);
	}

	
	public void increaseFrequency(int recursionDepth, long weight) {
		frequencies.increaseFrequency(recursionDepth, weight);
	}

	
	public void popFrequency(int height) {
		frequencies.popFrequency(height);
	}

	
	public long getFrequency(int height) {
		return frequencies.getFrequency(height);
	}

	
	public Item getNodeItem() {
		return this.nodeItem;
	}

	
	public Map<Item, FPTreeNode> getChildren() {
		return this.children;
	}

	
	public FPTreeNode getChild() {
		if (children.size() != 1) {
			return null;
		} else {
			return children.get(children.keySet().iterator().next());
		}
	}

	
	public FPTreeNode createChildNode(Item nodeItem) {
		return new FPTreeNode(this, nodeItem);
	}

 
}
