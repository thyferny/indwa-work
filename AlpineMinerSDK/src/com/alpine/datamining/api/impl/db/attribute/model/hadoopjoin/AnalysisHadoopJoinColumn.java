/**
 * ClassName  HadoopJoinColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin;


/**
 * @author Jeff Dong
 *
 */
public class AnalysisHadoopJoinColumn {

	
	String columnName;
	String newColumnName;
	String columnType;
	String fileName;
	String fileId;
	
	public AnalysisHadoopJoinColumn(String columnName, String newColumnName,String columnType,
			String fileName,String fileId) {
		this.columnName = columnName;
		this.newColumnName = newColumnName;
		this.fileName = fileName;
		this.fileId=fileId;
		this.columnType=columnType;
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getNewColumnName() {
		return newColumnName;
	}
	public void setNewColumnName(String newColumnNam) {
		this.newColumnName = newColumnNam;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public boolean equals(Object obj) {
		if(obj==null||(obj instanceof AnalysisHadoopJoinColumn) ==false){
			return false;
		}
		AnalysisHadoopJoinColumn joinColumn = (AnalysisHadoopJoinColumn) obj; 
		return  joinColumn.getColumnName().equals(columnName)
				&& joinColumn.getNewColumnName().equals(newColumnName)
				&& joinColumn.getFileName().equals(fileName)
				&& joinColumn.getFileId().equals(fileId)
				&& joinColumn.getColumnType().equals(columnType);

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisHadoopJoinColumn(columnName, newColumnName,columnType,fileName,fileId);
	}


}