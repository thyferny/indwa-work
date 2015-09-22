/**
 * ClassName BooleanColumnItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import com.alpine.datamining.db.Column;

/**
 * This is an {@link Item} based on columns.
 * 
 * @author Eason
 */
public class BooleanColumnItem implements Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7909167219175202234L;

	private long frequency = 0;

	private Column item;
	
	private String name;

	public BooleanColumnItem(Column item) {
		this.item = item;
		this.name = item.getName();
	}

	public BooleanColumnItem(String name){
		this.name = name;
	}
	public long getFrequency() {
		return this.frequency;
	}



	public boolean equals(Object other) {
		if (!(other instanceof BooleanColumnItem))
			return false;
		BooleanColumnItem o = (BooleanColumnItem)other;
		return (this.name.equals(o.name)) && (this.frequency == o.frequency);
	}
	
	public int hashCode() {
		return this.name.hashCode() ^ Double.valueOf(this.frequency).hashCode();
	}
	
	public int compareTo(Item arg0) {
		Item comparer = arg0;
		// Collections.sort generates ascending order. Descending needed,
		// therefore invert return values!
		if (comparer.getFrequency() == this.getFrequency()) {
			return (-1 * this.name.compareTo(arg0.toString()));
		} else if (comparer.getFrequency() < this.getFrequency()) {
			return -1;
		} else {
			return 1;
		}
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return this.name;
	}

	public Column getItem() {
		return item;
	}

	public void setItem(Column item) {
		this.item = item;
	}

	public void increaseFrequency(long value) {
		frequency += value;
	}
}
