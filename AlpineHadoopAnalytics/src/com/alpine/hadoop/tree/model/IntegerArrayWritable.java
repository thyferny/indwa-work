/**
*
* ClassName IntegerArrayWritable.java
*
* Version information: 1.00
*
* Sep 6, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.model;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import com.alpine.hadoop.utily.type.DoubleArrayWritable;

/**
 * @author Jonathan
 *  
 */

public class IntegerArrayWritable extends ArrayWritable {
	public IntegerArrayWritable() {
		super(IntWritable.class);
	}
	
	public IntegerArrayWritable clone() {
		IntegerArrayWritable newArr = new IntegerArrayWritable();
		Writable[] curr = this.get();
		IntWritable[] newInts = new IntWritable[curr.length];
		
		int i =0;
		for(Writable el : curr) {
			newInts[i] = new IntWritable(((IntWritable) el).get());
			i++;
		}
		
		newArr.set(newInts);
		
		return newArr;
	}
	
	public IntegerArrayWritable sum(IntegerArrayWritable second) {
		IntegerArrayWritable newArr = new IntegerArrayWritable();
		
		IntWritable[] curr = (IntWritable[]) this.get();
		Writable[] agg = second.get();
		
		IntWritable[] newInts = new IntWritable[curr.length];
		
		int i =0;
		for(IntWritable el : curr) {
			newInts[i] = new IntWritable(el.get() + ((IntWritable)agg[i]).get());
			i++;
		}
		
		newArr.set(newInts);
		
		return newArr;
	}
	
	public static IntegerArrayWritable wrapArray(int[] input) {
		IntegerArrayWritable newArr = new IntegerArrayWritable();
		IntWritable[] newInts = new IntWritable[input.length];
		
		int i =0;
		for(int el : input) {
			newInts[i] = new IntWritable(el);
			i++;
		}
		
		newArr.set(newInts);
		
		return newArr;
	}
	
	public int size() {
		return get().length;
	}
	
	public String toString(){
		StringBuffer stringBuffer=new StringBuffer();
		IntWritable[] me = (IntWritable[]) this.toArray();
		
		
		for(int j=0;j<me.length - 1;j++){
			stringBuffer.append(me[j].toString());
			stringBuffer.append(",");
		}
		stringBuffer.append(me[me.length - 1].toString());
		
		return stringBuffer.toString();
	}
	
	public boolean equals(Object o) {
		if(o instanceof IntegerArrayWritable) {
			IntWritable[] dd = (IntWritable[]) ((IntegerArrayWritable) o).toArray();
			IntWritable[] me = (IntWritable[]) this.toArray();
			
			int i = 0;
			if(dd.length != me.length) {
				return false;
			}
			
			for(IntWritable it : me) {
				if(!it.equals(dd[i])) {
					return false;
				}
				i++;
			}
			return true;
		}
		return false;
	}
}

