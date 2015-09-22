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



/**
 * @author zhaoyong
 *
 */
public class HTMLMeta extends AbstractElement {
	private String http_equiv	;
	private String content;
	public String getHttp_equiv() {
		return http_equiv;
	}


	public void setHttp_equiv(String http_equiv) {
		this.http_equiv = http_equiv;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public String getContent_charset() {
		return content_charset;
	}


	public void setContent_charset(String content_charset) {
		this.content_charset = content_charset;
	}


	private String content_charset;


	public static final String TYPE_JS = "text/javascript";
	 


	public static final String tag = TAG_META;
	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children
	 */
	public HTMLMeta(  String charset) {
		super( tag, null, null,null,null);
		this.content_charset = charset;
		this.content="text/html" ;
		this.http_equiv = "Content-Type" ;
		 
		 
	}
 
	
	@Override
	protected void appendAttributes(StringBuffer buffer) {
		buffer.append(" http-equiv=\"").append(getHttp_equiv()).append("\" ") ;
		buffer.append(" content=\"").append(getContent()).append(";").append("charset=").append(getContent_charset()).append("\" ") ;
	
		super.appendAttributes(buffer) ;
 	}



 

}
