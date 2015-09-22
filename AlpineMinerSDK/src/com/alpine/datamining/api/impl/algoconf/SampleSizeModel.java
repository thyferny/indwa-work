/**
 * ClassName  SampleSizeModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */

public class SampleSizeModel {
	public static final String TAG_NAME="SampleSizeModel";

	List<Integer> sampleIdList = null ;// 1,2,3
	List<Double> sampleSizeList = null;// 10,20,70
	
	public SampleSizeModel(List<Integer> sampleIdList,
			List<Double> sampleSizeList) {
		super();
		this.sampleIdList = sampleIdList;
		this.sampleSizeList = sampleSizeList;
	}
	public SampleSizeModel( ) {
		super();
	 
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sampleIdList == null) ? 0 : sampleIdList.hashCode());
		result = prime * result
				+ ((sampleSizeList == null) ? 0 : sampleSizeList.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleSizeModel other = (SampleSizeModel) obj;
		if (sampleIdList == null) {
			if (other.sampleIdList != null)
				return false;
		} else if (!ListUtility.equalsFocusOrder(sampleIdList, other.sampleIdList))
			return false;
		if (sampleSizeList == null) {
			if (other.sampleSizeList != null)
				return false;
		} else if (!ListUtility.equalsFocusOrder(sampleSizeList, other.sampleSizeList)  )
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SampleSizeModel [sampleIdList=" + sampleIdList
				+ ", sampleSizeList=" + sampleSizeList + "]";
	}
	public List<Integer> getSampleIdList() {
		return sampleIdList;
	}
	
	public void setSampleIdList(List<Integer> sampleIdList) {
		this.sampleIdList = sampleIdList;
	}
	
	public List<Double> getSampleSizeList() {
		return sampleSizeList;
	}
	
	public void setSampleSizeList(List<Double> sampleSizeList) {
		this.sampleSizeList = sampleSizeList;
	}
	
	
}
