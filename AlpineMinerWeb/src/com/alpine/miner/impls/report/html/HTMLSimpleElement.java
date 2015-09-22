/**
 * ClassName :HTMLSimpleElement.java.java
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
//for extention use only...
public class HTMLSimpleElement extends AbstractElement {
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param textValue
	 */
	//HTMLAttribute is used for extestion
	public HTMLSimpleElement( String tag, String id,String css,
			List<HTMLAttribute> attributes,  List<HTMLElement> children) {
		super( tag, id,css,attributes, children);
	}
  

}
