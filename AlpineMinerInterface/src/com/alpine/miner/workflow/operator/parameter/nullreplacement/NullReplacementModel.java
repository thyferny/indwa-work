package com.alpine.miner.workflow.operator.parameter.nullreplacement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;

public class NullReplacementModel extends AbstractParameterObject {

	public static final String TAG_NAME = "NullReplacementModel";

	private List<NullReplacementItem> nullReplacements = null;

    protected static final String ATTR_GROUP_BY = "groupBy";

    private String groupBy = null;

    protected static final String DEFAULT_GROUP = "";

	public List<NullReplacementItem> getNullReplacements() {
		return nullReplacements;
	}

	public void setNullReplacements(List<NullReplacementItem> nullReplacements) {
		this.nullReplacements = nullReplacements;
	}


    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

	public NullReplacementModel() {
		this.nullReplacements = new ArrayList<NullReplacementItem>();
        this.groupBy = DEFAULT_GROUP;
	}

	public NullReplacementModel(List<NullReplacementItem> nullReplacements) {
		this.nullReplacements = nullReplacements;
        this.groupBy = DEFAULT_GROUP;
	}

    public NullReplacementModel(List<NullReplacementItem> nullReplacements, String groupBy) {
        this.nullReplacements = nullReplacements;
        this.groupBy = groupBy;
    }

	public boolean equals(Object obj) {
		if (obj instanceof NullReplacementModel) {
			return ListUtility.equalsIgnoreOrder(nullReplacements,
				((NullReplacementModel) obj).getNullReplacements())
                    && groupBy.equals(((NullReplacementModel) obj).getGroupBy());

		} else {
			return false;
		}

	}
	public void addNullReplacement (NullReplacementItem layer) {
		if(nullReplacements==null){
			nullReplacements= new ArrayList<NullReplacementItem>();
		}
		nullReplacements.add(layer);
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
        element.setAttribute(ATTR_GROUP_BY, getGroupBy());
		if(getNullReplacements()!=null){
			for (Iterator<NullReplacementItem> iterator = getNullReplacements().iterator(); iterator.hasNext();) {
				NullReplacementItem item = (NullReplacementItem) iterator.next();
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
	public NullReplacementModel clone()  throws CloneNotSupportedException {
		NullReplacementModel model= new NullReplacementModel();
		List clone = ParameterUtility.cloneObjectList( this.getNullReplacements());
		model.setNullReplacements(clone);
        model.setGroupBy(this.getGroupBy());
		return model;
	}
	
	@Override
	public String getXMLTagName() {
		return null;
	}

	public static NullReplacementModel fromXMLElement(Element element) {
		List<NullReplacementItem> nullReplacements = new ArrayList<NullReplacementItem>();
 		NodeList hiddenItemList = element.getElementsByTagName(NullReplacementItem.TAG_NAME);
		for (int i = 0; i < hiddenItemList.getLength(); i++) {
			if (hiddenItemList.item(i) instanceof Element ) {
				NullReplacementItem aggFieldItem=NullReplacementItem.fromXMLElement((Element)hiddenItemList.item(i));
				nullReplacements.add(aggFieldItem);
			}
		}
		NullReplacementModel nullReplacementsModel=new NullReplacementModel();
		nullReplacementsModel.setNullReplacements(nullReplacements);
        String groupAttr = element.getAttribute(ATTR_GROUP_BY);
        nullReplacementsModel.setGroupBy(groupAttr);
		return nullReplacementsModel;
	}
	
}
