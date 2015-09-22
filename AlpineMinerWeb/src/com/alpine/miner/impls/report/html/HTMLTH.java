/**
 * ClassName :HTMLTH.java
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
public class HTMLTH extends AbstractElement {
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	public HTMLTH( String id,String css,
			List<HTMLAttribute> attributes,  List<HTMLElement> children) {
		super( tag,id,css, attributes, children);
	}

	public HTMLTH( ) {
		super( tag);
	}
	
	
	public static final String tag = TAG_TH;




}
