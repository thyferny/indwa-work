/**
 * ClassName AnalyticResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.api.AnalyticFlowMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticResult;


/**
 * @author John Zhao
 * 
 */
public class AnalyticResultImpl implements AnalyticResult {
 
	List<AnalyticOutPut> outPuts;
	private AnalyticFlowMetaInfo analyticMetaInfo;
	private String processID;
 

	public AnalyticResultImpl(String processID) {
		this.processID=processID;
		// TODO Auto-generated constructor stub
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticResult#getOutPuts()
	 */
	public List<AnalyticOutPut> getOutPuts() {
		return outPuts;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticResult#setOutPuts(java.util.List)
	 */
	public void setOutPuts(List<AnalyticOutPut> outPuts) {
		this.outPuts = outPuts;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticResult#getAnalyticMetaInfo()
	 */
	@Override
	public AnalyticFlowMetaInfo getAnalyticMetaInfo() {
		// TODO Auto-generated method stub
		return analyticMetaInfo;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticResult#setAnalyticMetaInfo(com.alpine.datamining.api.AnalyticMetaInfo)
	 */
	@Override
	public void setAnalyticMetaInfo(AnalyticFlowMetaInfo metaInfo) {
		this.analyticMetaInfo=metaInfo;
		
	}

	@Override
	public String getProcessID() {
		// TODO Auto-generated method stub
		return processID;
	}
	
	public String toString(){
		StringBuffer str=new StringBuffer();
		str.append("Process id:"+processID+"\n");
		str.append("out puts========================\n");
		for (Iterator iterator = outPuts.iterator(); iterator.hasNext();) {
			AnalyticOutPut outPut = (AnalyticOutPut) iterator.next();
			str.append(outPut.toString()+"\n");
			
		}
		str.append("out puts========================\n");
		return str.toString();
	}

}
