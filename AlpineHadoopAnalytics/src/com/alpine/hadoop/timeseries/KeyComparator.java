
package com.alpine.hadoop.timeseries;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class KeyComparator extends WritableComparator {
		protected KeyComparator() {
			super(LongSort.class, true);
		}
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			LongSort ip1 = (LongSort) w1;
			LongSort ip2 = (LongSort) w2;
			long l = ip1.getSortKey().get();
			long r = ip2.getSortKey().get();
			long gl=ip1.getGroup().get();
			long gr=ip2.getGroup().get();
			
			
			if(gl==gr)
			{
				return l == r ? 0 : (l < r ? 1 : -1);
			}
			else
			{
				 int cmp = (gl==gr ? 0 : (gl<gr?-1:1));
				return -cmp;//the sort of group   it works
			}
//			this.
		}
	}
