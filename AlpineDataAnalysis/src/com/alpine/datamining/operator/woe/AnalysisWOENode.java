


package com.alpine.datamining.operator.woe;

import java.io.Serializable;


public abstract class AnalysisWOENode  implements Serializable{
	
	private static final long serialVersionUID = 3851037871530393176L;
	protected String groupInfo;
	public double getWOEValue() {
		return WOEValue;
	}

	public void setWOEValue(double wOEValue) {
		WOEValue = wOEValue;
	}

	protected double WOEValue;
	public String getGroupInfror() {
		return groupInfo;
	}

	public void setGroupInfror(String groupInfo) {
		this.groupInfo = groupInfo;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
