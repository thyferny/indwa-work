/**
 * ClassName OutputObject.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator;

/**
 * 
 * @author Eason
 *          
 */
public abstract class OutputObject implements ConsumerProducer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6597732449994045294L;
	private String source = null;

	public OutputObject() {
	}

	public String getName() {
		Class c = this.getClass();
		return c.getName().substring(c.getName().lastIndexOf(".") + 1);
	}

	public String toResultString() {
		return toString();
	}

    public void setSource(String sourceName) {
        this.source = sourceName;
    }

    
    public String getSource() {
        return source;
    }

	public ConsumerProducer copy() {
		return this;
	}
}
