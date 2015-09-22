/**
 * ClassName AssociationRuleGenerator.java
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

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Operator;

/**
 * generate association rules
 * @author Eason
 */
public class AssociationRuleGenerator extends Operator {

	private AssociationRuleGeneratorParameter para;
	public static final String[] CRITERIA = {
		"confidence",
		"lift",
		"conviction",
		"ps",
		"gain",
		"laplace"
	};
	
	public static final int CONFIDENCE = 0;
	public static final int LIFT = 1;
	public static final int CONVICTION = 2;
	public static final int PS = 3;
	public static final int GAIN = 4;
	public static final int LAPLACE = 5;
	
	public AssociationRuleGenerator() {
		super();
	}

	public ConsumerProducer[] apply() throws OperatorException {
		para = (AssociationRuleGeneratorParameter)getParameter();
		double minValue = para.getMinConfidence();
//		if (0 != CONFIDENCE) {
//			minValue = 0.8d;
//		}
		double theta = 2d;
		double laplaceK = 1d;
		ItemSets sets = null;
		sets = getInput(ItemSets.class);
		AssociationRules rules = new AssociationRules(sets.getDataSet());
		rules.setPositiveValue(sets.getPositiveValue());
		HashMap<Collection<Item>, Long> setFrequencyMap = new HashMap<Collection<Item>, Long>();
		long numberOfTransactions = sets.getNumberOfTransactions();
		
		// iterating sorted over every frequent Set, generating every possible rule and building frequency map
		sets.sortSets();
		for (ItemSet set : sets) {
			setFrequencyMap.put(set.getItems(), set.getFrequency());
			// generating rule by splitting set in every two parts for head and body of rule 
			if (set.getItems().size() > 1) {
				PowerSet<Item> powerSet = new PowerSet<Item>(set.getItems());
				for (Collection<Item> premises : powerSet) {
					if (premises.size() > 0 && premises.size() < set.getItems().size()) {
						Collection<Item> conclusion = powerSet.getComplement(premises);
						long totalFrequency = set.getFrequency();
						long preconditionFrequency = setFrequencyMap.get(premises);
						long conclusionFrequency = setFrequencyMap.get(conclusion);
						
						double value = getCriterionValue(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions, theta, laplaceK);
						if (value >= minValue) {
							AssociationRule rule = 
								new AssociationRule(premises, 
	                                                conclusion,  
	                                                getSupport(totalFrequency, numberOfTransactions));
							rule.setConfidence(getConfidence(totalFrequency, preconditionFrequency));
							rule.setLift(getLift(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setConviction(getConviction(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setPs(getPs(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setGain(getGain(theta, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setLaplace(getLaPlace(laplaceK, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rules.addItemRule(rule);
						}
					}
				}
			}
		}
		return new ConsumerProducer[] {rules};
	}
	
	private double getCriterionValue(long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions, double theta, double laplaceK) throws OperatorException {
		int criterion = 0;
		switch (criterion) {
		case LIFT:
			return getLift(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions);
		case CONVICTION:
			return getConviction(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions);
		case PS:
			return getPs(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions);
		case GAIN:
			return getGain(theta, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions);
		case LAPLACE:
			return getLaPlace(laplaceK, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions);
		case CONFIDENCE:
		default:
			return getConfidence(totalFrequency, preconditionFrequency);
		}
	}
	
	private double getGain(double theta, long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions) {
		return getSupport(totalFrequency, numberOfTransactions) - theta * getSupport(preconditionFrequency, numberOfTransactions);
	}
	
	private double getLift(long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions) {
		return ((double) totalFrequency * ((double) numberOfTransactions)) / ((double)preconditionFrequency * conclusionFrequency);
	}
	
	private double getPs(long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions) {
		return getSupport(totalFrequency, numberOfTransactions) - getSupport(preconditionFrequency, numberOfTransactions) * getSupport(conclusionFrequency, numberOfTransactions);
	}
	
	private double getLaPlace(double k, long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions) {
		return (getSupport(totalFrequency, numberOfTransactions) + 1d) / (getSupport(preconditionFrequency, numberOfTransactions) + k);
	}
	
	private double getConviction(long totalFrequency, long preconditionFrequency, long conclusionFrequency, long numberOfTransactions) {
		double numerator = preconditionFrequency * (numberOfTransactions - conclusionFrequency);
		double denumerator = numberOfTransactions * (preconditionFrequency - totalFrequency);
		return numerator / denumerator;
	}
	
	private double getConfidence(long totalFrequency, long preconditionFrequency) {
		return (double)totalFrequency / (double)preconditionFrequency;
	}
	
	private double getSupport(long preconditionFrequency, long numberOfTransactions) {
		return (double)preconditionFrequency / (double)numberOfTransactions;
	}
	

	public Class<?>[] getInputClasses() {
		return new Class[] { ItemSets.class	};
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { AssociationRules.class };
	}
}
