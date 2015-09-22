/**
 * ClassName ParameterInputType.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter;


/**
 * In the future all object type parameter will implement this interface.
 * Currently we support:
 * AggregateFieldModel
 * ColumnBinsModel
 * DerivedFieldsModel
 * HiddenLayersModel
 * QuantileFieldsModel
 * TableJoinModel
 * WindowFieldsModel
 * 
 *  
 * The ParameterObject can be close and equals, it can also be read and write as xml documnet.
 * 
 * @author zhaoyong
 *
 */
public interface ParameterObject extends XMLFragment {
	
	

 
}
