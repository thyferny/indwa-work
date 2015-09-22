/**
 * ClassName XPathRow.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-20
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jeff Dong
 *
 */
public class XPathRow {
	HashMap<Integer, String> attributeColumns = new HashMap<Integer, String>();
	// tag name -> list
	HashMap<String, List<XPathRow>> subRows = new HashMap<String, List<XPathRow>>();
	int maxDepth = 0;

	public int getMaxDepth() {
		return maxDepth;
	}

	public void fillResultData(List<String[]> result) {
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			String[] strings = (String[]) iterator.next();
			for (Integer colIndex : attributeColumns.keySet()) {
				strings[colIndex] = attributeColumns.get(colIndex);
			}

		}
		for (List<XPathRow> subRowList : subRows.values()) {
			if (subRowList.size() == 1
					&& subRowList.get(0).getSubRows().size() == 0) {// a
																	// real
																	// cild
																	// with
																	// no
																	// sub
				XPathRow subRow = subRowList.get(0);
				HashMap<Integer, String> attributes = subRow.getRowDatas();
				for (Iterator iterator = result.iterator(); iterator
						.hasNext();) {
					String[] strings = (String[]) iterator.next();
					for (Integer colIndex : attributes.keySet()) {
						strings[colIndex] = attributes.get(colIndex);

					}
				}

			} else {
				int currentIndex = 0;
				for (Iterator iterator = subRowList.iterator(); iterator
						.hasNext();) {
					XPathRow xPathRow = (XPathRow) iterator.next();
					List<String[]> subresult = result.subList(currentIndex,
							currentIndex + xPathRow.getMaxDepth());
					xPathRow.fillResultData(subresult);
					currentIndex = currentIndex + xPathRow.getMaxDepth();
				}
			}
		}

	}

	public void countMaXDepth() {
		if (maxDepth < attributeColumns.size()) {//has attribute
			maxDepth = 1;

		}
		// per column
		for (Iterator iterator = subRows.values().iterator(); iterator
				.hasNext();) {
			List<XPathRow> rows = (List<XPathRow>) iterator.next();
			int rowSum = 0;
			for (Iterator iterator2 = rows.iterator(); iterator2.hasNext();) {
				XPathRow xPathRow = (XPathRow) iterator2.next();
				xPathRow.countMaXDepth();
				rowSum = rowSum + xPathRow.getMaxDepth();
			}
			if (maxDepth < rowSum) {
				maxDepth = rowSum;

			}
		}
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public HashMap<Integer, String> getRowDatas() {
		return attributeColumns;
	}

	public void addChildXPathRow(XPathRow createXPathRow, String tagName) {
	//	System.out.println(this + "addChildXPathRow: tagName = " + tagName
		//		+ " createXPathRow = " + createXPathRow);

		if (subRows.containsKey(tagName) == false) {
			subRows.put(tagName, new ArrayList<XPathRow>());
		}
		subRows.get(tagName).add(createXPathRow);

	}

	public void putAttributeValue(int columnIndex, String oneValue) {
		attributeColumns.put(columnIndex, oneValue);
	//	System.out.println(this + "putAttributeValue: columnIndex = "
		//		+ columnIndex + " value = " + oneValue);

	}

	public HashMap<String, List<XPathRow>> getSubRows() {
		return subRows;
	}

	public boolean isTagExisted(String tag) {
		return subRows.keySet() != null && subRows.keySet().contains(tag);
	}
}
