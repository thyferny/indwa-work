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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 *
 */
public abstract class AbstractElement implements HTMLElement {
	String tag = null;
	String id = null;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	String cssClass= null;
	//if have text value, will have no other children

	
	List<HTMLAttribute> attributes =null;

 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.report.html.HTML#getTag()
	 */
	@Override
	public String getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.report.html.HTML#setTag(java.lang.String)
	 */
	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.report.html.HTML#getAttributes()
	 */
	@Override
	public List<HTMLAttribute> getAttributes() {
		return attributes;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.report.html.HTML#setAttributes(java.util.List)
	 */
	@Override
	public void setAttributes(List<HTMLAttribute> attributes) {
		this.attributes = attributes;
	}

	List<HTMLElement> children = null; 

	/**
	 * @param style
	 * @param tag
	 * @param attributes
	 * @param children 
	 */
	public AbstractElement( String tag,String id,String cssClass,
			List<HTMLAttribute> attributes, List<HTMLElement> children) {
		this.tag=tag;
		if(StringUtil.isEmpty(cssClass)==false){
			this.cssClass=cssClass;
		}
		this.id=id;
		this.attributes=attributes; 
		this.children=children;
	}
	
	/**
	 * @param style2
	 * @param tag2
	 * @param attributes2
	 */
	public AbstractElement( String tag, String id,String cssClass) {
		this( tag,  id, cssClass,null,null) ;
	}
	
	/**
	 * @param tag2
	 */
	public AbstractElement( String tag , String id ) {
		this( tag,  id, null, null,null) ;
	}
	
	public AbstractElement( String tag  ) {
		this( tag,  null, null, null,null) ;
	}

	public List<HTMLElement> getChildren() {
		return children;
	}

	public void setChildren(List<HTMLElement> children) {
		this.children = children;
	}

	public void appendChild(HTMLElement child) throws Exception{
		if(child==null){
			throw new Exception ("child can not be null");
		}
		if(children==null){
			children= new ArrayList<HTMLElement> ();
		}
		children.add(child) ;
	}
	
	public void appendChilds(List<HTMLElement> htmlElements) throws Exception { 
		if(htmlElements!=null){
			for (Iterator<HTMLElement> iterator = htmlElements.iterator(); iterator.hasNext();) {
				HTMLElement ele =   iterator.next();
				appendChild(ele) ;
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.report.html.HTMLElement#appendToHTML(java.lang.StringBuffer)
	 */
	@Override
	public void appendToHTML(StringBuffer buffer) {
		appendTagStart(buffer);
		if(children!=null){
			for (int i = 0; i < children.size(); i++) {
				HTMLElement child = children.get(i) ;
				child.appendToHTML(buffer) ;
			}
		 
		}
		appendTagEnd(buffer);
		
	}
	
	protected void appendTagStart(StringBuffer buffer){
		buffer.append("<").append(getTag()); 
		if(StringUtil.isEmpty(getId())==false){
			buffer.append(" id =\"").append(getId()).append("\" ");  
		}
		
		if(StringUtil.isEmpty(getCssClass())==false){
			buffer.append(" class =\"").append(getCssClass()).append("\" ");  
		}
		appendAttributes(buffer);
		buffer.append(">") ;
	}

	protected void appendAttributes(StringBuffer buffer) {
		if(attributes!=null){
			for (Iterator<HTMLAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
				HTMLAttribute attr =   iterator.next();
				attr.appendToHTML(buffer) ;
			}
		}
	}
	
	protected void appendTagEnd(StringBuffer buffer){
		buffer.append("</").append(getTag()).append(">") ;
	}
	
	
	public StringBuffer toHTML(){
		StringBuffer buffer=new StringBuffer();
		appendToHTML(buffer);
		return buffer ;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer=new StringBuffer();
		appendToHTML(buffer);
		return buffer.toString() ;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

}
