/**
 * ClassName :HTMLHead.java
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
public class HTMLHead extends AbstractElement {

	public static final String tag = TAG_HEAD;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLHead(  List<HTMLElement> children) {
		super( tag, null,null, null,children);
		 
	}
	
	public HTMLHead(  ) {
		this( null);
		 
	}




}
