/**
 * ClassName :FlowReporter.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhaoyong
 * 
 */
public class ReporterModel {
	String title = null;
	String description = null;
	String author = null;
	Date time = null;
	List<Chapter> chapters = null;
	List<IndexItem> index = null;
	
	public List<IndexItem> getIndex() {
		return index;
	}

	public void setIndex(List<IndexItem> index) {
		this.index = index;
	}
	
	public void addIndexItem(IndexItem child) {
		if(index==null){
			index= new ArrayList<IndexItem>();
		}
		index.add(child) ;
	}
	
	public void appendChapter(Chapter chapter){
		if(chapters==null){
			chapters= new ArrayList<Chapter> ();
		}
		if(chapter!=null&&chapters.contains(chapter) ==false){
			chapters.add(chapter) ;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

}
