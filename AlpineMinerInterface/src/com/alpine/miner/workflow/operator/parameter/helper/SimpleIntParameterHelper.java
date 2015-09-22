/**
 * ClassName SimpleIntParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterDataType;

/**
 * @author zhaoyong
 *
 */
//name may have special validation, like table name ...
public class SimpleIntParameterHelper extends SimpleInputParameterHelper{
 
	 private int max=0;
	private int min=0;

	public SimpleIntParameterHelper(int max, int min){
		 super(ParameterDataType.INT);
		 //for validation use...
		 this.max=max;
		 this.min=min;
	 }

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	@Override
	public boolean doValidate(OperatorParameter parameter) {
		double value=Integer.valueOf(	parameter.getValue().toString());
		// not set, always true
		if(max==min&&max==0){
			return true;
		}else{
			return (value<max&&value>min);
		}
	}
}
