/**
 * ClassName LogisticRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;




/**
 * @author John Zhao
 * 
 */
public class AprioriConfig extends AbstractAssociationConfig {

	private String minSupport;
	private String findMinItemsets;
	
	public String getFindMinItemsets() {
		return findMinItemsets;
	}

	private String positiveValue;
	private String labelAttribute;

	private String idAttribute;

	public String getLabelAttribute() {
		return labelAttribute;
	}

	public String getIdAttribute() {
		return idAttribute;
	}
	/**
	 * @param tableName
	 */
	public AprioriConfig( String minSupport,String findMinItemsets,
			String positiveValue,String ruleMinConfidence,String ruleCriterion,
			String idAttribute,String labelAttribute) {
 
		
		
		
		super (ruleMinConfidence,ruleCriterion);
		
		this.minSupport=minSupport;
		
		this.findMinItemsets = findMinItemsets;//string  false
			
		this.positiveValue =positiveValue;
			
		this.idAttribute=idAttribute;
		this.labelAttribute=labelAttribute;
		
		
		// TODO Auto-generated constructor stub
	}

 

	public String getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(String minSupport) {
		this.minSupport = minSupport;
	}

	public String getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(String positiveValue) {
		this.positiveValue = positiveValue;
	}

 
 

}
