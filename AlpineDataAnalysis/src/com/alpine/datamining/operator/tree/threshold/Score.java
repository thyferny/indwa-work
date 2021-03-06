
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;


public class Score implements Comparable<Score> {

	private Column column;
	
	private double score;
	
	private double splitValue;
	
	public Score(double benefit, Column column) {
		this(benefit, column, Double.NaN);
	}
	
	public Score(double score, Column column, double splitValue) {
		this.score = score;
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
		return this.score;
	}
	
	public String toString() {
		return "Column = " + column.getName() + ", score = " + score + (!Double.isNaN(splitValue) ? ", split = " + splitValue : "");
	}

	public int compareTo(Score o) {
		return -1 * Double.compare(this.score, o.score);
	}
}
