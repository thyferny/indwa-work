/**
 * ClassName VisualNodeLink.java
 *
 * Version information: 3.00
 *
 * Data: 2011-7-11
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.workflow.output.visual;

public class VisualNodeLink {
	String label;

	int startIndex;
	int endIndex;

	public VisualNodeLink(int startIndex, int endIndex, String label) { 
		this.startIndex=startIndex;
		this.endIndex=endIndex;
		this.label=label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VisualNodeLink [label=");
		builder.append(label);
		builder.append(", startIndex=");
		builder.append(startIndex);
		builder.append(", endIndex=");
		builder.append(endIndex);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endIndex;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + startIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VisualNodeLink other = (VisualNodeLink) obj;
		if (endIndex != other.endIndex)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (startIndex != other.startIndex)
			return false;
		return true;
	}
	
}
