package com.alpine.miner.framework;

import java.util.ArrayList;
import java.util.List;
/**   
 * ClassName:TableInfo 
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 *  
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class TableInfo {

	private String name;
	private List<ColumnInfo> list = new ArrayList<ColumnInfo>();
	private List<String[]> condition = new ArrayList<String[]>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addColumn(ColumnInfo col){
		this.list.add(col);
	}
	public void setColumnList(List<ColumnInfo> list){
		this.list = list;
	}
	public List<ColumnInfo> getColumnList(){
		return this.list;
	}
	public void addCondition(String[] cond){
		condition.add(cond);
	}
	public List<String[]> getCondition(){
		return this.condition;
	}
	public void setCondition(List<String[]> list){
		this.condition = list;
	}
}
