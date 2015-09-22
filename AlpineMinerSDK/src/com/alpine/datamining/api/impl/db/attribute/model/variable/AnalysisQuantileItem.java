/**
 * ClassName  QuantileItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author John Zhao
 *
 */
public class AnalysisQuantileItem {
	public static final String TAG_NAME="QuantileItem";
	public static final int TYPE_CUSTOMIZE=0;
	//only for numeric...
	public static final int TYPE_AVG_ASC=1;
		
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
	
	List<AnalysisQuantileItemBin> bins=new ArrayList<AnalysisQuantileItemBin>();
	
	List<AnalysisQuantileItemBinNumeric> numericBins=new ArrayList<AnalysisQuantileItemBinNumeric>();
	public List<AnalysisQuantileItemBinNumeric> getNumericBins() {
		return numericBins;
	}
	public void setNumericBins(List<AnalysisQuantileItemBinNumeric> numericBins) {
		this.numericBins = numericBins;
	}
	public List<AnalysisQuantileItemBinCategory> getCategoryBins() {
		return categoryBins;
	}
	public void setCategoryBins(List<AnalysisQuantileItemBinCategory> categoryBins) {
		this.categoryBins = categoryBins;
	}
	public List<AnalysisQuantileItemBinDateTime> getDatetimeBins() {
		return datetimeBins;
	}
	public void setDatetimeBins(List<AnalysisQuantileItemBinDateTime> datetimeBins) {
		this.datetimeBins = datetimeBins;
	}

	List<AnalysisQuantileItemBinCategory> categoryBins=new ArrayList<AnalysisQuantileItemBinCategory>();
	List<AnalysisQuantileItemBinDateTime> datetimeBins=new ArrayList<AnalysisQuantileItemBinDateTime>();
	
	
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
	public List<AnalysisQuantileItemBin> getBins() {
		return bins;
	}
	public void setBins(List<AnalysisQuantileItemBin> bins) {
		this.bins = bins;
	}


	public boolean equals(Object obj) {
		
		if (this == obj){
			return true;
		}else if (obj instanceof AnalysisQuantileItem==false){
			return false;
		}else{
			AnalysisQuantileItem qItem= (AnalysisQuantileItem) obj;
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
	private boolean isBinEquals(AnalysisQuantileItem qItem) {
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
	public void AddBinItem(AnalysisQuantileItemBin binItem) {
		if(bins==null){
			bins=new ArrayList<AnalysisQuantileItemBin>();
		}
		bins.add(binItem) ;
		
		if(binItem instanceof AnalysisQuantileItemBinNumeric){
			if(numericBins==null){
				numericBins=new ArrayList<AnalysisQuantileItemBinNumeric>();
			}
			numericBins.add((AnalysisQuantileItemBinNumeric)binItem) ;
		}
		else if(binItem instanceof AnalysisQuantileItemBinCategory){
			if(categoryBins==null){
				categoryBins=new ArrayList<AnalysisQuantileItemBinCategory>();
			}
			categoryBins.add((AnalysisQuantileItemBinCategory)binItem) ;
		}
		else if(binItem instanceof AnalysisQuantileItemBinDateTime){
			if(datetimeBins==null){
				datetimeBins=new ArrayList<AnalysisQuantileItemBinDateTime>();
			}
			datetimeBins.add((AnalysisQuantileItemBinDateTime)binItem) ;
		}
			
	}

	private List<AnalysisQuantileItemBin> createAllBinsList() {
		List<AnalysisQuantileItemBin> bins= new ArrayList<AnalysisQuantileItemBin> ();
		bins.addAll(numericBins) ;
		bins.addAll(categoryBins) ;
		bins.addAll(datetimeBins) ;
		return bins;
	}

	public AnalysisQuantileItem clone() {
		AnalysisQuantileItem item = new AnalysisQuantileItem();
		
		item.setColumnName(getColumnName()) ; 
		item.setNewColumnName(getNewColumnName()) ; 
		item.setNumberOfBin(getNumberOfBin()) ; 
		item.setQuantileType(getQuantileType());
		item.setIsCreateNewColumn(isCreateNewColumn());
		
		if(bins!=null&&bins.size()>0){
			List<AnalysisQuantileItemBin> clone= new ArrayList<AnalysisQuantileItemBin>();
			for (Iterator<AnalysisQuantileItemBin> iterator = bins.iterator(); iterator.hasNext();) {
				AnalysisQuantileItemBin bin = (AnalysisQuantileItemBin) iterator.next();
				clone.add(bin.clone()) ;
			}
			item.setBins(clone) ;
		}
		
		if(numericBins!=null&&numericBins.size()>0){
			List<AnalysisQuantileItemBinNumeric> clone= new ArrayList<AnalysisQuantileItemBinNumeric>();
			for (Iterator<AnalysisQuantileItemBinNumeric> iterator = numericBins.iterator(); iterator.hasNext();) {
				AnalysisQuantileItemBinNumeric bin = (AnalysisQuantileItemBinNumeric) iterator.next();
				clone.add(bin.clone()) ;
			}
			item.setNumericBins(clone) ;
		}
		
		if(categoryBins!=null&&categoryBins.size()>0){
			List<AnalysisQuantileItemBinCategory> clone= new ArrayList<AnalysisQuantileItemBinCategory>();
			for (Iterator<AnalysisQuantileItemBinCategory> iterator = categoryBins.iterator(); iterator.hasNext();) {
				AnalysisQuantileItemBinCategory bin = (AnalysisQuantileItemBinCategory) iterator.next();
				clone.add(bin.clone()) ;
			}
			item.setCategoryBins(clone) ;
		}
		
		if(datetimeBins!=null&&datetimeBins.size()>0){
			List<AnalysisQuantileItemBinDateTime> clone= new ArrayList<AnalysisQuantileItemBinDateTime>();
			for (Iterator<AnalysisQuantileItemBinDateTime> iterator = datetimeBins.iterator(); iterator.hasNext();) {
				AnalysisQuantileItemBinDateTime bin = (AnalysisQuantileItemBinDateTime) iterator.next();
				clone.add(bin.clone()) ;
			}
			item.setDatetimeBins(clone) ; 
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
		List<AnalysisQuantileItemBin> bins = createAllBinsList();
		for (Iterator<AnalysisQuantileItemBin> iterator = bins.iterator(); iterator.hasNext();) {
			AnalysisQuantileItemBin bin = (AnalysisQuantileItemBin) iterator.next();
			out.append(bin.toString());
		}
		return out.toString();
	}
}
