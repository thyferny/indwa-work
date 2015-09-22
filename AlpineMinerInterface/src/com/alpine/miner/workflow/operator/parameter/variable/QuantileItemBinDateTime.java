/**
 * ClassName  QuantileItemBinNumeric.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.utility.file.StringUtil;


/**
 * support the date, time and datetime 3 
 * @author John Zhao
 *
 */
public class QuantileItemBinDateTime extends AbstractQuantileItemBin {
	public static final String TAG_NAME="QuantileItemBinDateTime";


	
	public static final String ATTR_DATE_START_FROM="startDate";
	public static final String ATTR_DATE_END_TO="endDate";
	
	public static final String ATTR_TIME_START_FROM="startTime";
	public static final String ATTR_TIME_END_TO="endTime";
 
	//>=
	private String startDate ="";

	private String endDate = "";
	
	private String startTime ="";
	
	private String endTime = "";
	
	//222,222,33, in xml...
	private List <String> values=null;

	//for step run
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}else if (obj instanceof QuantileItemBinDateTime==false){
			return false;
		}else{
			QuantileItemBinDateTime qItem= (QuantileItemBinDateTime) obj;
			if(startDate!=null&&startTime!=null){	
				return startDate.equals(qItem.getStartDate())
					&&endDate.equals(qItem.getEndDate())
					&&startTime.equals(qItem.getStartTime())
					&&endTime.equals(qItem.getEndTime())
					&&getBinIndex()==qItem.getBinIndex()
					&&getBinType()==qItem.getBinType();
			}
			else if(startDate==null){
				return  
				 startTime.equals(qItem.getStartTime())
				&&endTime.equals(qItem.getEndTime())
				&&getBinIndex()==qItem.getBinIndex()
				&&getBinType()==qItem.getBinType();
			}else if(startTime!=null){
				return startDate.equals(qItem.getStartDate())
				&&endDate.equals(qItem.getEndDate())				
				&&getBinIndex()==qItem.getBinIndex()
				&&getBinType()==qItem.getBinType();
			}else{
				return true;
			}
			
		}
		 
	}
	
	public String toString(){   
		StringBuffer sb = new StringBuffer();
		if(getBinType()==BIN_TYPE_COLLECTION){
			sb.append(StringUtil.listToString(values, COLLECTION_SEPARATOR));
		}else{
			if(startDate!=null){
				sb.append(startDate  ) ;
			}
			if(startTime!=null){
				sb.append(" ").append(startTime) ;
			 }
			if(sb.length()>0){
				sb.append(RANGE_SEPARATOR);
			}
			if(endDate!=null){
				sb.append(endDate) ;
			}
			if(endTime!=null){
				sb.append(" ").append(endTime) ;
			 }
		}
		return sb.toString();
		
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute( ATTR_BIN_INDEX,String.valueOf(getBinIndex()));
		element.setAttribute( ATTR_BIN_TYPE,String.valueOf(getBinType()));	
		if(values!=null&&values.size()>0){
			element.setAttribute( ATTR_VALUES,StringUtil.listToString( values, COLLECTION_SEPARATOR));
		}
		if(getStartDate()!=null){
			element.setAttribute( ATTR_DATE_START_FROM,String.valueOf(getStartDate()));
		}
		if(getEndDate()!=null){
			element.setAttribute( ATTR_DATE_END_TO,String.valueOf(getEndDate()));
		}
		if(getStartTime()!=null){
			element.setAttribute( ATTR_TIME_START_FROM,String.valueOf(getStartTime()));
		}
		if(getEndTime()!=null){
			element.setAttribute( ATTR_TIME_END_TO,String.valueOf(getEndTime()));
		}
		return element;
	}	

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	/**
	 * @return
	 */
	public boolean isValid() {

		if(getBinType()==BIN_TYPE_COLLECTION){
			return values!=null&&values.size()>0;
			
		}else if(getBinType()==BIN_TYPE_RANGE){
			return ((startDate!=null&&startDate.trim().length()>0)
				&& (endDate!=null&&endDate.trim().length()>0))||
				 ((startTime!=null&&startTime.trim().length()>0)
							&& (endTime!=null&&endTime.trim().length()>0));
		}else{
			return true;
		}
	}


	public QuantileItemBinDateTime clone(){
		QuantileItemBinDateTime clone=new QuantileItemBinDateTime();
		clone.setBinIndex(getBinIndex());
	 
		
			clone.setStartDate(startDate);
			clone.setEndDate(endDate) ;
			
			clone.setStartTime(startTime) ;
			clone.setEndTime(endTime) ;
		 
			
		return clone;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		
		return TAG_NAME;
	} 
}
