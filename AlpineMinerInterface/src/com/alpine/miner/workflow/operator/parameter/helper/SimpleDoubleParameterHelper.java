/**
 * ClassName SimpleDoubleParameterHelper.java
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
public class SimpleDoubleParameterHelper extends SimpleInputParameterHelper{
	 	private double max=0;
		private double min=0;

		public SimpleDoubleParameterHelper(double max, double min){
			 super(ParameterDataType.INT);
			 //for validation use...
			 this.max=max;
			 this.min=min;
		 }

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
		}

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
		}
		
		 

		@Override
		public boolean doValidate(OperatorParameter parameter) {
			double value=Double.valueOf(	parameter.getValue().toString());
			if(max==min&&max==0){
				return true;
			}else{
				return (value<max&&value>min);
			}
		}
}
