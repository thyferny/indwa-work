/**
* 
* ClassName DoubleArrayWritable.java
*
* Version information: 1.00
*
* Date: Aug 9, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.utily.type;


import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
/**
 * @author Shawn,Peter
 *  
 */
public class DoubleArrayWritable extends ArrayWritable {
	int size;
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public DoubleArrayWritable() {
		super(DoubleWritable.class);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof DoubleArrayWritable) {
			DoubleWritable[] dd = (DoubleWritable[]) ((DoubleArrayWritable) o).toArray();
			DoubleWritable[] me = (DoubleWritable[]) this.toArray();
			int i = 0;
			if(dd.length != me.length) {
				return false;
			}
			
			for(DoubleWritable it : me) {
				if(!it.equals(dd[i])) {
					return false;
				}
				i++;
			}
			return true;
		}
		return false;
	}
	public String toString(){
		StringBuffer stringBuffer=new StringBuffer();
		DoubleWritable[] me = (DoubleWritable[]) this.toArray();
		
		for(int j=0;j<me.length - 1;j++){
			stringBuffer.append(me[j].toString());
			stringBuffer.append(",");
		}
		stringBuffer.append(me[me.length - 1].toString());
		return stringBuffer.toString();
	}
	
	public DoubleArrayWritable clone() {
		DoubleArrayWritable newArray = new DoubleArrayWritable();
		DoubleWritable[] me = (DoubleWritable[]) this.toArray();
		DoubleWritable[] arr = new DoubleWritable[me.length];
		
		for(int i = 0; i < me.length; i++){
			arr[i] = me[i];
		}
		
		newArray.set(arr);
		return newArray;
	}
}
