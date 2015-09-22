/**
 * CustomDataset.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.dataset;

import java.util.List;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;


/**
 * @author Jimmy
 *
 */
public class BoxAndWhiskerDataset extends DefaultBoxAndWhiskerCategoryDataset {

	public void add(Number mean,
            Number median,
            Number q1,
            Number q3,
            Number minRegularValue,
            Number maxRegularValue,
            Number minOutlier,
            Number maxOutlier,
            List outliers, Comparable rowKey, Comparable columnKey) {
	    BoxAndWhiskerItem item = new BoxAndWhiskerItem(mean,median,q1,q3,minRegularValue,maxRegularValue,minOutlier,maxOutlier,outliers);
	    add(item, rowKey, columnKey);
	}
}
