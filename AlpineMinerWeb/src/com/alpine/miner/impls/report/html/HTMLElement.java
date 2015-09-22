/**
 * ClassName :HTMLElement.java
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
public interface HTMLElement {

	//---------
	public static final String TAG_HTML = "html";
	public static final String TAG_HEAD = "head";
	public static final String TAG_BODY = "body";
	//---------table 
	public static final String TAG_TABLE = "table";
	public static final String TAG_TR = "tr";
	public static final String TAG_TD = "td";
	public static final String TAG_TH = "th";
	public static final String TAG_IMAGE = "img";
	public static final String TAG_STYLE = "style";
	
	public static final String TAG_SCRIPT = "script";
	public static final String TAG_META = "meta";
	
	public static final String TAG_FRAMESET = "frameset";
	public static final String TAG_FRAME = "frame" ;
	//--------controler
	public static final String TAG_P = "p";
	public static final String TAG_ANCHOR = "A";
	public static final String TAG_HR = "HR";

	//---this is for the tree
	public static final String TAG_SPAN = "span";
	public static final String TAG_UL = "ul";
	public static final String TAG_LI = "li";
	
 

	public abstract String getTag();
	public abstract void setTag(String tag);

	public String getCssClass() ;
	public void setCssClass(String cssClass) ;
	
	public void appendToHTML(StringBuffer buffer);	
	public StringBuffer toHTML();
	
	public List<HTMLElement> getChildren() ;
	public void setChildren(List<HTMLElement> children);
	public void appendChild(HTMLElement child) throws Exception;
	public void appendChilds(List<HTMLElement> htmlElements) throws Exception ;
	
	//this is only for extenstion use 
	public abstract List<HTMLAttribute> getAttributes();
	public abstract void setAttributes(List<HTMLAttribute> attributes);

}