/**
 * ClassName PigExecutableModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-29
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.pigexe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.PigInputMapItem;
import com.alpine.miner.workflow.operator.parameter.ParameterObject;

public class PigExecutableModel extends AnalysisPigExecutableModel implements ParameterObject{

	
	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element ele = xmlDoc.createElement(TAG_NAME);
		ele.setAttribute(ATTR_NAME_SCRIPT, super.getPigScript());

		if (pigInputMapItems != null) {
			for (PigInputMapItem item: pigInputMapItems) {
				 
				if (item != null) {
					Element element =  xmlDoc.createElement(TAG_NAME_ITEM);
					
					element.setAttribute(ATTR_NAME_UUID, item.getInputUUID());
					element.setAttribute(ATTR_NAME_ALIAS, item.getPigAliasName());
					ele.appendChild(element);
				}

			}
		}

 
		return ele;
	}


	public static PigExecutableModel fromXMLElement(Element element) {
		PigExecutableModel model = new PigExecutableModel();
		model.initFromXmlElement(element) ;
		return model ;
	}

	@Override
	public String getXMLTagName() {
		 
		return TAG_NAME;
	}
	
	public PigExecutableModel clone(){
		PigExecutableModel model = new PigExecutableModel();
		super.cloneCommonFields(model) ;
		
		return model;
		
		
	}

}
