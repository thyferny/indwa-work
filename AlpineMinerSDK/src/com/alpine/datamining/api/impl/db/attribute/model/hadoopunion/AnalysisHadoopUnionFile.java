/**
 * ClassName  AnalysisHadoopUnionFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopunion;

import com.alpine.utility.file.StringUtil;


/**
 * @author john zhao 
 *
 */
public class AnalysisHadoopUnionFile    {
 
 
	
	String file;
	String operatorModelID;
	
	

	public AnalysisHadoopUnionFile(String file,String operatorModelID){
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
		return new AnalysisHadoopUnionFile(file,operatorModelID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		else if (obj == null){
			return false;
		}
		else if (getClass() != obj.getClass()){
			return false;
		}
		else{
			AnalysisHadoopUnionFile other = (AnalysisHadoopUnionFile) obj;
			return StringUtil.safeEquals(getOperatorModelID(),other.getOperatorModelID());
		}
	}

	
}