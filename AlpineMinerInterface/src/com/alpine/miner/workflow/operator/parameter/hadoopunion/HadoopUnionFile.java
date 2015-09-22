/**
 * ClassName  HadoopUnionFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopunion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUnionFile implements XMLFragment {
 
	public static final String TAG_NAME="UnionFileModel";
	
	private static final String ATTR_OPERATOR_ID="operID";//uuid
	private static final String ATTR_FILE = "file";
	
	String file;
	String operatorModelID;
	
	

	public HadoopUnionFile(String file,String operatorModelID){
		this.file=file;
		this.operatorModelID = operatorModelID;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getOperatorModelID() {
		return operatorModelID;
	}

	public void setOperatorModelID(String operatorModelID) {
		this.operatorModelID = operatorModelID;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_FILE, getFile());
		element.setAttribute(ATTR_OPERATOR_ID, getOperatorModelID());
		return element;
	}

	/**
	 * @param item
	 * @return
	 */
	public static HadoopUnionFile fromXMLElement(Element item) {
		String file=item.getAttribute(ATTR_FILE);
		String uuid= item.getAttribute(ATTR_OPERATOR_ID);
		return new HadoopUnionFile(file,uuid);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new HadoopUnionFile(file,operatorModelID);
	}

	
	public   boolean equals( Object obj){
		if(obj==null||(obj instanceof HadoopUnionFile) == false){
			return false;
		}
		HadoopUnionFile unionFile=(HadoopUnionFile ) obj;
		return ParameterUtility.nullableEquales(unionFile.getFile(), file);
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

	
}