/**
 * ClassName  SampleSizeModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.sampling;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;

/**
 * @author Jeff Dong
 *
 */

public class SampleSizeModel extends AbstractParameterObject{
	public static final String TAG_NAME="SampleSizeModel";
	
	public static final String SAMPLES_ID_TAG_NAME="sampleIDs";
	
	public static final String SAMPLES_SIZE_TAG_NAME="sampleSizes";
	
	public static final String SAMPLE_ID_TAG_NAME="sampleID";
	
	public static final String SAMPLE_SIZE_TAG_NAME="sampleSize";

	List<String> sampleIdList = null ;// 1,2,3
	List<String> sampleSizeList = null;// 10,20,70
	
	public SampleSizeModel(List<String> sampleIdList,
			List<String> sampleSizeList) {
		super();
		this.sampleIdList = sampleIdList;
		this.sampleSizeList = sampleSizeList;
	}
	public SampleSizeModel() {
		super();
	}
	
	public Element toXMLElement(Document xmlDoc) {	 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getSampleIdList()!=null){
			for(String s:getSampleIdList()){
				Element groupByEle=xmlDoc.createElement(SAMPLES_ID_TAG_NAME);
				groupByEle.setAttribute(SAMPLE_ID_TAG_NAME, s);
				element.appendChild(groupByEle);		
			}
		}
		if(getSampleSizeList()!=null){
			for(String s:getSampleSizeList()){
				Element groupByEle=xmlDoc.createElement(SAMPLES_SIZE_TAG_NAME);
				groupByEle.setAttribute(SAMPLE_SIZE_TAG_NAME, s);
				element.appendChild(groupByEle);		
			}
		}
		return element;
	}
	
	public static SampleSizeModel fromXMLElement(Element element) {
		NodeList sampleIdItemList = element.getElementsByTagName(SAMPLES_ID_TAG_NAME);
		List<String> sampleIdList=new ArrayList<String>();
		for (int i = 0; i < sampleIdItemList.getLength(); i++) {
			if (sampleIdItemList.item(i) instanceof Element ) {
				String sampleId=((Element)sampleIdItemList.item(i)).getAttribute(SAMPLE_ID_TAG_NAME);
				sampleIdList.add(sampleId);
			}
		}
		
		NodeList sampleSizeItemList = element.getElementsByTagName(SAMPLES_SIZE_TAG_NAME);
		List<String> sampleSizeList=new ArrayList<String>();
		for (int i = 0; i < sampleSizeItemList.getLength(); i++) {
			if (sampleSizeItemList.item(i) instanceof Element ) {
				String sampleSize=((Element)sampleSizeItemList.item(i)).getAttribute(SAMPLE_SIZE_TAG_NAME);
				sampleSizeList.add(sampleSize);
			}
		}
		
		SampleSizeModel sampleSizeModel=new SampleSizeModel(sampleIdList,sampleSizeList);
		
		return sampleSizeModel;
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
	
	@SuppressWarnings("unchecked")
	public SampleSizeModel clone() throws CloneNotSupportedException {
		SampleSizeModel model= new SampleSizeModel();
		
		model.setSampleIdList(ParameterUtility.cloneObjectList(getSampleIdList()));
		model.setSampleSizeList(ParameterUtility.cloneObjectList(getSampleSizeList()));
		
		return model;
	}
	
	@Override
	public String toString() {
		return "SampleSizeModel [sampleIdList=" + sampleIdList
				+ ", sampleSizeList=" + sampleSizeList + "]";
	}

	public List<String> getSampleIdList() {
		return sampleIdList;
	}
	public void setSampleIdList(List<String> sampleIdList) {
		this.sampleIdList = sampleIdList;
	}
	public List<String> getSampleSizeList() {
		return sampleSizeList;
	}
	public void setSampleSizeList(List<String> sampleSizeList) {
		this.sampleSizeList = sampleSizeList;
	}
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
}
