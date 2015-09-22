package com.alpine.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;

public class FlowUtility {
	public static List<String[]> createLinkList(
			List<UIOperatorConnectionModel> connLists) {
		List<String[]> result = new ArrayList<String[]>();
		for (Iterator iterator = connLists.iterator(); iterator.hasNext();) {
			UIOperatorConnectionModel conn = (UIOperatorConnectionModel) iterator
					.next();
			
			String[] link = new String[]{conn.getSource().getId(),conn.getTarget().getId()}; 
			result.add(link) ;
		}

		return result;
	}
}
