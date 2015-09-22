/**
 * ClassName  AnalysisStorageParameterModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-5
 *
 * COPYRIGHT (C) 2010-2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.configure;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.SqlUtil;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 *
 */
public class AnalysisStorageParameterModel{
	public static final String COLUMN_SEPARATOR = SqlUtil.COMMA;
	
	boolean isAppendOnly =false;
	boolean isColumnarStorage =false;
	boolean isCompression = false;
	int compressionLevel = 1 ; // between 1 and 10
	boolean isDistributedRandomly = true;
	List<String> distributColumns = new ArrayList<String>();
	public AnalysisStorageParameterModel(){
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + compressionLevel;
		result = prime
				* result
				+ ((distributColumns == null) ? 0 : distributColumns.hashCode());
		result = prime * result + (isAppendOnly ? 1231 : 1237);
		result = prime * result + (isColumnarStorage ? 1231 : 1237);
		result = prime * result + (isCompression ? 1231 : 1237);
		result = prime * result + (isDistributedRandomly ? 1231 : 1237);
		return result;
	}





	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisStorageParameterModel other = (AnalysisStorageParameterModel) obj;
		if (compressionLevel != other.compressionLevel)
			return false;
		if (distributColumns == null) {
			if (other.distributColumns != null)
				return false;
		} else if (!ListUtility.equalsFocusOrder(distributColumns,other.distributColumns))
			return false;
		if (isAppendOnly != other.isAppendOnly)
			return false;
		if (isColumnarStorage != other.isColumnarStorage)
			return false;
		if (isCompression != other.isCompression)
			return false;
		if (isDistributedRandomly != other.isDistributedRandomly)
			return false;
		return true;
	}





	@Override
	public String toString() {
		return "StorageParameterModel [compressionLevel=" + compressionLevel
				+ ", distributColumns=" + distributColumns + ", isAppendOnly="
				+ isAppendOnly + ", isColumnarStorage=" + isColumnarStorage
				+ ", isCompression=" + isCompression
				+ ", isDistributedRandomly=" + isDistributedRandomly + "]";
	}





	public AnalysisStorageParameterModel(boolean isAppendOnly,
			boolean isColumnarStorage, boolean isCompression,
			int compressionLevel, boolean isDistributedRandomly,
			List<String> distributColumns) {
		this.isAppendOnly = isAppendOnly;
		this.isColumnarStorage = isColumnarStorage;
		this.isCompression = isCompression;
		this.compressionLevel = compressionLevel;
		this.isDistributedRandomly = isDistributedRandomly;
		this.distributColumns = distributColumns;
	}





	public boolean isAppendOnly() {
		return isAppendOnly;
	}





	public void setAppendOnly(boolean isAppendOnly) {
		this.isAppendOnly = isAppendOnly;
	}





	public boolean isColumnarStorage() {
		return isColumnarStorage;
	}





	public void setColumnarStorage(boolean isColumnarStorage) {
		this.isColumnarStorage = isColumnarStorage;
	}





	public boolean isCompression() {
		return isCompression;
	}





	public void setCompression(boolean isCompression) {
		this.isCompression = isCompression;
	}





	public int getCompressionLevel() {
		return compressionLevel;
	}





	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}





	public boolean isDistributedRandomly() {
		return isDistributedRandomly;
	}





	public void setDistributedRandomly(boolean isDistributedRandomly) {
		this.isDistributedRandomly = isDistributedRandomly;
	}





	public List<String> getDistributColumns() {
		return distributColumns;
	}





	public void setDistributColumns(List<String> distributColumns) {
		this.distributColumns = distributColumns;
	}
	





	@Override
	public AnalysisStorageParameterModel clone() throws CloneNotSupportedException {
		List<String> aDistributColumns =null;
		if(distributColumns!=null ){
			aDistributColumns = ListUtility.cloneStringList(distributColumns) ;
		}
		AnalysisStorageParameterModel clone = new AnalysisStorageParameterModel(
				isAppendOnly, isColumnarStorage,   isCompression, compressionLevel, isDistributedRandomly, aDistributColumns );
		
		return clone;
	 
	}
 

	public String getDistributColumnsAsString() { 
		if(distributColumns!=null){
			StringBuffer sb = new StringBuffer();
			for(int i = 0 ;i <distributColumns.size();i++){
				sb.append(distributColumns.get(i));
				if(i<distributColumns.size()-1){
					sb.append(COLUMN_SEPARATOR) ;
				}
			}
			return sb.toString();
		}
		return "";
	}
	public String getSqlDistributeString(){
		if(isDistributedRandomly){
			return SqlUtil.SPACE;
		}else{
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for(String distributColumn : distributColumns){
				if(first){
					first = false;
				}else{
					sb.append(SqlUtil.COMMA);
				}
				sb.append(StringHandler.doubleQ(StringHandler.escQ(distributColumn.trim())));
			}
			return sb.toString();
		}
	}
}

