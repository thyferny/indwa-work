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

import java.util.Iterator;

import com.alpine.utility.file.StringUtil;


/**
 * @author zhaoyong
 *
 */
public class HTMLScript extends AbstractElement {
	private String type;


	public static final String TYPE_JS = "text/javascript";

	private static final String TAG_SCRIPT = "script";
	public static final String tag = TAG_SCRIPT;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLScript( String type,String id,String css) {
		super( tag, id, css,null,null);
		if(StringUtil.isEmpty(type)==false){
			this.type=type;
		}else{
			this.type=TYPE_JS;
		}
		 
	}

	public HTMLScript( ) {
		super( tag, null, null,null,null);
 
			this.type=TYPE_JS;
 
		 
	}
	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" type=\"").append(getType()).append("\" ") ;
		super.appendAttributes(buffer) ;
 	}



	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
