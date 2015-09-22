/**
*
* ClassName TextToDoubleArrayWritable.java
*
* Version information: 1.00
*
* Aug 23, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.utily.conversion;

/**
 * @author Jonathan
 *  
 */

public class TextToDoubleArray {
	public static Double[] convert(String[] input, int start, int end) {
		Double[] out = new Double[end - start];
		
		int j = 0;
		for(int i = start; i < end; i++) {
			out[j] = new Double(input[i]);
			j++;
		}
		
		return out;
	}
}

