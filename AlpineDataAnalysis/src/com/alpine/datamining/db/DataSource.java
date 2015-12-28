
package com.alpine.datamining.db;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Operator;


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