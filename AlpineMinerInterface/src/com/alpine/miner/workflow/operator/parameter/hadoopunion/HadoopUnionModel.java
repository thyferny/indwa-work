/**
 * ClassName HadoopUnionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopunion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * 
 * @author john zhao
 *
 */
public class HadoopUnionModel extends AbstractParameterObject {
	public static final String TAG_NAME = "HadoopUnionDefinition";
	public static final String ATTR_NAME_SETTYPE = "setType";
	public static final String ATTR_NAME_FIRSTTABLE = "firstTable";
	
	public static final String UNION = "UNION";
	public static final String UNION_ALL="UNION_ALL";
	public static final String INTERSECT= "INTERSECT";
	public static final String EXCEPT ="EXCEPT";
	public static final String[] TABLE_SET_TYPE = new String[]{UNION,UNION_ALL,INTERSECT,EXCEPT};//
	
	List<HadoopUnionFile> unionFiles = null;
	String setType =UNION_ALL;

	//first is left (will use this column name and type)
	List<HadoopUnionModelItem> outputColumns = null; 
	
	
	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		out.append("setType =" +getSetType()+"\n");
		out.append("firstTable =" +getFirstTable()+"\n");
		out.append("columnMaps =" +outputColumns.toArray()+"\n");
	 
		return out.toString();
	}
	
	public Element toXMLElement(Document xmlDoc) {

		Element ele = xmlDoc.createElement(TAG_NAME);
		ele.setAttribute(ATTR_NAME_SETTYPE, getSetType());
		ele.setAttribute(ATTR_NAME_FIRSTTABLE, getFirstTable());

		if (outputColumns != null) {
			for (Iterator<HadoopUnionModelItem> iterator = outputColumns.iterator(); iterator
					.hasNext();) {
				HadoopUnionModelItem joinColumn = iterator.next();
				if (joinColumn != null) {
					Element joinTableElement = joinColumn.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}

		if (unionFiles != null) {
			for (Iterator<HadoopUnionFile> iterator = unionFiles.iterator(); iterator
					.hasNext();) {
				HadoopUnionFile unionFile = iterator.next();
				if (unionFile != null) {
					Element joinTableElement = unionFile.toXMLElement(xmlDoc);
					ele.appendChild(joinTableElement);
				}

			}
		}
  
		return ele;
	}
	
	public static HadoopUnionModel fromXMLElement(Element element) {

		List<HadoopUnionModelItem> unionColumns = new ArrayList<HadoopUnionModelItem>();

		String setType = element.getAttribute(ATTR_NAME_SETTYPE);		
		String firstTable = element.getAttribute(ATTR_NAME_FIRSTTABLE);
		NodeList joinTableList = element
				.getElementsByTagName(HadoopUnionModelItem.TAG_NAME);

		for (int i = 0; i < joinTableList.getLength(); i++) {
			if (joinTableList.item(i) instanceof Element) {
				HadoopUnionModelItem joinTable = HadoopUnionModelItem
						.fromXMLElement((Element) joinTableList.item(i));
				unionColumns.add(joinTable);

			}
		}
		List<HadoopUnionFile > unionFiles = new ArrayList<HadoopUnionFile>();


		NodeList fileNodeList = element
				.getElementsByTagName(HadoopUnionFile.TAG_NAME);

		for (int i = 0; i < fileNodeList.getLength(); i++) {
			if (fileNodeList.item(i) instanceof Element) {
				HadoopUnionFile unionFile = HadoopUnionFile
						.fromXMLElement((Element) fileNodeList.item(i));
				unionFiles.add(unionFile);

			}
		}
 
		HadoopUnionModel unionModel = new HadoopUnionModel(unionColumns,unionFiles,setType,firstTable);
		return unionModel;

	}
	
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	
	
 
	@Override
	public Object clone() throws CloneNotSupportedException {
	 
		List<HadoopUnionModelItem> newOutputColumns = new ArrayList<HadoopUnionModelItem>();
		for (HadoopUnionModelItem joinColumn : outputColumns) {
			newOutputColumns.add((HadoopUnionModelItem) joinColumn.clone());
		}
	 
		List<HadoopUnionFile> newUnionFiles = new ArrayList<HadoopUnionFile> (); 
		for(HadoopUnionFile unionFile:unionFiles){
			newUnionFiles.add((HadoopUnionFile)unionFile.clone());
		} 
		return new HadoopUnionModel( newOutputColumns,newUnionFiles,setType,firstTable);
	}



	public HadoopUnionModel( 
			List<HadoopUnionModelItem> outputColumns,List<HadoopUnionFile> unionFiles,String setType,String firstTable) {
		super();
		this.outputColumns = outputColumns ;
		this.unionFiles = unionFiles ;
		if(StringUtil.isEmpty(setType) == false){
			this.setType = setType ;
		}
		if(StringUtil.isEmpty(firstTable) == false){
			this.firstTable = firstTable ;
		}
	}

	public HadoopUnionModel(  ) {
		super();
		this.outputColumns = null;
		this.unionFiles = null;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HadoopUnionModel other = (HadoopUnionModel) obj;
		if (outputColumns == null) {
			if (other.outputColumns != null)
				return false;
		} else if (!ListUtility.equalsIgnoreOrder( outputColumns,other.outputColumns))
			return false;
		if (unionFiles == null) {
			if (other.unionFiles != null)
				return false;
		} else if (!ListUtility.equalsIgnoreOrder(unionFiles,other.unionFiles))
			return false;
		 HadoopUnionModel target=(HadoopUnionModel )obj;

		

		return ParameterUtility.nullableEquales(setType, target.getSetType())&&
		 		  ParameterUtility.nullableEquales(firstTable, target.getFirstTable());
	}




 
	public List<HadoopUnionModelItem> getOutputColumns() {
		return outputColumns;
	}
	public void setOutputColumns(List<HadoopUnionModelItem> outputColumns) {
		this.outputColumns = outputColumns;
	}
	

	
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
	
	public List<HadoopUnionFile> getUnionFiles() {
		return unionFiles;
	}

	public void setUnionFiles(List<HadoopUnionFile> unionFiles) {
		this.unionFiles = unionFiles;
	}
}
