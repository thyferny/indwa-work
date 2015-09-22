/**
 * ClassName :HTMLTR.java
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
public class HTMLTR extends AbstractElement {

	public static final String tag = TAG_TR;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLTR(String id,String css,  List<HTMLElement> children) {
		super( tag,id,css, null, children);
	}


	public HTMLTR(  ) {
		super( tag);
	}

}
