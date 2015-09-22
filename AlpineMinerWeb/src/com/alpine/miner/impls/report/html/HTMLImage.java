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
public class HTMLImage extends AbstractElement {
	private String src;

	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public static final String tag = TAG_IMAGE;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLImage( String src,String id,String css) {
		super( tag, id, css,null,null);
		this.src=src;
		 
	}
	
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLImage( String src) {
		super( tag, null, null,null,null);
		this.src=src;
		 
	}
	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" src=\"").append(getSrc()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}



}
