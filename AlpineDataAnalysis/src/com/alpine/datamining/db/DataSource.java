/**
 * ClassName DataSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Operator;

/** 
 * Super class of all operators requiring no input and creating a {@link DataSet}.
 *  
 * @author  Eason
 */
public abstract class DataSource<T extends ConsumerProducer> extends Operator {
	private final Class<?>[] outputClasses;
	private static final Class<?>[] INPUT_CLASSES = {};
	
	public DataSource() {
		super();
		outputClasses = new Class[] { DataSet.class }; 	
	}

	public final ConsumerProducer[] apply() throws OperatorException {
		return new ConsumerProducer[] {  };
	}
	
	public final Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}
	
	public final Class<?>[] getOutputClasses() {
		return outputClasses;		
	}
}