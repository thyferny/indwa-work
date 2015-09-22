/**
 * ClassName AbstractAnnalysiticSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.xml.XmlDocManager;



/**
 * @author John Zhao
 *
 */
public abstract class AbstractAnalyticSource implements AnalyticSource {


	
	private AnalyticConfiguration config;

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticSource#getAnalyticConfig()
	 */
	@Override
	public AnalyticConfiguration getAnalyticConfig() {
		return config;
	}
 
	public void setAnalyticConfiguration(AnalyticConfiguration config) {
		this.config = config;
	}


	protected List<TableColumnMetaInfo> inputFieldInfo(
			XmlDocManager opTypeXmlManager, Node inputNode) {
		ArrayList<Node> fieldNodeList = opTypeXmlManager.getNodeList(inputNode,
				"Fields");

		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		if (fieldNodeList.size() > 0) {
			ArrayList<Node> fields = opTypeXmlManager.getNodeList(fieldNodeList
					.get(0), "Field");
			for (Iterator<Node> iterator = fields.iterator(); iterator
					.hasNext();) {
				Node node = iterator.next();
				String name = ((Element) node).getAttribute(OP_NAME_ATTR);
				String type = ((Element) node).getAttribute(OP_TYPE_ATTR);
				TableColumnMetaInfo columnInfo = new TableColumnMetaInfo(name,
						type);
				columns.add(columnInfo);
			}
		}
		return columns;
	}
}
