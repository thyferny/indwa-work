/**
 * ClassName  QuantileItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.XMLFragment;
import com.alpine.utility.common.ListUtility;

/**
 * @author John Zhao
 *
 */
public class QuantileItem implements XMLFragment{
	public static final String TAG_NAME="QuantileItem";
	public static final int TYPE_CUSTOMIZE=0;
	//only for numeric...
	public static final int TYPE_AVG_ASC=1;
	
	public static final String TYPE_CUSTIMZE_LABEL= "Customize";
	public static final String TYPE_AVG_ASC_LABEL= "Average Ascend";//SDKLanguagePack.getMessage(LanguagePack.TYPE_AVG_ASC_LABEL;//
	
	public static final String ATTR_COLUMNNAME="columnName";	
	public static final String ATTR_NEW_COLUMNNAME="newColumnName";
	public static final String ATTR_NUMBER_OFBIN="numberOfBin";
	public static final String ATTR_QUANTILR_TYPE="quantileType";
	public static final String ATTR_CREATE_NEWCOLUMN="isCreateNewColumn";
	
	//with default value...
	private String columnName="";
	private String newColumnName="";
	private int numberOfBin=0;
	private int quantileType=TYPE_CUSTOMIZE;
	
	List<QuantileItemBin> bins=new ArrayList<QuantileItemBin>();
	
	List<QuantileItemBinNumeric> numericBins=new ArrayList<QuantileItemBinNumeric>();
	public List<QuantileItemBinNumeric> getNumericBins() {
		return numericBins;
	}
	public void setNumericBins(List<QuantileItemBinNumeric> numericBins) {
		this.numericBins = numericBins;
	}
	public List<QuantileItemBinCategory> getCategoryBins() {
		return categoryBins;
	}
	public void setCategoryBins(List<QuantileItemBinCategory> categoryBins) {
		this.categoryBins = categoryBins;
	}
	public List<QuantileItemBinDateTime> getDatetimeBins() {
		return datetimeBins;
	}
	public void setDatetimeBins(List<QuantileItemBinDateTime> datetimeBins) {
		this.datetimeBins = datetimeBins;
	}

	List<QuantileItemBinCategory> categoryBins=new ArrayList<QuantileItemBinCategory>();
	List<QuantileItemBinDateTime> datetimeBins=new ArrayList<QuantileItemBinDateTime>();
	
	
	boolean isCreateNewColumn=false;
	
	public int getQuantileType() {
		return quantileType;
	}
	public void setQuantileType(int quantileType) {
		this.quantileType = quantileType;
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
	public void setNewColumnName(String newColumnName) {
		this.newColumnName = newColumnName;
	}
	public int getNumberOfBin() {
		return numberOfBin;
	}
	public void setNumberOfBin(int numberOfBin) {
		this.numberOfBin = numberOfBin;
	}
	public List<QuantileItemBin> getBins() {
//		List<QuantileItemBin> bins=new ArrayList<QuantileItemBin>();
//		
//		List<QuantileItemBinCategory> categoryBins=getCategoryBins();
//		for(QuantileItemBinCategory categoryBin:categoryBins){
//			bins.add(categoryBin);
//		}
//		
//		List<QuantileItemBinDateTime> dataTimeBins=getDatetimeBins();
//		for(QuantileItemBinDateTime dataTimeBin:dataTimeBins){
//			bins.add(dataTimeBin);
//		}
//		
//		List<QuantileItemBinNumeric> numericBins=getNumericBins();
//		for(QuantileItemBinNumeric numericBin:numericBins){
//			bins.add(numericBin);
//		}
		return bins;
	}
	public void setBins(List<QuantileItemBin> bins) {
		this.bins = bins;
	}


	public boolean equals(Object obj) {
		
		if (this == obj){
			return true;
		}else if (obj instanceof QuantileItem==false){
			return false;
		}else{
			QuantileItem qItem= (QuantileItem) obj;
			return columnName.equals(qItem.getColumnName())
				&&newColumnName.equals(qItem.getNewColumnName())
				&&numberOfBin==qItem.getNumberOfBin()
				&&quantileType==qItem.getQuantileType()
				&&isCreateNewColumn==qItem.isCreateNewColumn()
				&&isBinEquals(qItem);
		}
		 
		
	}
	/**
	 * @return
	 */
	private boolean isBinEquals(QuantileItem qItem) {
		if(getQuantileType()==TYPE_AVG_ASC){
			return true;
		}else{
			return 	ListUtility.equalsIgnoreOrder(numericBins, qItem.getNumericBins())
						&&ListUtility.equalsIgnoreOrder(categoryBins, qItem.getCategoryBins())
						&&ListUtility.equalsIgnoreOrder(datetimeBins, qItem.getDatetimeBins());
		}
	}
	/**
	 * @param binItem
	 */
	public void AddBinItem(QuantileItemBin binItem) {
		if(bins==null){
			bins=new ArrayList<QuantileItemBin>();
		}
		bins.add(binItem) ;
		
		if(binItem instanceof QuantileItemBinNumeric){
			if(numericBins==null){
				numericBins=new ArrayList<QuantileItemBinNumeric>();
			}
			numericBins.add((QuantileItemBinNumeric)binItem) ;
		}
		else if(binItem instanceof QuantileItemBinCategory){
			if(categoryBins==null){
				categoryBins=new ArrayList<QuantileItemBinCategory>();
			}
			categoryBins.add((QuantileItemBinCategory)binItem) ;
		}
		else if(binItem instanceof QuantileItemBinDateTime){
			if(datetimeBins==null){
				datetimeBins=new ArrayList<QuantileItemBinDateTime>();
			}
			datetimeBins.add((QuantileItemBinDateTime)binItem) ;
		}
			
	}
	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getColumnName());
		element.setAttribute(ATTR_NEW_COLUMNNAME, getNewColumnName());
		element.setAttribute(ATTR_NUMBER_OFBIN , String.valueOf(getNumberOfBin()));
		element.setAttribute(ATTR_QUANTILR_TYPE, String.valueOf(getQuantileType()));
		element.setAttribute(ATTR_CREATE_NEWCOLUMN, String.valueOf(isCreateNewColumn()));
	
