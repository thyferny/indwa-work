package com.alpine.miner.workflow.output.visual;

public class VisualLabel {
	int value;
	String text;
	public VisualLabel(int value, String text) {
		super();
		this.value = value;
		this.text = text;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

}
