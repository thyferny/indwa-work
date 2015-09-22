/**
 * ClassName :HTMLTableElement.java
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
public class HTMLTable extends AbstractElement {

	public static final String tag = TAG_TABLE;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLTable( String id,String css,
			List<HTMLAttribute> attributes, List<HTMLElement> children) {
		super( tag,id,css ,attributes, children);
		 
	}
	
	public HTMLTable( String id,String cssClass) {
		super( tag, id,cssClass);
		 
	}


}
