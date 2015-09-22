/**
 * ClassName :HTMLTextNode.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.html;


/**
 * @author zhaoyong
 * 
 */
public  class HTMLTextNode extends AbstractElement {

	String textValue;

	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	//this is special wothout tag!!!
	public HTMLTextNode(   String textValue) {
		super(  null);
		if(textValue==null){
			textValue="" ;
		}
		this.textValue = textValue;
	}
	//this is very important , text node have no tags!
	@Override
	public void appendToHTML(StringBuffer buffer) {
		 
		if(textValue!=null){
			buffer.append(textValue);
		}
		 
	}

}
