/**
 * ClassName :Chapter.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoyong
 * 
 */
public class Chapter extends AbstractReportElement {
	/**
	 * @param id
	 * @param title
	 */
	protected Chapter(String id, String title,String chapterNumber) { 
		super(id, title);
		this.chapterNumber=chapterNumber;
	}
	//string , could be 1.1 2.3.2 ...
	String chapterNumber;
	// getfulltitle
 
	List<Paragraph> paragraphs;
	List<Chapter> subChapters;
	
	public void appendParagraph(Paragraph paragraph){ 
		if(paragraphs==null){
			paragraphs= new ArrayList<Paragraph> ();
		}
		if(paragraph!=null&&paragraphs.contains(paragraph) ==false){
			paragraphs.add(paragraph) ;
		}
	}
 
	public void appendSubChapter(Chapter chapter){
		if(subChapters==null){
			subChapters= new ArrayList<Chapter> ();
		}
		if(chapter!=null&&subChapters.contains(chapter) ==false){
			subChapters.add(chapter) ;
		}
	}
	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}
	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
	public List<Chapter> getSubChapters() {
		return subChapters;
	}
	public void setSubChapters(List<Chapter> subChapter) {
		this.subChapters = subChapter;
	}
	
	public String getChapterNumber() {
		return chapterNumber;
	}
	public void setChapterNumber(String chapterNumber) {
		this.chapterNumber = chapterNumber;
	}

	/**
	 * @param paragraphs2
	 */
	public void appendParagraphs(List<Paragraph> paras) { 
		if(paragraphs==null){
			paragraphs= new ArrayList<Paragraph> ();
		}
		if(paras!=null){
			paragraphs.addAll(paras) ;
		}
		
	}

	public void appendSubChapters(List<Chapter> chapters) {
		if(subChapters==null){
			subChapters= new ArrayList<Chapter> ();
		}
		if(chapters!=null){
			subChapters.addAll(chapters) ;
		}
		
	}
}
