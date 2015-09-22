/**
 * ClassName :HTMLAttribute.java
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
public class HTMLAttribute {
 
	
	String name;
	String value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	public void appendToHTML(StringBuffer buffer){
		
		buffer.append(" ").append(name).append("=\"").append(value).append("\" ") ;
	}
	
	public StringBuffer toHTML(){
		StringBuffer buffer=new StringBuffer();
		appendToHTML(buffer);
		return buffer ;
	}

}
