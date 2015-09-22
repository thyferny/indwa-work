/**
 * ClassName :Paragraph.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

/**
 * @author zhaoyong
 *
 */
//simple text paragraph...
public class Paragraph extends AbstractReportElement{

	String content;
	
	/**
	 * @param id
	 * @param title
	 */
	protected Paragraph(String id, String title,String content) { 
		super(id, title);
		this.content=content;
	}
	protected Paragraph(  String content) { 
		super(String.valueOf(System.currentTimeMillis()),null);
		if(content==null){
			content="";
		}
		this.content=content;
	}

	
	protected Paragraph(String title, String content) { 
		super("output_"+String.valueOf(System.currentTimeMillis()),title);
		if(content==null){
			content="";
		}
		this.content=content;
	}


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
