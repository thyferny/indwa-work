/**
 * ClassName AdaboostModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.adboost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AnalysisAdaboostPersistenceModel {
	public static final String TAG_NAME="AdaboostPersistenceModel";
	
	private List<AnalysisAdaboostPersistenceItem> adaboostUIItems;
	
	public AnalysisAdaboostPersistenceModel() {
		this.adaboostUIItems = new ArrayList<AnalysisAdaboostPersistenceItem>();
	}
	public AnalysisAdaboostPersistenceModel(List<AnalysisAdaboostPersistenceItem> adaboostUIItems) {
		this.adaboostUIItems = adaboostUIItems;
	}
	public List<AnalysisAdaboostPersistenceItem> getAdaboostUIItems() {
		return adaboostUIItems;
	}

	public void setAdaboostUIItems(List<AnalysisAdaboostPersistenceItem> adaboostUIItems) {
		this.adaboostUIItems = adaboostUIItems;
	}
	
	public Element toXMLElement(Document xmlDoc){
		Element element = xmlDoc.createElement(TAG_NAME);
		List<AnalysisAdaboostPersistenceItem>  adaboostUIItems=getAdaboostUIItems();
		Iterator<AnalysisAdaboostPersistenceItem>  iter=adaboostUIItems.iterator();
		while(iter.hasNext()){
			AnalysisAdaboostPersistenceItem item=iter.next();
			Element itemElement=item.toXMLElement(xmlDoc);
			element.appendChild(itemElement); 
		}
		return element;
	}
	public static AnalysisAdaboostPersistenceModel fromXMLElement(Element element) {
		List<AnalysisAdaboostPersistenceItem> adaboostUIItems = new ArrayList<AnalysisAdaboostPersistenceItem>();
 		NodeList adaboostUIItemList = element.getElementsByTagName(AnalysisAdaboostPersistenceItem.TAG_NAME);
		for (int i = 0; i < adaboostUIItemList.getLength(); i++) {
			if (adaboostUIItemList.item(i) instanceof Element ) {
				AnalysisAdaboostPersistenceItem interActionItem=AnalysisAdaboostPersistenceItem.fromXMLElement((Element)adaboostUIItemList.item(i));
				adaboostUIItems.add(interActionItem);
			}
		}
		return new AnalysisAdaboostPersistenceModel(adaboostUIItems);
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AnalysisAdaboostPersistenceModel)){
			return false;
		}
		AnalysisAdaboostPersistenceModel target=(AnalysisAdaboostPersistenceModel)obj;
		boolean res=true;
		if(target.getAdaboostUIItems().size()!=getAdaboostUIItems().size()){
			return false;
		}else{
			for(int i=0;i<getAdaboostUIItems().size();i++){
				AnalysisAdaboostPersistenceItem item=getAdaboostUIItems().get(i);
				AnalysisAdaboostPersistenceItem item_target=target.getAdaboostUIItems().get(i);
				if(item.equals(item_target)){
					continue;
				}else{
					return false;
				}
			}
		}
		return res;
	}
	
}
