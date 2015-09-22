/**
 * ClassName  TableSetModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-10
 *
 * COPYRIGHT (C) 2010,2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.storageparam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */
public class StorageParameterModel  extends AbstractParameterObject {
	public static final String COLUMN_SEPARATOR = ",";
 	 
	//we keep  this tag name to compaitable with old version's flow file
	public static final String DEFAULT_TAG_NAME = OperatorParameter.NAME_outputTable_StorageParams;//"StorageParameters"; 
	public static final String[] tags = new String[]{DEFAULT_TAG_NAME,
		OperatorParameter.NAME_UmatrixTable_StorageParams,
		OperatorParameter.NAME_VmatrixTable_StorageParams,
		OperatorParameter.NAME_singularValueTable_StorageParams,
		
		OperatorParameter.NAME_PCAQoutputTable_StorageParams,
		OperatorParameter.NAME_PCAQvalueOutputTable_StorageParams,
		
		OperatorParameter.NAME_PLDADocTopicOutputTable_StorageParams,
		OperatorParameter.NAME_PLDAModelOutputTable_StorageParams,
		OperatorParameter.NAME_topicOutTable_StorageParams,
		OperatorParameter.NAME_docTopicOutTable_StorageParams };
	
	public static final String TAG_NAME_DISTRIBUTE_COLUMNS = "DistributColumnList";
	
	public static final String ATTR_NAME_APPEND_ONLY = "appendOnly";
	public static final String ATTR_NAME_COLUMNAR_STORAGE = "columnarStorage";
	public static final String ATTR_NAME_COMPRESSION = "compression";
	public static final String ATTR_NAME_COMPRESSION_LEVEL = "compressionLevel";
	public static final String ATTR_NAME_DISTRIBUTE_RANDOMLY = "isDistributedRandomly";
	
	boolean isAppendOnly =false;
	boolean isColumnarStorage =false;
	boolean isCompression = false;
	int compressionLevel = 1 ; // between 1 and 10
	boolean isDistributedRandomly = true;
	List<String> distributColumns = new ArrayList<String>();
	public StorageParameterModel(){
		
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
		StorageParameterModel other = (StorageParameterModel) obj;
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





	public StorageParameterModel(boolean isAppendOnly,
			boolean isColumnarStorage, boolean isCompression,
			int compressionLevel, boolean isDistributedRandomly,
			List<String> distributColumns) {
		super();
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
	public StorageParameterModel clone() throws CloneNotSupportedException {
		List<String> aDistributColumns =null;
		if(distributColumns!=null ){
			aDistributColumns = ListUtility.cloneStringList(distributColumns) ;
		}
		StorageParameterModel clone = new StorageParameterModel(
				isAppendOnly, isColumnarStorage,   isCompression, compressionLevel, isDistributedRandomly, aDistributColumns );
		
		return clone;
	 
	}
 

	public Element toXMLElement(Document xmlDoc,String tagName){
		 
		Element element = xmlDoc.createElement(tagName);
 
		
		element.setAttribute(ATTR_NAME_APPEND_ONLY,String.valueOf(isAppendOnly()));
		element.setAttribute(ATTR_NAME_COLUMNAR_STORAGE ,String.valueOf(isColumnarStorage()));
		element.setAttribute(ATTR_NAME_COMPRESSION ,String.valueOf(isCompression()));
		element.setAttribute(ATTR_NAME_COMPRESSION_LEVEL ,String.valueOf(getCompressionLevel()));
		element.setAttribute(ATTR_NAME_DISTRIBUTE_RANDOMLY ,String.valueOf(isDistributedRandomly()));
		
		
		if(distributColumns !=null){
			for (Iterator<String> iterator = distributColumns.iterator(); iterator.hasNext();) {
				String column = iterator.next();
				if(column!=null){
					Element child = xmlDoc.createElement(TAG_NAME_DISTRIBUTE_COLUMNS);
					child.setTextContent(column);
					element.appendChild(child);
				 
				}
				
			}
		}
		
		return element;
		
	}
	
	public static StorageParameterModel fromXMLElement(Element element) {
		List<String> aDistributColumns = new ArrayList<String>();
 
		NodeList distributerColumns = element.getElementsByTagName(TAG_NAME_DISTRIBUTE_COLUMNS);
	 
		
		for (int i = 0; i < distributerColumns.getLength(); i++) { 
			if (distributerColumns.item(i) instanceof Element ) {
				aDistributColumns.add(((Element)distributerColumns.item(i)).getTextContent());

			}
		}
		
		StorageParameterModel clone = new StorageParameterModel(
				Boolean.parseBoolean(element.getAttribute(ATTR_NAME_APPEND_ONLY))	, 
				Boolean.parseBoolean(element.getAttribute(ATTR_NAME_COLUMNAR_STORAGE)),   
				Boolean.parseBoolean(element.getAttribute(ATTR_NAME_COMPRESSION)), 
				Integer.parseInt(element.getAttribute(ATTR_NAME_COMPRESSION_LEVEL)),
				Boolean.parseBoolean(element.getAttribute(ATTR_NAME_DISTRIBUTE_RANDOMLY)), aDistributColumns) ;
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

	public static String[] getPossibleTags() { 
		 
		return tags;
	}
	
	public static boolean isTableStorageParameter(String parameterName) {
		String[] tags = StorageParameterModel.getPossibleTags();
		if(tags!=null){
			for(int i = 0;i<tags.length;i++){
				if(tags[i].equals(parameterName)){
					return true;
				} 
			}
		}
		return false;
	}

	@Override
	public String getXMLTagName() {
		// no use for now...
		return null;
	}
	
	 

 

}