/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * QuantileFieldsModelUI.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 31, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.workflow.operator.parameter.variable.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author sam_zang
 *
 */
public class QuantileFieldsModelUI {
	
	class QuantileItemUI {
		String columnName;
		String quantileTypeLabel;
		boolean isCreateNewColumn;
		int numberOfBin;
	}
	
	class QuantileItemBinNumericUI {
		String columnName;
		List<QuantileItemBinNumeric> binList;
	}
	
	class QuantileItemBinCategoryUI {
		String columnName;
		List<QuantileItemBinCategory> binList;
		List<String> availableValueList;
	}
	
	class QuantileItemBinDateTimeUI {
		String columnName;
		List<QuantileItemBinDateTime> binList;
	}
	
	public QuantileFieldsModelUI(QuantileFieldsModel obj) {
		if (obj == null) {
			return;
		}
		for (QuantileItem item : obj.getQuantileItems()) {
			QuantileItemUI ui = new QuantileItemUI();
			ui.columnName = item.getColumnName();
			ui.isCreateNewColumn = item.isCreateNewColumn();
			ui.numberOfBin = item.getNumberOfBin();
			int type = item.getQuantileType();
			if (type == QuantileItem.TYPE_CUSTOMIZE) {
				ui.quantileTypeLabel = QuantileItem.TYPE_CUSTIMZE_LABEL;
			}
			else {
				ui.quantileTypeLabel = QuantileItem.TYPE_AVG_ASC_LABEL;
			}
		
			this.quantileItems.add(ui);
			
			// there should be one and only one
			// that is not null.
			List cbins = item.getCategoryBins();		
			if (cbins != null && cbins.size() > 0) {
				QuantileItemBinCategoryUI bin = new QuantileItemBinCategoryUI();
				bin.columnName = item.getColumnName();
				bin.binList = cbins;
				
				if (categoryBins == null) {
					categoryBins = new LinkedList<QuantileItemBinCategoryUI>();
				}
				categoryBins.add(bin);
				continue;
			}
			
			List dbins = item.getDatetimeBins();
			if (dbins != null && dbins.size() > 0) {
				QuantileItemBinDateTimeUI bin = new QuantileItemBinDateTimeUI();
				bin.columnName = item.getColumnName();
				bin.binList = dbins;
				
				if (datetimeBins == null) {
					datetimeBins = new LinkedList<QuantileItemBinDateTimeUI>();
				}
				datetimeBins.add(bin);
				continue;
			}
			
			List nbins = item.getNumericBins();
			if (nbins != null && nbins.size() > 0) {
				QuantileItemBinNumericUI bin = new QuantileItemBinNumericUI();
				bin.columnName = item.getColumnName();
				bin.binList = nbins;
				
				if (numericBins == null) {
					numericBins = new LinkedList<QuantileItemBinNumericUI>();
				}
				numericBins.add(bin);
			}			
		}		
	}

	@SuppressWarnings("unused")
	QuantileFieldsModelUI() {}
	
	List<QuantileItemUI> quantileItems = new LinkedList<QuantileItemUI>();
	List<QuantileItemBinNumericUI> numericBins = null;
	List<QuantileItemBinCategoryUI> categoryBins = null;
	List<QuantileItemBinDateTimeUI> datetimeBins = null;
	
	/**
	 *
	 */
	public QuantileFieldsModel getValue() {

		QuantileFieldsModel model = new QuantileFieldsModel();
		for (QuantileItemUI ui : this.quantileItems) {
			QuantileItem item = new QuantileItem();

			item.setColumnName(ui.columnName);
			item.setIsCreateNewColumn(ui.isCreateNewColumn);
			if (ui.isCreateNewColumn) {
				item.setNewColumnName(ui.columnName + "_bin");
			}
			else {
				item.setNewColumnName("");
			}
			item.setNumberOfBin(ui.numberOfBin);
			String label = ui.quantileTypeLabel;
			if (label.equals(QuantileItem.TYPE_CUSTIMZE_LABEL)) {
				item.setQuantileType(QuantileItem.TYPE_CUSTOMIZE);
			}
			else {
				item.setQuantileType(QuantileItem.TYPE_AVG_ASC);
			}
			
			model.addQuantileItem(item);
			
			// there should be one and only one
			// that is not null.
			List bins = this.getCategoryBins(ui.columnName);
			if (bins != null && bins.size() > 0) {
				item.setCategoryBins(bins);
				continue;
			}
			bins = this.getDatetimeBins(ui.columnName);
			if (bins != null && bins.size() > 0) {
				item.setDatetimeBins(bins);
				continue;
			}
			bins = this.getNumericBins(ui.columnName);
			if (bins != null && bins.size() > 0) {
				item.setNumericBins(bins);
			}			
		}
		return model;
	}

	/**
	 * @param columnName
	 * @return
	 */
	private List getNumericBins(String columnName) {
		if (numericBins == null) {
			return null;
		}
		for (QuantileItemBinNumericUI uibin : numericBins) {
			if (uibin.columnName.equals(columnName)) {
				return uibin.binList;
			}
		}
		return null;
	}

	/**
	 * @param columnName
	 * @return
	 */
	private List getDatetimeBins(String columnName) {
		if (datetimeBins == null) {
			return null;
		}
		for (QuantileItemBinDateTimeUI uibin : datetimeBins) {
			if (uibin.columnName.equals(columnName)) {
				return uibin.binList;
			}
		}
		return null;
	}

	/**
	 * @param columnName
	 * @return
	 */
	private List getCategoryBins(String columnName) {
		if (categoryBins == null) {
			return null;
		}
		for (QuantileItemBinCategoryUI uibin : categoryBins) {
			if (uibin.columnName.equals(columnName)) {
				return uibin.binList;
			}
		}
		return null;
	}
}
