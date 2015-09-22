/**
 * ClassName AnalysisHadoopUnionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopunion;

import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * 
 * @author john zhao
 *
 */
public class AnalysisHadoopUnionModel     {
	
	
	public static final String UNION = "UNION";
	public static final String UNION_ALL="UNION ALL";
	public static final String INTERSECT= "INTERSECT";
	public static final String EXCEPT ="EXCEPT";
	public static final String[] TABLE_SET_TYPE = new String[]{UNION,UNION_ALL,INTERSECT,EXCEPT};//
	
	String setType =UNION_ALL;
	public String getSetType() {
		return setType;
	}

	public void setSetType(String setType) {
		this.setType = setType;
	}

	public String getFirstTable() {
		return firstTable;
	}

	public void setFirstTable(String firstTable) {
		this.firstTable = firstTable;
	}
	String  firstTable =""; 

	//first is left (will use this column name and type)
	List<AnalysisHadoopUnionModelItem> outputColumns = null; 
	List<AnalysisHadoopUnionFile> unionFiles = null;
	 
	
	public List<AnalysisHadoopUnionFile> getUnionFiles() {
		return unionFiles;
	}

	public void setUnionFiles(List<AnalysisHadoopUnionFile> unionFiles) {
		this.unionFiles = unionFiles;
	}
	
	@Override
	public String toString() {
		return "HadoopUnionModel [  outputColumns=" + outputColumns + "]";
	}
	 
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((outputColumns == null) ? 0 : outputColumns.hashCode());
		result = prime * result
				+ ((unionFiles == null) ? 0 : unionFiles.hashCode());
		result = prime * result
				+ ((setType == null) ? 0 : setType.hashCode());
		result = prime * result
				+ ((firstTable == null) ? 0 : firstTable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		
		if (obj == null){
			return false;
		}
		
		if (getClass() != obj.getClass()){
			return false;
		}
		
		AnalysisHadoopUnionModel other = (AnalysisHadoopUnionModel) obj;
		
		if (outputColumns == null) {
			if (other.outputColumns != null){
				return false;
			}
		} else if (!ListUtility.equalsIgnoreOrder( outputColumns,other.outputColumns)){
			return false;
		}
		
		if (unionFiles == null) {
			if (other.unionFiles != null){
				return false;
			}
		} else if (!ListUtility.equalsIgnoreOrder(unionFiles,other.unionFiles)){
			return false;
		}
		
		if (setType == null) {
			if (other.setType != null){
				return false;
			}
		} else if (!StringUtil.safeEquals(setType, other.setType)){
			return false;
		}
		
		if (firstTable == null) {
			if (other.firstTable != null){
				return false;
			}
		} else if (!StringUtil.safeEquals(firstTable, other.firstTable)){
			return false;
		}
		
		return true;
	}




	public AnalysisHadoopUnionModel( 
			List<AnalysisHadoopUnionModelItem> outputColumns,List<AnalysisHadoopUnionFile> unionFiles,String setType,String firstTable) {
		super();
		this.outputColumns = outputColumns;
		this.unionFiles = unionFiles;
		this.setType = setType ;
		this.firstTable = firstTable ;
	}

	public AnalysisHadoopUnionModel(  ) {
		super();
		this.outputColumns = null;
		this.unionFiles = null;
	}


 


 
	public List<AnalysisHadoopUnionModelItem> getOutputColumns() {
		return outputColumns;
	}
	public void setOutputColumns(List<AnalysisHadoopUnionModelItem> outputColumns) {
		this.outputColumns = outputColumns;
	}
	

}
