/**
 * AnalyzerOutPutBoxWhisker.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.output;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;

/**
 * @author Jimmy
 *
 */
public class AnalyzerOutPutBoxWhisker extends AbstractAnalyzerOutPut{

	/**
	 * box and whisker list contianer
	 */
	private static final long serialVersionUID = -7694172752648755657L;
	private List<BoxAndWhiskerItem> list = new ArrayList<BoxAndWhiskerItem>();
	private boolean isApprox;
    public List<BoxAndWhiskerItem> getItemList(){
		return list;
	}
	
	public void addItem(BoxAndWhiskerItem item){
		list.add(item);
	}
	public void setItemList(List<BoxAndWhiskerItem> list){
		this.list = list;
	}

    public void setApprox(boolean isApprox)
    {
        this.isApprox = isApprox;
    }

    public boolean isApprox()
    {
        return isApprox;
    }
}
