
package com.alpine.datamining.operator.fpgrowth;


public interface FrequencyStack {

	
	public void increaseFrequency(int stackHeight, long weight);

	
	public void popFrequency(int height);

	
	public long getFrequency(int height);
}
