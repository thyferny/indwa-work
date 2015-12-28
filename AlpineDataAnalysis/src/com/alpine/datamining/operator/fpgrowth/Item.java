
package com.alpine.datamining.operator.fpgrowth;

import java.io.Serializable;


public interface Item extends Comparable<Item>, Serializable {


	public long getFrequency();


	public void increaseFrequency(long frequency);

	public String toString();
	
}
