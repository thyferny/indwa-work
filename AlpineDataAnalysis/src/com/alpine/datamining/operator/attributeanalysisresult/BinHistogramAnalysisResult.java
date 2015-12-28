package com.alpine.datamining.operator.attributeanalysisresult;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;




@XmlRootElement(name = "BinHistogramAnalysisResult")
@XmlType(propOrder = { "columnName", "bin", "begin", "end","count","percentage","accumCount","accumPercentage" })
public class BinHistogramAnalysisResult {
	private String columnName;
	private int bin;
	private float begin;
	private float end;
	private int count;
	private float percentage;
	private float accumCount;
	private float accumPercentage;

	
	public String getColumnName() {
		return columnName;
	}

	
	@XmlElement
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	
	public int getBin() {
		return bin;
	}

	
	@XmlElement
	public void setBin(int bin) {
		this.bin = bin;
	}

	
	public float getBegin() {
		return begin;
	}

	
	@XmlElement
	public void setBegin(float begin) {
		this.begin = begin;
	}

	
	public float getEnd() {
		return end;
	}

	
	@XmlElement
	public void setEnd(float end) {
		this.end = end;
	}

	
	public int getCount() {
		return count;
	}

	
	@XmlElement
	public void setCount(int count) {
		this.count = count;
	}

	
	public float getPercentage() {
		return percentage;
	}

	
	@XmlElement
	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	
	public float getAccumCount() {
		return accumCount;
	}

	
	@XmlElement
	public void setAccumCount(float accumCount) {
		this.accumCount = accumCount;
	}

	
	public float getAccumPercentage() {
		return accumPercentage;
	}

	
	@XmlElement
	public void setAccumPercentage(float accumPercentage) {
		this.accumPercentage = accumPercentage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(accumCount);
		result = prime * result + Float.floatToIntBits(accumPercentage);
		result = prime * result + Float.floatToIntBits(begin);
		result = prime * result + bin;
		result = prime * result + count;
		result = prime * result + Float.floatToIntBits(end);
		result = prime * result + Float.floatToIntBits(percentage);
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
		BinHistogramAnalysisResult other = (BinHistogramAnalysisResult) obj;
		if (Float.floatToIntBits(accumCount) != Float
				.floatToIntBits(other.accumCount))
			return false;
		if (Float.floatToIntBits(accumPercentage) != Float
				.floatToIntBits(other.accumPercentage))
			return false;
		if (Float.floatToIntBits(begin) != Float.floatToIntBits(other.begin))
			return false;
		if (bin != other.bin)
			return false;
		if (count != other.count)
			return false;
		if (Float.floatToIntBits(end) != Float.floatToIntBits(other.end))
			return false;
		if (Float.floatToIntBits(percentage) != Float
				.floatToIntBits(other.percentage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BinHistogramAnalysisResult [count=" + count + ", bin=" + bin
				+ ", begin=" + begin + ", end=" + end + ", percentage="
				+ percentage + ", accumCount=" + accumCount
				+ ", accumPercentage=" + accumPercentage + ", columnName="
				+ columnName + "]";
	}

	
}