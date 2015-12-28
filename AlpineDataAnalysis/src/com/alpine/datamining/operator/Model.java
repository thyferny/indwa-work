
package com.alpine.datamining.operator;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;


public interface Model extends ConsumerProducer{


	public DataSet apply(DataSet testSet) throws OperatorException;
	
}
