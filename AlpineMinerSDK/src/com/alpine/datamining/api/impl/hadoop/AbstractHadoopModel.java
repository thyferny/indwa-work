/**
 * 

* ClassName AbstractHadoopModel.java
*
* Version information: 1.00
*
* Date: 2012-8-20
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.hadoop;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.AbstractModel;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.OutputObject;

/**
 * @author Shawn
 *
 *  
 */

public class AbstractHadoopModel    extends OutputObject implements Model{

/**
	 * 
	 */
	private static final long serialVersionUID = -6293735265191972737L;

//	protected AbstractHadoopModel() {
//		super (null);
//	}
//	
//	protected AbstractHadoopModel(DataSet dataSet) {
//		super(dataSet);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public DataSet apply(DataSet testSet) throws OperatorException {
		// TODO Auto-generated method stub
		return null;
	}

}
