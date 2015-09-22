package com.alpine.miner.framework;

import java.util.Hashtable;
import java.util.List;
/**   
 * ClassName:DataStore  
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 *  
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class DataStore {
	
	private int pageSize = 10;
	private int pageNo = 0;
	private int totalCount = 0;
	private String name;
	private Hashtable<String,String> colsParam = new Hashtable<String, String>();
	private Hashtable<String,String> colsMapUI = new Hashtable<String, String>(); 
	private RowInfo row;
	private List<String[]> workFlowLinks;
	private Exception error;
	public Exception getError() {
		return error;
	}
	public void setError(Exception error) {
		this.error = error;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public void addColumnParam(String column,String value){
		colsParam.put(column, value);
	}
	public String getColumnValue(String column){
		return colsParam.get(column);
	}
	public Hashtable<String,String> getColumnParam(){
		return this.colsParam;
	}
	public void addColsMapUI(String column,String uiColumn){
		colsMapUI.put(column,uiColumn);
	}
	public String getColsMapUI(String column){
		return colsMapUI.get(column);
	}
	public Hashtable<String,String> getColsMapUI(){
		return colsMapUI;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public RowInfo getRow() {
		return row;
	}
	public void setRow(RowInfo row) {
		this.row = row;
	}
	
	public List<String[]> getWorkFlowLinks() {
		return workFlowLinks;
	}
	public void setWorkFlowLinks(List<String[]> workFlowLinks) {
		this.workFlowLinks = workFlowLinks;
	}
}
