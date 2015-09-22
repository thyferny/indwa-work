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

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/** category only support collection values
 * @author John Zhao
 *
 */
public class AnalysisQuantileItemBinCategory extends AbstractQuantileItemBin {
	public static final String TAG_NAME="QuantileItemBinCategory";
 
	private List<String> values;
	
	/**
	 * @param binIndex
	 * @param binType
	 * @param cloneStringList
	 */
	public AnalysisQuantileItemBinCategory(int binIndex, int binType,
			List<String> values) {
		this.values=values;
		super.setBinIndex(binIndex);
		super.setBinType(binType) ;
	}
	public AnalysisQuantileItemBinCategory(){
	 
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
	public void addValue(String value){
		 if(values==null){
			 values= new ArrayList<String>();
		 }
		 
		 if(values.contains(value) ==false){
			 values.add(value);
		 }
	}
	
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}else if (obj instanceof AnalysisQuantileItemBinCategory==false){
			return false;
		}else{
			AnalysisQuantileItemBinCategory qItem= (AnalysisQuantileItemBinCategory) obj;
			return ListUtility.equalsIgnoreOrder(values, qItem.getValues())		 
						&&getBinIndex()==qItem.getBinIndex();
		}
	}
	
 
	public String toString(){
		StringBuffer sb = new StringBuffer();
		if(getBinType()==BIN_TYPE_REST_VALUES){
			return "Rest Values";
		}
		if(values!=null){
			for (int i = 0; i < values.size(); i++) {
				String value=values.get(i) ;
				if(i!=0){
					sb=sb.append(",");
				}
				sb=sb.append("\"").append(value).append("\"");
 			}
 
		}
		return sb.toString();
		
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.variable.QuantileItemBin#isValid()
	 */
	@Override
	public boolean isValid() {
		if(getBinType()==BIN_TYPE_COLLECTION){
			return values!=null&&values.size()>0;
		}else{
			return true;
		}
	}

	public AnalysisQuantileItemBinCategory clone(){
		AnalysisQuantileItemBinCategory clone=new AnalysisQuantileItemBinCategory(getBinIndex(),getBinType(),ListUtility.cloneStringList(getValues()));
 		return clone;
	}

	
}
