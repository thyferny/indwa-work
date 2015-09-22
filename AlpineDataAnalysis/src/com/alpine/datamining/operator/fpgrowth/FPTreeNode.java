/**
 * ClassName FPTreeNode.java
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A node in the FPTree.
 * 
 * @author Eason
 */
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

	/**
	 *  This method adds a set of Items to the tree of
	 * this node. 
	 * 
	 * @param itemSet
	 *            the sorted set of items
	 * @param headerTable
	 *            gives the headertable for finding other nodes of an item
	 */
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

	/**
	 * Returns the father of this node or null if node is root
	 */
	public FPTreeNode getFather() {
		return father;
	}

	/**
	 * Returns true if node has father
	 */
	public boolean hasFather() {
		return (this.father != null);
	}

	/**
	 * Returns the next node representing the same item as this node.
	 */
	public FPTreeNode getSibling() {
		return sibling;
	}

	/**
	 * Returns the last node of the chain of nodes representing the same item as this node
	 */
	public FPTreeNode getLastSibling() {
		FPTreeNode currentNode = this;
		while (currentNode.hasSibling()) {
			currentNode = currentNode.getSibling();
		}
		return currentNode;
	}

	/**
	 * This method sets the next node in the chain of node representing the same item as this node
	 * 
	 * @param sibling
	 *            is the next node in the chain
	 */
	public void setSibling(FPTreeNode sibling) {
		this.sibling = sibling;
	}

	/**
	 * Returns true if this node is not the last one in the chain of nodes representing the same item as this node. 
	 */
	public boolean hasSibling() {
		return (this.sibling != null);
	}

	/**
	 * This method increases the frequency of this current node by the given weight in given recusionDepth
	 * 
	 * @param weight
	 *            the frequency is increased by this value
	 */
	public void increaseFrequency(int recursionDepth, long weight) {
		frequencies.increaseFrequency(recursionDepth, weight);
	}

	/**
	 * This method clears the frequency stack on top
	 */
	public void popFrequency(int height) {
		frequencies.popFrequency(height);
	}

	/**
	 * this returns the frequency of the node in current recursion
	 */
	public long getFrequency(int height) {
		return frequencies.getFrequency(height);
	}

	/**
	 * this returns the item, this node represents
	 */
	public Item getNodeItem() {
		return this.nodeItem;
	}

	/**
	 * This returns the map, which maps the child nodes on items. It may be used to get a set of all childNodes or all represented items.
	 */
	public Map<Item, FPTreeNode> getChildren() {
		return this.children;
	}

	/**
	 * This method returns the first child. If no child exists, null is returned
	 */
	public FPTreeNode getChild() {
		if (children.size() != 1) {
			return null;
		} else {
			return children.get(children.keySet().iterator().next());
		}
	}

	/**
	 * this method creates a new childnode of this node, representing the node item
	 * 
	 * @param nodeItem
	 *            the item, represented by the new node
	 */
	public FPTreeNode createChildNode(Item nodeItem) {
		return new FPTreeNode(this, nodeItem);
	}

 
}
