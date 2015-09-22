package com.alpine.logparser;

public interface IAlpineLogParser {
	public abstract String getRegexp();
	public abstract String[] getMatchingKeywords();
	public abstract String[] getMatchingTypes();
	public abstract void setMatchingKeywords(String[] mkw);
	public abstract void setMatchingTypes(String[] mt);
	
	public abstract String[] processTheLine(String line);
	
}