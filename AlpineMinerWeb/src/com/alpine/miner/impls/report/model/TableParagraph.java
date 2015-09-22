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

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoyong
 *
 */
public class TableParagraph extends Paragraph{
	/**
	 * @param id
	 * @param title
	 * @param content
	 */
	protected TableParagraph(String id, String title, String[] tableHeader,List<List<Paragraph>> tableRows) { 
		super(id, title, null);
		this.tableHeader=tableHeader;
		//row is paragraph. each cell could be another table
		this.tableRows=tableRows;
	}
	/**
	 * @param string
	 */
	public TableParagraph(String id) {
		this(id,null,null,null) ;
	}
	String[] tableHeader;
	//each table cell could be a text or image
	List<List<Paragraph>> tableRows;
	public String[] getTableHeader() { 
		return tableHeader;
	}
	public void setTableHeader(String[] tableHeader) {
		this.tableHeader = tableHeader;
	}
 

	
	
	public List<List<Paragraph>> getTableRows() {
		return tableRows;
	}
	public void setTableRows(List<List<Paragraph>> tableRows) {
		this.tableRows = tableRows;
	}
	public void appendRow(List<Paragraph> row){ 
		if(tableRows==null){
			tableRows= new ArrayList<List<Paragraph>> ();
		}
		if(tableRows!=null&&tableRows.contains(row) ==false){
			tableRows.add(row) ;
		}
	}
}
