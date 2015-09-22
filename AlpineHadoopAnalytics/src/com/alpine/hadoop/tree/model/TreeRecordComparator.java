/**
 * 

* ClassName TreeRecordComparator.java
*
* Version information: 1.00
*
* Date: Nov 28, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.tree.model;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

 

/**
 * @author Shawn
 *
 *  
 */

public class TreeRecordComparator extends WritableComparator {

	protected TreeRecordComparator() {
		super(SplitRecordWritable.class, true);
		// TODO Auto-generated constructor stub
	}

	
	public int compare(WritableComparable w1, WritableComparable w2) {
		SplitRecordWritable ip1 = (SplitRecordWritable) w1;
		SplitRecordWritable ip2 = (SplitRecordWritable) w2;
		int gl = ip1.getColumnIndex().get();
		int gr = ip2.getColumnIndex().get();
		if(gl==gr)
		{
			if(ip1.getIsNumeric().get()==true)
			{
				double l = Double.parseDouble(ip1.getTextValue().toString());// getDataValue().get();
				double r = Double.parseDouble(ip2.getTextValue().toString());
				return l==r?0:(l<r?1:-1);
			}
			else {
				double l = ip1.getTextValue().hashCode();// getDataValue().get();
				double r = ip2.getTextValue().hashCode();
				return l==r?0:(l<r?1:-1);
			} 
		}else
		{
			int cmp=(gl==gr?0:(gl<gr?-1:1));
			return -cmp;//the sort of group   it works
		}
		
		
	}
}
