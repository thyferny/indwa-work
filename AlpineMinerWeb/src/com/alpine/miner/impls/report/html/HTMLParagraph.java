/**
 * ClassName :HTMLParagraph.java
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
public class HTMLParagraph extends AbstractElement {
	public static final String tag = TAG_P;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	public HTMLParagraph(  String id,String css,
			List<HTMLAttribute> attributes,  List<HTMLElement> children) {
		super( tag,    id,  css,attributes, children);
 
	}
	
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	public HTMLParagraph( String id,String css,
			List<HTMLAttribute> attributes) {
		super( tag,  id,  css,attributes, null);
 
	}
	
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	public HTMLParagraph( String id,String css ) {
		super( tag,  id,  css,null, null);
 
	}

}
