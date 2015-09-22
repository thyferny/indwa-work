/**
 * ClassName  JoinFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin;

import com.alpine.utility.file.StringUtil;


/**
 * @author Jeff Dong
 *
 */
public class AnalysisHadoopJoinFile{
 	
	String file;
	String operatorModelID;
	
	public AnalysisHadoopJoinFile(String file,String operatorModelID){
		this.file=file;
		this.operatorModelID = operatorModelID;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getOperatorModelID() {
		return operatorModelID;
	}

	public void setOperatorModelID(String operatorModelID) {
		this.operatorModelID = operatorModelID;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisHadoopJoinFile(file,operatorModelID);
	}

	
	public   boolean equals( Object obj){
		if(obj==null||(obj instanceof AnalysisHadoopJoinFile) == false){
			return false;
		}
		AnalysisHadoopJoinFile joinTable=(AnalysisHadoopJoinFile ) obj;
		return StringUtil.safeEquals(operatorModelID,joinTable.getOperatorModelID());
	}
	
}