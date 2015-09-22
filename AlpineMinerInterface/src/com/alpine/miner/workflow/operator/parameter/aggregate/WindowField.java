/**
 * ClassName :WindowField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.aggregate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author zhaoyong
 * 
 */
public class WindowField implements XMLFragment {
	public static final String TAG_NAME = "WindowField";

	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_DATATYPE = "dataType";

	private static final String ATTR_FUNCTION = "function";

	private static final String ATTR_SPECIFICATION = "specification";
	
	String resultColumn;
	String windowFunction;
	String windowSpecification;
	String dataType;

	/**
	 * @param resultColumn2
	 * @param windowFunction2
	 * @param windowSpecification2
	 * @param dataType2
	 */
	public WindowField(String resultColumn, String windowFunction,
			String windowSpecification, String dataType) {
		this.resultColumn = resultColumn;
		this.windowFunction = windowFunction;
		this.windowSpecification = windowSpecification;
		this.dataType = dataType;
	}

	public String getResultColumn() {
		return resultColumn;
	}

	public void setResultColumn(String resultColumn) {
		this.resultColumn = resultColumn;
	}

	public String getWindowFunction() {
		return windowFunction;
	}

	public void setWindowFunction(String windowFunction) {
		this.windowFunction = windowFunction;
	}

	public String getWindowSpecification() {
		return windowSpecification;
	}

	public void setWindowSpecification(String windowSpecification) {
		this.windowSpecification = windowSpecification;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getResultColumn());
		element.setAttribute(ATTR_DATATYPE, getDataType());
		element.setAttribute(ATTR_FUNCTION, getWindowFunction());
		element.setAttribute(ATTR_SPECIFICATION, getWindowSpecification());
		return element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new WindowField(resultColumn, windowFunction,
				windowSpecification, dataType);
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof WindowField) {
			WindowField field = (WindowField) obj;
			return ParameterUtility.nullableEquales(resultColumn,
					field.getResultColumn())
					&& ParameterUtility.nullableEquales(windowFunction,
							field.getWindowFunction())
					&& ParameterUtility.nullableEquales(windowSpecification,
							field.getWindowSpecification())
					&& ParameterUtility.nullableEquales(dataType,
							field.getDataType());

		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement
	 * (org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public static WindowField fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		String dataType=item.getAttribute(ATTR_DATATYPE);
		String function=item.getAttribute(ATTR_FUNCTION);
		String specification=item.getAttribute(ATTR_SPECIFICATION);
		WindowField windowField=new WindowField(columnName,function,specification,dataType);
		return windowField;
	}
}