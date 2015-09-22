/**
 * ClassName  AbstractQuantileItemBin.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.variable;



/**
 * @author John Zhao
 *
 */
public abstract class AbstractQuantileItemBin implements AnalysisQuantileItemBin {

	public static final String ATTR_VALUES="values";
	
	private int binIndex;
	private int binType;

	public void setBinIndex(int binIndex) {
		this.binIndex = binIndex;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.gef.runoperator.field.varible.QuantileItemBin#getBinIndex()
	 */
	@Override
	public int getBinIndex() {
		return binIndex;
	}

	
	public int getBinType(){
		return binType;
	}
	public void setBinType(int binType){
		this.binType=binType;
	}

	public abstract AnalysisQuantileItemBin clone();
	
}
