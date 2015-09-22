/**
 * ClassName OperatorFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator;
import java.lang.reflect.Constructor;
import java.util.Locale;

import org.apache.log4j.Logger;


public class OperatorFactory {
    private static final Logger itsLogger=Logger.getLogger(OperatorFactory.class);

    public static Operator createOperator(String className) {
		return createOperator(className,Locale.getDefault());
	}
	
	public static Operator createOperator(String className,Locale locale) {
		//this is the result of refine package name form runoperator -> operator
		
		Operator object = null;
		try {
			if(className.indexOf(".runoperator.")>0){
				className=className.replace(".runoperator.", ".operator.");
			}
			Class<?> classDefinition = Class.forName(
					className);
			object = (Operator) classDefinition.newInstance();
			object.setLocale(locale);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}
		return object;
	}

	public static Operator createOperator(String className,
			String udfOperatorName) throws Exception {
		if (className.indexOf(".runoperator.") > 0) {
			className = className.replace(".runoperator.", ".operator.");
		}
		Operator object = null;
		try {
			Class<?> classDefinition = Class.forName(className);
			Constructor<?> ctor[] = classDefinition.getDeclaredConstructors();
			object = (Operator) classDefinition.getConstructor(
					ctor[0].getParameterTypes()).newInstance(
					new Object[] { udfOperatorName });
		} catch (Exception e) {
			itsLogger.error(
					OperatorFactory.class.getName() + "\n" + e.toString());
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw e;
			}
		}
		return object;
	}
}
