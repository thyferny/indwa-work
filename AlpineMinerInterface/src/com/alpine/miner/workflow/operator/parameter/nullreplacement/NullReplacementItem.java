package com.alpine.miner.workflow.operator.parameter.nullreplacement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

public class NullReplacementItem extends AnalysisNullReplacementItem implements XMLFragment {
 
	
	public NullReplacementItem(String columnName, String value, String type) {
		super(columnName,   value,   type);
	}
	 
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	@Override
	public void initFromXmlElement(Element element) {

	}

	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getColumnName());
		element.setAttribute(ATTR_NULL, getValue());
		element.setAttribute(ATTR_TYPE, getType());

		return element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new NullReplacementItem( columnName,value,type);
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof NullReplacementItem){
			 NullReplacementItem column = (NullReplacementItem) obj;
			 return ParameterUtility.nullableEquales(columnName ,column.getColumnName())
			    && ParameterUtility.nullableEquales(value ,column.getValue())
					 && ParameterUtility.nullableEquales(value ,column.getValue());
 
		 }else{
			 return false;
		 }
	 }

	public static NullReplacementItem fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		String nullValue=item.getAttribute(ATTR_NULL);
		String nullType=item.getAttribute(ATTR_TYPE);

		NullReplacementItem nullValues=new NullReplacementItem(columnName,nullValue,nullType);
		return nullValues;
	}
}
