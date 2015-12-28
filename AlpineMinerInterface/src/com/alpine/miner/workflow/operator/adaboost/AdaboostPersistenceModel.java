
package com.alpine.miner.workflow.operator.adaboost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AdaboostPersistenceModel {
	public static final String TAG_NAME="AdaboostPersistenceModel";
	
	private List<AdaboostPersistenceItem> adaboostUIItems;
	
	public AdaboostPersistenceModel() {
		this.adaboostUIItems = new ArrayList<AdaboostPersistenceItem>();
	}
	public AdaboostPersistenceModel(List<AdaboostPersistenceItem> adaboostUIItems) {
		this.adaboostUIItems = adaboostUIItems;
	}
	public List<AdaboostPersistenceItem> getAdaboostUIItems() {
		return adaboostUIItems;
	}

	public void setAdaboostUIItems(List<AdaboostPersistenceItem> adaboostUIItems) {
		this.adaboostUIItems = adaboostUIItems;
	}
	
	public Element toXMLElement(Document xmlDoc){
		Element element = xmlDoc.createElement(TAG_NAME);
		List<AdaboostPersistenceItem>  adaboostUIItems=getAdaboostUIItems();
		Iterator<AdaboostPersistenceItem>  iter=adaboostUIItems.iterator();
		while(iter.hasNext()){
			AdaboostPersistenceItem item=iter.next();
			Element itemElement=item.toXMLElement(xmlDoc);
			element.appendChild(itemElement); 
		}
		return element;
	}
	public static AdaboostPersistenceModel fromXMLElement(Element element) {
		List<AdaboostPersistenceItem> adaboostUIItems = new ArrayList<AdaboostPersistenceItem>();
 		NodeList adaboostUIItemList = element.getElementsByTagName(AdaboostPersistenceItem.TAG_NAME);
		for (int i = 0; i < adaboostUIItemList.getLength(); i++) {
			if (adaboostUIItemList.item(i) instanceof Element ) {
				AdaboostPersistenceItem interActionItem=AdaboostPersistenceItem.fromXMLElement((Element)adaboostUIItemList.item(i));
				adaboostUIItems.add(interActionItem);
			}
		}
		return new AdaboostPersistenceModel(adaboostUIItems);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null||!(obj instanceof AdaboostPersistenceModel)){
			return false;
		}
		AdaboostPersistenceModel target=(AdaboostPersistenceModel)obj;
		boolean res=true;
		if(target.getAdaboostUIItems().size()!=getAdaboostUIItems().size()){
			return false;
		}else{
			for(int i=0;i<getAdaboostUIItems().size();i++){
				AdaboostPersistenceItem item=getAdaboostUIItems().get(i);
				AdaboostPersistenceItem item_target=target.getAdaboostUIItems().get(i);
				if(item!=null&&item.equals(item_target)){
					continue;
				}else{
					return false;
				}
			}
		}
		return res;
	}
	
}
