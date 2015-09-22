/**
 * ClassName ListFrequencyStack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.util.LinkedList;

/**
 * A frequency stack based on a list implementation.
 * 
 * @author Eason
 */
public class ListFrequencyStack implements FrequencyStack {

	private LinkedList<Long> list;

	public ListFrequencyStack() {
		list = new LinkedList<Long>();
	}

	public long getFrequency(int height) {
		if (height >= list.size()) {
			return 0;
		} else if (height == list.size() - 1) {
			return list.getLast();
		} else {
			return list.get(height);
		}
	}

	public void increaseFrequency(int stackHeight, long value) {
		if (stackHeight == list.size() - 1) {
			// int newValue = value + list.pollLast(); // IM: pollLast only
			// available in JDK 6
//			int newValue = value + list.removeLast();
			list.addLast(value + list.removeLast());
		} else if (stackHeight == list.size()) {
			list.addLast(value);
		}
	}

	public void popFrequency(int height) {
		if (height == list.size() - 1) {
			// list.pollLast(); // IM: pollLast only available in JDK 6
			list.removeLast();
		} else if (height < list.size() - 1) {
			list.remove(height);
		}
	}
}
