/**
 * ClassName  QuantileItemBinNumeric.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.variable;

import java.util.List;

import com.alpine.utility.common.DoubleUtil;
import com.alpine.utility.common.ListUtility;


/**
 * @author John Zhao
 *
 */
public class AnalysisQuantileItemBinNumeric extends AbstractQuantileItemBin {
	public static final String TAG_NAME="QuantileItemBinNumeric";
	public static final String ATTR_START_FROM="startFrom";
	public static final String ATTR_END_TO="endTo";
 
	//>=
	private Double startFrom =null;
	private Double endTo = null;
	
	//222,222,33, in xml...
	private List <Double> values=null;

	//for step run
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}else if (obj instanceof AnalysisQuantileItemBinNumeric==false){
			return false;
		}else{
			AnalysisQuantileItemBinNumeric qItem= (AnalysisQuantileItemBinNumeric) obj;
			if(getBinType()==BIN_TYPE_RANGE){
				return startFrom.equals(qItem.getStartFrom())
				&&endTo.equals(qItem.getEndTo())
						&&getBinIndex()==qItem.getBinIndex()
						&&getBinType()==qItem.getBinType();
			}else {
			return 
				 getBinIndex()==qItem.getBinIndex()
				&&getBinType()==qItem.getBinType()
				&&ListUtility.equalsIgnoreOrder(values, qItem.getValues());
			} 
		}
		 
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		if(getBinType()==BIN_TYPE_COLLECTION){
			sb.append(DoubleUtil.doubleListToString(values,COLLECTION_SEPARATOR));
		}else {
			if(startFrom!=null&&endTo!=null)
		 	sb.append(String.valueOf(startFrom)).append("-").append(endTo) ;
		}
		return sb.toString();
		
	}
	
	public List<Double> getValues() {
		return values;
	}
	public void setValues(List<Double> values) {
		this.values = values;
	}
	public Double getStartFrom() {
		return startFrom;
	}
	public void setStartFrom(Double startFrom) {
		this.startFrom = startFrom;
	}
	public Double getEndTo() {
		return endTo ;
	}
	public void setEndTo(Double endTo) {
		this.endTo = endTo;
	}

	public boolean isValid() {

		if(getBinType()==BIN_TYPE_COLLECTION){
			return values!=null&&values.size()>0;
			
		}else if(getBinType()==BIN_TYPE_RANGE){
			return ( startFrom!=null ) 	&&( endTo!=null )
						 ;
		}else{
			return true;
		}
	}
	public AnalysisQuantileItemBinNumeric clone(){
		AnalysisQuantileItemBinNumeric clone=new AnalysisQuantileItemBinNumeric();
		clone.setBinIndex(getBinIndex());
		clone.setBinType(getBinType());
		if(getBinType()==BIN_TYPE_RANGE){
			clone.setStartFrom(startFrom);
			clone.setEndTo(endTo) ;
		}else{
			clone.setValues(ListUtility.cloneDoubleList(getValues())) ;
		
		}
			
		return clone;
	}

}
