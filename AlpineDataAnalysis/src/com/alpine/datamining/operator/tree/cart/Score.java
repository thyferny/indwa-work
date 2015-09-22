/**
 * ClassName Benefit.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cart;

import java.util.List;

import com.alpine.datamining.db.Column;

/**
 * some information about the score of a split.
 * 
 *
 */
public class Score implements Comparable<Score> {

	private Column column;
	
	private double benefit;
	
	private double splitValue;
	
	private List<String> values;
	
	public Score(double benefit, Column column) {
		this(benefit, column, Double.NaN);
	}
	
	public Score(double benefit, Column column, double splitValue) {
		this.benefit = benefit;
		this.column = column;
		this.splitValue = splitValue;
	}
	
	public Column getColumn() {
		return this.column;
	}
	
	public double getSplitValue() {
		return this.splitValue;
	}
	
	public double getScore() {
		return this.benefit;
	}
	
	public String toString() {
//		return "Column = " + column.getName() + ", benefit = " + benefit + (!Double.isNaN(splitValue) ? ", split = " + splitValue : "");
		if (column.isNumerical())
		{
			return "Column = " + column.getName() + ", benefit = " + benefit + (!Double.isNaN(splitValue) ? ", split = " + splitValue : "");
		}
		else
		{
			return "Column = " + column.getName() + ", benefit = " + benefit + ((values != null) ? ", split = " + values : "");
		}
	}

	public int compareTo(Score o) {
		return -1 * Double.compare(this.benefit, o.benefit);
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<String> getValues() {
		return values;
	}
}
