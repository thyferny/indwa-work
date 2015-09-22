/**
 * ClassName :HTMLBody.java
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
public class HTMLBody extends AbstractElement {

	public static final String tag = TAG_BODY;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLBody(  List<HTMLElement> children) {
		super( tag, null,null, null,children);
		 
	}
	
	public HTMLBody(  ) {
		super( tag,  null,null, null, null);
		 
	}




}
