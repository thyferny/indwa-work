/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.columnbins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */

 

public class ColumnBinsModel extends AbstractParameterObject{
	public static final String TAG_NAME="ColumnBinsModel";
 
	List<ColumnBin> columnBins=null;
	 
	 
	public ColumnBinsModel(List<ColumnBin> columnBins){ 
		this.columnBins=columnBins;
	}
	
	public ColumnBinsModel( ){
		this.columnBins=new ArrayList<ColumnBin>();
	}
	
	public List<ColumnBin> getColumnBins() {
		return columnBins;
	}

	public void setColumnBins(List<ColumnBin> columnBins) {
		this.columnBins = columnBins;
	}
	
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof ColumnBinsModel){
		 
				return  ListUtility.equalsIgnoreOrder(columnBins,
						((ColumnBinsModel)obj).getColumnBins()) 				  ;
		}else{
			return false;
		}
	
	}
  
	public void addColumnBin(ColumnBin columnBin) {
		if(columnBins==null){
			columnBins= new ArrayList<ColumnBin>();
		}
		columnBins.add(columnBin) ;
		
	}
	 

	/** not ready...
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getColumnBins()!=null){
			for (Iterator<ColumnBin> iterator = getColumnBins().iterator(); iterator.hasNext();) {
				ColumnBin item = iterator.next();
				if(item!=null){
					Element itemElement=item.toXMLElement(xmlDoc);
					element.appendChild(itemElement); 
				}			
			}
		}
		return element;
	}

	 
	/**
	 * @return
	 */
	@Override
	public ColumnBinsModel clone()  throws CloneNotSupportedException {
		ColumnBinsModel model= new ColumnBinsModel();
		if(this.getColumnBins()!=null){
			model.setColumnBins(ParameterUtility.cloneObjectList(columnBins));
		}
		 
	 return model;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public static ColumnBinsModel fromXMLElement(Element element) {
		List<ColumnBin> columnBinItems = new ArrayList<ColumnBin>();
 		NodeList columnBinItemList = element.getElementsByTagName(ColumnBin.TAG_NAME);
		for (int i = 0; i < columnBinItemList.getLength(); i++) {
			if (columnBinItemList.item(i) instanceof Element ) {
				ColumnBin columnBinItem=ColumnBin.fromXMLElement((Element)columnBinItemList.item(i));
				columnBinItems.add(columnBinItem);
			}
		}
		ColumnBinsModel model=new ColumnBinsModel(columnBinItems);
		return model;
	}
	  
 
	
}
