/**
 * ClassName CompositeVisuliazationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.visual;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AbstractVisualizationOutPut;

/**
 * @author John Zhao
 *mutil output
 */
public class CompositeVisualizationOutPut extends AbstractVisualizationOutPut implements VisualizationOutPut {
	List<VisualizationOutPut> childOutPuts;

	
	public CompositeVisualizationOutPut (){
		childOutPuts =new ArrayList<VisualizationOutPut> ();	
	}
	public List<VisualizationOutPut> getChildOutPuts() {
		return childOutPuts;
	}

	public void setChildOutPuts(List<VisualizationOutPut> childOutPuts) {
		this.childOutPuts = childOutPuts;
	}
	public void addChildOutPut(VisualizationOutPut outPut){
		childOutPuts.add(outPut);
	}

}
  