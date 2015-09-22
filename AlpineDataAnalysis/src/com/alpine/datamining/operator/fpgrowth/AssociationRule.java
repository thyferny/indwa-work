/**
 * ClassName AssociationRule.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * An association rule generated from a frequent item set.
 * @author Eason
 */
public class AssociationRule implements Serializable, Comparable<AssociationRule> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3871579274858215710L;
	/**
	 * Confidence is the ratio of the number of transactions that include all items in the
	 * consequent as well as the antecedent (namely, the support) to the number
	 * of transactions that include all items in the antecedent.
	 * */
	private double confidence;
	/***
	 * The support is
	 * simply the number of transactions that include all items in the
	 * antecedent and consequent parts of the rule. (The support is sometimes
	 * expressed as a percentage of the total number of records in the
	 * database.)
	 */
	private double totalSupport;

	private double lift;

	private double laplace;

	private double gain;

	private double ps;

	private double conviction;

	private Collection<Item> premise;

	private Collection<Item> conclusion;

	public AssociationRule(Collection<Item> premise, Collection<Item> conclusion,
			double totalSupport) {
		this.premise = premise;
		this.conclusion = conclusion;
		this.totalSupport = totalSupport;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public double getConviction() {
		return conviction;
	}

	public void setConviction(double conviction) {
		this.conviction = conviction;
	}

	public double getLaplace() {
		return laplace;
	}

	public void setLaplace(double laplace) {
		this.laplace = laplace;
	}

	public double getLift() {
		return lift;
	}

	public void setLift(double lift) {
		this.lift = lift;
	}

	public double getPs() {
		return ps;
	}

	public void setPs(double ps) {
		this.ps = ps;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getConfidence() {
		return this.confidence;
	}

	public double getTotalSupport() {
		return this.totalSupport;
	}

	public Iterator<Item> getPremiseItems() {
		return premise.iterator();
	}

	public Iterator<Item> getConclusionItems() {
		return conclusion.iterator();
	}

	public String toPremiseString() {
		return premise.toString();
	}

	public String toConclusionString() {
		return conclusion.toString();
	}

	public Collection<Item> getPremise() {
		return premise;
	}

	public void setPremise(Collection<Item> premise) {
		this.premise = premise;
	}

	public Collection<Item> getConclusion() {
		return conclusion;
	}

	public void setConclusion(Collection<Item> conclusion) {
		this.conclusion = conclusion;
	}

	public int compareTo(AssociationRule o) {
		return Double.compare(this.confidence, o.confidence);
	}

	 
	public boolean equals(Object o) {
		if (!(o instanceof AssociationRule))
			return false;
		AssociationRule other = (AssociationRule) o;
		return premise.toString().equals(other.premise.toString())
				&& conclusion.toString().equals(other.conclusion.toString())
				&& this.confidence == other.confidence;
	}

	public int hashCode() {
		return premise.toString().hashCode() ^ conclusion.toString().hashCode()
				^ Double.valueOf(this.confidence).hashCode();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(premise.toString());
		buffer.append(" --> ");
		buffer.append(conclusion.toString());
		buffer.append(" (confidence: ");
		buffer.append(confidence);
		buffer.append(" support: ");
		buffer.append(totalSupport);
		buffer.append(")");
		return buffer.toString();
	}
}
