package com.alpine.hadoop.cluster.mapper;
/**
 * 
 * ClassName KeyComparator.java
 *
 * Version information: 1.00
 *
 * Date: Nov 5, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
/**
* @author Shawn 
* 
*/
public class RandomKeyComparator extends WritableComparator {
		protected RandomKeyComparator() {
			super(Text.class, true);
		}
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			Text ip1 = (Text) w1;
			Text ip2 = (Text) w2;
			double r1=Math.random();
			double r2=Math.random();
			if(ip1.equals(ip2))
			{
				return r1 == r2 ? 0 : (r1 < r2 ? 1 : -1);
			}
			else
			{
				return ip1.compareTo(ip2);
			}
		}
	}
