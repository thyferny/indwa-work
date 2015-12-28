
package com.alpine.datamining.operator.kmeans;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Operator;


public abstract class AbstractClusterer extends Operator {	

//	private static final Class<?>[] INPUT_CLASSES =  { DataSet.class };
//	private static final Class<?>[] OUTPUT_CLASSES = { ClusterModel.class };
	
	public AbstractClusterer() {
		super();		
	}

	
	public abstract ClusterModel generateClusterModel(DataSet dataSet) throws OperatorException;
	
//	@Override
//	public final Class<?>[] getInputClasses() {
//		return INPUT_CLASSES;
//	}
//
//	@Override
//	public final Class<?>[] getOutputClasses() {
//		return OUTPUT_CLASSES;
//	}

	@Override
	public final ConsumerProducer[] apply() throws OperatorException {
		return new ConsumerProducer[] { generateClusterModel(getInput(DataSet.class)) };
	}
}
