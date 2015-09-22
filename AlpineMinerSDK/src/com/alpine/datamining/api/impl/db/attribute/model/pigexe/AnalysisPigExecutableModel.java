/**
 * ClassName AnalysisHadoopUnionModel.java
 *
 * Version information: 1.00
 *
 * Date: 2012-10-29
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.pigexe;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;


public class AnalysisPigExecutableModel {
	public static final String TAG_NAME = "PigExecutableModel" ;
	public static final String ATTR_NAME_SCRIPT = "pigScript" ;
	public static final String TAG_NAME_ITEM = "PigInputMapItem" ;
	public static final String ATTR_NAME_UUID = "inputUuid" ;
	public static final String ATTR_NAME_ALIAS = "pigAlias" ;
	
	protected String pigScript = null;
	protected List<PigInputMapItem> pigInputMapItems= null;
	
	public AnalysisPigExecutableModel(){
		
	}
	
	public AnalysisPigExecutableModel(String pigScript,
			List<PigInputMapItem> pigInputMapItems) {
		super();
		this.pigScript = pigScript;
		this.pigInputMapItems = pigInputMapItems;
	}

	public String getPigScript() {
		return pigScript;
	}

	public void setPigScript(String pigScript) {
		this.pigScript = pigScript;
	}

	public List<PigInputMapItem> getPigInputMapItems() {
		return pigInputMapItems;
	}

	public void setPigInputMapItems(List<PigInputMapItem> pigInputMapItems) {
		this.pigInputMapItems = pigInputMapItems;
	}

	@Override
	public String toString() {
		return "PigExecutableModel [pigScript=" + pigScript
				+ ", pigInputMapItems=" + pigInputMapItems + "]";
	}

	public AnalysisPigExecutableModel clone(){
		AnalysisPigExecutableModel model = new AnalysisPigExecutableModel();
		cloneCommonFields(model) ;
		return model;
		
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return StringUtil.safeEquals(pigScript,((AnalysisPigExecutableModel)obj).getPigScript())
				&&ListUtility.equalsIgnoreOrder(pigInputMapItems,
						((AnalysisPigExecutableModel)obj).getPigInputMapItems());
	}

	public void cloneCommonFields(AnalysisPigExecutableModel model) {
		model.setPigScript(getPigScript());
		
		List<PigInputMapItem> newPigInputMapItems = new ArrayList<PigInputMapItem>(); 
		List<PigInputMapItem> items = getPigInputMapItems();
		
		if(items!=null){
			for(PigInputMapItem item: items){
				newPigInputMapItems.add(item.clone()) ;
			}
		}
		
		model.setPigInputMapItems(newPigInputMapItems ) ;
		
	}
	
	 
		public void initFromXmlElement(Element element) {

			 

			String pigScript = element.getAttribute(ATTR_NAME_SCRIPT);
			setPigScript(pigScript);
			 
			List<PigInputMapItem> pigInputMapItems = new ArrayList<PigInputMapItem>();


			NodeList fileNodeList = element
					.getElementsByTagName(TAG_NAME_ITEM);

			for (int i = 0; i < fileNodeList.getLength(); i++) {
				if (fileNodeList.item(i) instanceof Element) {
					PigInputMapItem item = new PigInputMapItem(
							((Element)fileNodeList.item(i)).getAttribute(ATTR_NAME_UUID),
							((Element)fileNodeList.item(i)).getAttribute(ATTR_NAME_ALIAS)
							);
					pigInputMapItems.add(item);

				}
			}
			setPigInputMapItems(pigInputMapItems) ;
	  
		}
		
}
