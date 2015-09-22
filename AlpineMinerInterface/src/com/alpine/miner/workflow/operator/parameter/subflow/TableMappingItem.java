/**
 * ClassName TableMappingItem
 *
 * Version information: 1.00
 *
 * Data: 2012-4-8
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.subflow;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.utility.tools.StringHandler;

public class TableMappingItem {
	public static final String TAG_NAME = "TableMappingItem"; 
	
	public static final String Attr_Input_Schema = "inputSchema" ;
	public static final String Attr_Input_Table = "inputTable" ;
	public static final String Attr_SubFlow_Schema = "subFlowSchema" ;
	public static final String Attr_SubFlow_Table = "subFlowTable" ;
	
	//should be a same connection
	//demo.xxx
	String inputSchema;
	String inputTable;
	String subFlowSchema;
	String subFlowTable;
	
	
	@Override
	public TableMappingItem clone() throws CloneNotSupportedException {
		TableMappingItem clone = new TableMappingItem(inputSchema, inputTable, subFlowSchema, subFlowTable);
  
		return clone;
	 
	}
	
	public Element toXMLElement(Document xmlDoc,boolean addSuffixToOutput, String userName){
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		
		element.setAttribute(Attr_Input_Schema, getInputSchema());
		
		if(addSuffixToOutput==true){
			  
			element.setAttribute(Attr_Input_Table, StringHandler.addPrefix(getInputTable(), userName)); 
		}else{
		 
			element.setAttribute(Attr_Input_Table, getInputTable());
		}
		
		element.setAttribute(Attr_SubFlow_Schema, getSubFlowSchema()); 
		element.setAttribute(Attr_SubFlow_Table, getSubFlowTable());
 
		
		return element;
		
	}
	
	public static TableMappingItem fromXMLElement(Element element) {
	 	
		String inputSchema = element.getAttribute(Attr_Input_Schema);
		String inputTable = element.getAttribute(Attr_Input_Table);

		String subFlowSchema = element.getAttribute(Attr_SubFlow_Schema);
		String subFlowTable = element.getAttribute(Attr_SubFlow_Table);
 
		TableMappingItem columnMap = new TableMappingItem(inputSchema, inputTable, subFlowSchema, subFlowTable);
		
		return columnMap;

	}
 

	 @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((inputSchema == null) ? 0 : inputSchema.hashCode());
			result = prime * result
					+ ((inputTable == null) ? 0 : inputTable.hashCode());
			result = prime * result
					+ ((subFlowSchema == null) ? 0 : subFlowSchema.hashCode());
			result = prime * result
					+ ((subFlowTable == null) ? 0 : subFlowTable.hashCode());
			return result;
		}
		@Override
		public String toString() {
			return "TableMappingItem [inputSchema=" + inputSchema + ", inputTable="
					+ inputTable + ", subFlowSchema=" + subFlowSchema
					+ ", subFlowTable=" + subFlowTable + "]";
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableMappingItem other = (TableMappingItem) obj;
			if (inputSchema == null) {
				if (other.inputSchema != null)
					return false;
			} else if (!inputSchema.equals(other.inputSchema))
				return false;
			if (inputTable == null) {
				if (other.inputTable != null)
					return false;
			} else if (!inputTable.equals(other.inputTable))
				return false;
			if (subFlowSchema == null) {
				if (other.subFlowSchema != null)
					return false;
			} else if (!subFlowSchema.equals(other.subFlowSchema))
				return false;
			if (subFlowTable == null) {
				if (other.subFlowTable != null)
					return false;
			} else if (!subFlowTable.equals(other.subFlowTable))
				return false;
			return true;
		}
		public TableMappingItem(String inputSchema, String inputTable,
				String subFlowSchema, String subFlowTable) {
			super();
			this.inputSchema = inputSchema;
			this.inputTable = inputTable;
			this.subFlowSchema = subFlowSchema;
			this.subFlowTable = subFlowTable;
		}
		public String getInputSchema() {
			return inputSchema;
		}
		public void setInputSchema(String inputSchema) {
			this.inputSchema = inputSchema;
		}
		public String getInputTable() {
			return inputTable;
		}
		public void setInputTable(String inputTable) {
			this.inputTable = inputTable;
		}
		public String getSubFlowSchema() {
			return subFlowSchema;
		}
		public void setSubFlowSchema(String subFlowSchema) {
			this.subFlowSchema = subFlowSchema;
		}
		public String getSubFlowTable() {
			return subFlowTable;
		}
		public void setSubFlowTable(String subFlowTable) {
			this.subFlowTable = subFlowTable;
		}
}