		List<QuantileItemBin> bins = createAllBinsList();
		for (Iterator<QuantileItemBin> iterator =  bins.iterator(); iterator.hasNext();) {
			QuantileItemBin bin = (QuantileItemBin) iterator.next();
			element.appendChild(bin.toXMLElement(xmlDoc)) ;
		}
		
		return element;
 
	}
	private List<QuantileItemBin> createAllBinsList() {
		List<QuantileItemBin> bins= new ArrayList<QuantileItemBin> ();
		bins.addAll(numericBins) ;
		bins.addAll(categoryBins) ;
		bins.addAll(datetimeBins) ;
		return bins;
	}
	/**
	 * @param item
	 * @return
	 */
	public static QuantileItem fromXMLElement(Element element) {
		QuantileItem item= new QuantileItem();
		item.setNewColumnName(element.getAttribute(ATTR_NEW_COLUMNNAME));
		
		item.setColumnName(element.getAttribute(ATTR_COLUMNNAME));
		
		item.setNumberOfBin(Integer.parseInt(element.getAttribute(ATTR_NUMBER_OFBIN )));
		item.setQuantileType(Integer.parseInt(element.getAttribute(ATTR_QUANTILR_TYPE)));
		item.setIsCreateNewColumn(Boolean.parseBoolean(element.getAttribute(ATTR_CREATE_NEWCOLUMN))) ;
		
		NodeList binItems=element.getElementsByTagName(QuantileItemBinCategory.TAG_NAME) ;
		if(binItems==null||binItems.getLength()==0){
			 binItems=element.getElementsByTagName(QuantileItemBinNumeric.TAG_NAME) ;
		}
		if(binItems==null||binItems.getLength()==0){
			 binItems=element.getElementsByTagName(QuantileItemBinDateTime.TAG_NAME) ;
		}
		if(binItems!=null&&binItems.getLength()!=0){
			for (int i = 0; i < binItems.getLength(); i++) {
				Element ele=(Element)binItems.item(i);
				QuantileItemBin binItem  = AbstractQuantileItemBin.fromXMLElement(ele); 
				item.AddBinItem(binItem);
			}
	  
		}
 
		return item;
	}

	public QuantileItem clone() {
		QuantileItem item = new QuantileItem();
		
		item.setColumnName(getColumnName()) ; 
		item.setNewColumnName(getNewColumnName()) ; 
		item.setNumberOfBin(getNumberOfBin()) ; 
		item.setQuantileType(getQuantileType());
		item.setIsCreateNewColumn(isCreateNewColumn());
		
		if(numericBins!=null&&numericBins.size()>0){
			numericBins.clear();
		}
		
		if(categoryBins!=null&&categoryBins.size()>0){
			categoryBins.clear();
		}
		
		if(datetimeBins!=null&&datetimeBins.size()>0){
			datetimeBins.clear();
		}
		
		if(bins!=null&&bins.size()>0){
			List<QuantileItemBin> clone= new ArrayList<QuantileItemBin>();
			for (Iterator<QuantileItemBin> iterator = bins.iterator(); iterator.hasNext();) {
				QuantileItemBin bin = (QuantileItemBin) iterator.next();
				QuantileItemBin itemBin = bin.clone();
				clone.add(itemBin);
				if(bin instanceof QuantileItemBinNumeric){
					numericBins.add((QuantileItemBinNumeric)itemBin);
				}else if(bin instanceof QuantileItemBinCategory){
					categoryBins.add((QuantileItemBinCategory)itemBin);
				}else if(bin instanceof QuantileItemBinDateTime){
					datetimeBins.add((QuantileItemBinDateTime)itemBin);
				}			
			}
			item.setBins(clone);
			item.setNumericBins(numericBins);
			item.setCategoryBins(categoryBins);
			item.setDatetimeBins(datetimeBins);
		}		
		return item;
	}
	/**
	 * @param selection
	 */
	public void setIsCreateNewColumn(boolean isCreateNewColumn) {
		this.isCreateNewColumn=isCreateNewColumn;
		
	}
	public boolean isCreateNewColumn() {
		return isCreateNewColumn;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(getColumnName()).append(getNewColumnName()).append(getNumberOfBin());
		out.append(getQuantileType()).append(isCreateNewColumn());
		List<QuantileItemBin> bins = createAllBinsList();
		for (Iterator<QuantileItemBin> iterator = bins.iterator(); iterator.hasNext();) {
			QuantileItemBin bin = (QuantileItemBin) iterator.next();
			out.append(bin.toString());
		}
		return out.toString();
	}
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	@Override
	public void initFromXmlElement(Element element) {
		
	}
	
 
}
