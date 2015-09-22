/**
 * ClassName OperatorUtil
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Operator;

/**
 * @author Eason
 */
public class OperatorUtil {

	public static <T extends Operator> T createOperator(Class<T> clazz) throws OperatorException{
		Operator operator = null;
		try {
			operator = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return (T) operator;
	}
}


