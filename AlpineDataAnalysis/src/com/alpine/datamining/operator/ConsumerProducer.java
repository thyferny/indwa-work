/**
 * ClassName ConsumerProducer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator;

import java.io.Serializable;


/**
 * @author Eason
 */
public interface ConsumerProducer extends Serializable {


    public void setSource(String sourceName);
    public String getSource();
	public ConsumerProducer copy();

    
}
