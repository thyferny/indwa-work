/**
 * ClassName :HTMLHR.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.html;

import java.util.List;

/**
 * @author zhaoyong
 *
 */
public class HTMLHR extends AbstractElement {

	public static final String tag = TAG_HR;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLHR(  List<HTMLElement> children) {
		super( tag, null,null, null,children);
		 
	}
	
	public HTMLHR(  ) {
		this( null);
		 
	}
	
	//default for the chapter separater
	String color="#987cb9"; 
	String size ="3" ;
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}


	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" color=\"").append(getColor()).append("\" ") ;
		buffer.append(" size=\"").append(getSize()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}


	
}
