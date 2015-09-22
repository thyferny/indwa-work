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

import com.alpine.utility.file.StringUtil;


/**
 * @author zhaoyong
 *
 */
public class HTMLFrameSet extends AbstractElement {
	private String cols;

	public static final String COLS_DEFAULT = "15%,85%";

	
	public static final String tag = TAG_FRAMESET;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLFrameSet( String cols,String id,String css) {
		super( tag, id, css,null,null);
		if(StringUtil.isEmpty(cols)==false){
			this.cols=cols;
		}else{
			this.cols=COLS_DEFAULT;
		}
		 
	}
 

	public String getCols() {
		return cols;
	}

	public void setCols(String cols) {
		this.cols = cols;
	}

	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" cols=\"").append(getCols()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}



}
