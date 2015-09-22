/**
 * ClassName AnalysisHadoopUnionModel.java
 *
 * Version information: 1.00
 *
 * Date: 2012-10-29
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.pigexe;

import com.alpine.utility.file.StringUtil;

public class PigInputMapItem {

	String inputUUID;
	String pigAliasName;
	
	public PigInputMapItem(){
		
	}
	  

	public String getInputUUID() {
		return inputUUID;
	}

	public void setInputUUID(String inputUUID) {
		this.inputUUID = inputUUID;
	}

	public String getPigAliasName() {
		return pigAliasName;
	}

	public void setPigAliasName(String pigAliasName) {
		this.pigAliasName = pigAliasName;
	}

	@Override
	public String toString() {
		return "PigInputMapItem [inputUUID=" + inputUUID + ", pigAliasName="
				+ pigAliasName + "]";
	}

 

	@Override
	public boolean equals(Object obj) {
		return StringUtil.safeEquals(inputUUID,((PigInputMapItem)obj).getInputUUID())
				&&StringUtil.safeEquals(pigAliasName,((PigInputMapItem)obj).getPigAliasName());
	}


	public PigInputMapItem(String inputUUID, String pigAliasName) {
		super();
		this.inputUUID = inputUUID;
		this.pigAliasName = pigAliasName;
	}
	
	public PigInputMapItem clone(){
		 
		return new PigInputMapItem(inputUUID,pigAliasName);
		
	}
	
}
