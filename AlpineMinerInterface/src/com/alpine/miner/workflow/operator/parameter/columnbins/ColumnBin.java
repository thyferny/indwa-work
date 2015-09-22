/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.columnbins;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 * 
 */
public class ColumnBin implements XMLFragment {

	public static final String TAG_NAME="ColumnBin";

	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_BIN = "bin";
	
	private static final String ATTR_TYPE = "type";
	
	private static final String ATTR_IS_MIN = "isMin";
	
	private static final String ATTR_MIN = "min";
	
	private static final String ATTR_IS_MAX = "isMax";
	
	private static final String ATTR_MAX = "max";
	
	private static final String ATTR_WIDTH = "width";
	
	public static final int TYPE_BY_NUMBER = 0;
	
	public static final int TYPE_BY_WIDTH = 1;
	
	private String columnName = null;
	private int type=TYPE_BY_NUMBER;
	private String bin = "10";//Default Value;
	private String width = "1.0";//Default Value;
	private String min= "0.0";//Default Value;
	private String max= "100.0";//Default Value;
	private boolean isMin=false;
	private boolean isMax=false;
	

	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}
	 
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}
	
	public boolean isMin() {
		return isMin;
	}

	public void setIsMin(boolean isMin) {
		this.isMin = isMin;
	}

	public boolean isMax() {
		return isMax;
	}

	public void setIsMax(boolean isMax) {
		this.isMax = isMax;
	}

	public ColumnBin( String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public ColumnBin( String columnName,String bin) {
		this.columnName = columnName;
		this.bin = bin;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getColumnName());
		element.setAttribute(ATTR_BIN, String.valueOf(getBin()));
		element.setAttribute(ATTR_TYPE, String.valueOf(getType()));
		element.setAttribute(ATTR_IS_MIN, String.valueOf(isMin()));
		element.setAttribute(ATTR_IS_MAX, String.valueOf(isMax()));
		element.setAttribute(ATTR_MIN, String.valueOf(getMin()));
		element.setAttribute(ATTR_MAX, String.valueOf(getMax()));
		element.setAttribute(ATTR_WIDTH, String.valueOf(getWidth()));
		return element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ColumnBin columnBin=new ColumnBin( columnName,bin);
		columnBin.setType(type);
		columnBin.setWidth(width);
		columnBin.setMin(min);
		columnBin.setMax(max);
		columnBin.setIsMin(isMin);
		columnBin.setIsMax(isMax);
		return columnBin;
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof ColumnBin){
			 ColumnBin column = (ColumnBin) obj;
			 return ParameterUtility.nullableEquales(columnName ,column.getColumnName())
			 && ParameterUtility.nullableEquales(bin ,column.getBin())
			 && ParameterUtility.nullableEquales(isMin ,column.isMin())
			 && ParameterUtility.nullableEquales(isMax ,column.isMax())
			 && ParameterUtility.nullableEquales(type ,column.getType())
			 && ParameterUtility.nullableEquales(width ,column.getWidth())
			 && ParameterUtility.nullableEquales(min ,column.getMin())
			 && ParameterUtility.nullableEquales(max ,column.getMax())
			 ;
 
		 }else{
			 return false;
		 }
	 }
 
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
 
		return TAG_NAME;
	}

	public static ColumnBin fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		String bin=item.getAttribute(ATTR_BIN);
		String isMin=item.getAttribute(ATTR_IS_MIN);
		String isMax=item.getAttribute(ATTR_IS_MAX);
		String min=item.getAttribute(ATTR_MIN);
		String max=item.getAttribute(ATTR_MAX);
		String width=item.getAttribute(ATTR_WIDTH);
		String type=item.getAttribute(ATTR_TYPE);
		ColumnBin columnBin=new ColumnBin(columnName,bin);
		if(!StringUtil.isEmpty(max)){
			columnBin.setMax(max);
		}
		if(!StringUtil.isEmpty(min)){
			columnBin.setMin(min);
		}	
		columnBin.setIsMin(StringUtil.isEmpty(isMin)?false:Boolean.parseBoolean(isMin));
		columnBin.setIsMax(StringUtil.isEmpty(isMax)?false:Boolean.parseBoolean(isMax));
		columnBin.setWidth(StringUtil.isEmpty(width)?"0.0":width);
		columnBin.setType(StringUtil.isEmpty(type)?0:Integer.parseInt(type));
		return columnBin;
	}
}