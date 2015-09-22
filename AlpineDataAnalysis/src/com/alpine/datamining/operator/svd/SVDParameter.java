package com.alpine.datamining.operator.svd;

import com.alpine.datamining.operator.Parameter;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;

public class SVDParameter implements Parameter {
	private String colName;
	private String rowName;
	private int numFeatures = 3;
	private double originalStep  = 10.0;
	private double speedupConst  = 1.1;
	private double fastSpeedupConst  = 10.0;
	private double slowdownConst  = .1;
	private int numIterations   = 1000;
	private int minNumIterations  = 1;
	private double minImprovement  = 1.0;
	private int improvementReached  = 1;
	private double initValue  = 0.1;
	private int earlyTeminate  = 1;
	private String Umatrix;
	private String Vmatrix;
	private String singularValue;
	private int Udrop = 0;
	private int Vdrop = 0;
	private int singularValueDrop = 0;
	private AnalysisStorageParameterModel UmatrixTableStorageParameters;
	private AnalysisStorageParameterModel VmatrixTableStorageParameters;
	private AnalysisStorageParameterModel singularValueTableStorageParameters;

	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getRowName() {
		return rowName;
	}
	public void setRowName(String rowName) {
		this.rowName = rowName;
	}
	public int getNumFeatures() {
		return numFeatures;
	}
	public void setNumFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}
	public double getOriginalStep() {
		return originalStep;
	}
	public void setOriginalStep(double originalStep) {
		this.originalStep = originalStep;
	}
	public double getSpeedupConst() {
		return speedupConst;
	}
	public void setSpeedupConst(double speedupConst) {
		this.speedupConst = speedupConst;
	}
	public double getFastSpeedupConst() {
		return fastSpeedupConst;
	}
	public void setFastSpeedupConst(double fastSpeedupConst) {
		this.fastSpeedupConst = fastSpeedupConst;
	}
	public double getSlowdownConst() {
		return slowdownConst;
	}
	public void setSlowdownConst(double slowdownConst) {
		this.slowdownConst = slowdownConst;
	}
	public int getNumIterations() {
		return numIterations;
	}
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}
	public int getMinNumIterations() {
		return minNumIterations;
	}
	public void setMinNumIterations(int minNumIterations) {
		this.minNumIterations = minNumIterations;
	}
	public double getMinImprovement() {
		return minImprovement;
	}
	public void setMinImprovement(double minImprovement) {
		this.minImprovement = minImprovement;
	}
	public double getInitValue() {
		return initValue;
	}
	public void setInitValue(double initValue) {
		this.initValue = initValue;
	}
	public String getUmatrix() {
		return Umatrix;
	}
	public void setUmatrix(String umatrix) {
		Umatrix = umatrix;
	}
	public String getVmatrix() {
		return Vmatrix;
	}
	public void setVmatrix(String vmatrix) {
		Vmatrix = vmatrix;
	}
	public int getImprovementReached() {
		return improvementReached;
	}
	public void setImprovementReached(int improvementReached) {
		this.improvementReached = improvementReached;
	}
	public int getEarlyTeminate() {
		return earlyTeminate;
	}
	public void setEarlyTeminate(int earlyTeminate) {
		this.earlyTeminate = earlyTeminate;
	}
	public int getUdrop() {
		return Udrop;
	}
	public void setUdrop(int udrop) {
		Udrop = udrop;
	}
	public int getVdrop() {
		return Vdrop;
	}
	public void setVdrop(int vdrop) {
		Vdrop = vdrop;
	}
	public int getSingularValueDrop() {
		return singularValueDrop;
	}
	public void setSingularValueDrop(int singularValueDrop) {
		this.singularValueDrop = singularValueDrop;
	}
	public String getSingularValue() {
		return singularValue;
	}
	public void setSingularValue(String singularValue) {
		this.singularValue = singularValue;
	}
	public AnalysisStorageParameterModel getUmatrixTableStorageParameters() {
		return UmatrixTableStorageParameters;
	}
	public void setUmatrixTableStorageParameters(
			AnalysisStorageParameterModel umatrixTableStorageParameters) {
		UmatrixTableStorageParameters = umatrixTableStorageParameters;
	}
	public AnalysisStorageParameterModel getVmatrixTableStorageParameters() {
		return VmatrixTableStorageParameters;
	}
	public void setVmatrixTableStorageParameters(
			AnalysisStorageParameterModel vmatrixTableStorageParameters) {
		VmatrixTableStorageParameters = vmatrixTableStorageParameters;
	}
	public AnalysisStorageParameterModel getSingularValueTableStorageParameters() {
		return singularValueTableStorageParameters;
	}
	public void setSingularValueTableStorageParameters(
			AnalysisStorageParameterModel singularValueTableStorageParameters) {
		this.singularValueTableStorageParameters = singularValueTableStorageParameters;
	}
}
