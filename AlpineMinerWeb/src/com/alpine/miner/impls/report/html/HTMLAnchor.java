/**
 * ClassName :HTMLAnchor.java
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
public class HTMLAnchor extends AbstractElement {
	private String href;
	private String target; 
 

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public static final String tag = TAG_ANCHOR;

	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	public HTMLAnchor(String id,String cssClass,String target,String href, List<HTMLElement> children) {
		super( tag,id,cssClass, null, children);
		this.href = href;
		this.target=target;
	}
	
	public HTMLAnchor(String href ,String target) {
		super( tag,null,null, null, null);
		this.href = href;
		this.target=target;
	}
	
	public HTMLAnchor() {
		super( tag );
	}

 
	public String getHref() {
		return href;
	}

	public void setHref( String href) {
		this.href = href;
	}

	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" target=\"").append(getTarget()).append("\" ") ;
		buffer.append(" href=\"").append(getHref()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}

}
