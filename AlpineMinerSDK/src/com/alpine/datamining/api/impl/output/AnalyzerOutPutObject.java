/**
 * ClassName DataAnlyticOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractDBTableOutPut;

/**
 * @author John Zhao
 *
 */
public class AnalyzerOutPutObject extends AbstractDBTableOutPut   {
// Object  like doublelist and double data
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1155669583658641360L;
	private Object outPutObject;
	
	public AnalyzerOutPutObject(Object outPutObject){
		this.outPutObject=outPutObject;
	}

	public Object getOutPutObject() {
		return outPutObject;
	}

	public void setOutPutObject(Object outPutObject) {
		this.outPutObject = outPutObject;
	}

	public String toString(){
		if(outPutObject!=null){
			return outPutObject.toString();
		}else{
			return "";
		}
	}
 
}
