/**
 * ClassName :HTMLImage.java.java
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
public class HTMLFrame extends AbstractElement {
	
	private String name; 
	private String src;

	public static final String tag = TAG_FRAME;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLFrame( String name,String src) {
		super( tag, null, null,null,null);
		this.name = name ;
		this.src =src;
		 
	}
	
	
	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getSrc() {
		return src;
	}



	public void setSrc(String src) {
		this.src = src;
	}
  
	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" name=\"").append(getName()).append("\" ") ;
		buffer.append(" src=\"").append(getSrc()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}



}
