
package com.alpine.datamining.operator;

import java.io.Serializable;



public interface ConsumerProducer extends Serializable {


    public void setSource(String sourceName);
    public String getSource();
	public ConsumerProducer copy();

    
}
