

package com.alpine.datamining.operator.woe;




public class AnalysisWOENumericNode extends AnalysisWOENode {
	
	private static final long serialVersionUID = 4035347843991818928L;
	private double upper;
	private double bottom;

	public double getUpper() {
		return upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (bottom != -Double.MAX_VALUE) {
			result.append(bottom).append("<=  ");
		}
		result.append("X");
		if (upper != Double.MAX_VALUE) {
			result.append(" <").append(upper);
		}
		result.append(" : ");
		result.append(groupInfo);
		result.append("\n");
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj==null||!(obj instanceof AnalysisWOENumericNode)) {
			return false;
		} else {
			AnalysisWOENumericNode woeNumbernicNode = (AnalysisWOENumericNode) obj;

			if (upper == woeNumbernicNode.getUpper()
					&& bottom == woeNumbernicNode.getBottom()) {
				return true;
			}
			return false;
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		AnalysisWOENumericNode newNode=new AnalysisWOENumericNode();
		newNode.setWOEValue(WOEValue);
		newNode.setGroupInfror(groupInfo);
		newNode.setBottom(bottom);
		newNode.setUpper(upper);
		return newNode;
	}
}
