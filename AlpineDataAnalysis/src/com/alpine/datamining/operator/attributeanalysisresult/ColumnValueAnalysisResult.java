/**
 * ClassName ColumnValueAnalysisResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author Eason
 * 
 */
@XmlRootElement(name = "ColumnValueAnalysisResult")
public class ColumnValueAnalysisResult {
	private String columnName = "";
	private String columnType = "";
	private long count = 0;
	private long uniqueValueCount = 0;
	private long nullCount = 0;
	private long emptyCount = 0;
	private long zeroCount = 0;
	private long positiveValueCount = 0;
	private long negativeValueCount = 0;
	private double avg = 0;
	private double min = 0;
	private double max = 0;
	private double deviation = 0;
	private double q1 = 0;
	private double median = 0;
	private double q3 = 0;

	private Object top01Value = 0;
	private long top01Count = 0;
	private Object top02Value = 0;
	private long top02Count = 0;
	private Object top03Value = 0;
	private long top03Count = 0;
	private Object top04Value = 0;
	private long top04Count = 0;
	private Object top05Value = 0;
	private long top05Count = 0;
	private Object top06Value = 0;
	private long top06Count = 0;
	private Object top07Value = 0;
	private long top07Count = 0;
	private Object top08Value = 0;
	private long top08Count = 0;
	private Object top09Value = 0;
	private long top09Count = 0;
	private Object top10Value = 0;
	private long top10Count = 0;

	private boolean countNA = false;
	private boolean uniqueValueCountNA = false;
	private boolean nullCountNA = false;
	private boolean emptyCountNA = false;
	private boolean zeroCountNA = false;
	private boolean positiveValueCountNA = false;
	private boolean negativeValueCountNA = false;
	private boolean avgNA = false;
	private boolean minNA = false;
	private boolean maxNA = false;
	private boolean deviationNA = false;
	private boolean q1NA = false;
	private boolean medianNA = false;
	private boolean q3NA = false;

	private boolean top01_valNA		=	false;
	private boolean top01_countNA	=	false;
	private boolean top02_valNA		=	false;
	private boolean top02_countNA	=	false;
	private boolean top03_valNA		=	false;
	private boolean top03_countNA	=	false;
	private boolean top04_valNA		=	false;
	private boolean top04_countNA	=	false;
	private boolean top05_valNA		=	false;
	private boolean top05_countNA	=	false;
	private boolean top06_valNA		=	false;
	private boolean top06_countNA	=	false;
	private boolean top07_valNA		=	false;
	private boolean top07_countNA	=	false;
	private boolean top08_valNA		=	false;
	private boolean top08_countNA	=	false;
	private boolean top09_valNA		=	false;
	private boolean top09_countNA	=	false;
	private boolean top10_valNA		=	false;
	private boolean top10_countNA	=	false;

	public String getColumnName() {
		return columnName;
	}

	public long getNullCount() {
		return nullCount;
	}

	public long getEmptyCount() {
		return emptyCount;
	}

	public String getColumnType() {
		return columnType;
	}

	public long getCount() {
		return count;
	}

	public long getUniqueValueCount() {
		return uniqueValueCount;
	}

	public long getZeroCount() {
		return zeroCount;
	}

	public long getPositiveValueCount() {
		return positiveValueCount;
	}

	public long getNegativeValueCount() {
		return negativeValueCount;
	}
	@XmlElement
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	@XmlElement
	public void setNullCount(long nullCount) {
		this.nullCount = nullCount;
	}
	@XmlElement
	public void setEmptyCount(long emptyCount) {
		this.emptyCount = emptyCount;
	}
	@XmlElement
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	@XmlElement
	public void setCount(long count) {
		this.count = count;
	}
	@XmlElement
	public void setUniqueValueCount(long uniqueValueCount) {
		this.uniqueValueCount = uniqueValueCount;
	}
	@XmlElement
	public void setZeroCount(long zeroCount) {
		this.zeroCount = zeroCount;
	}
	@XmlElement
	public void setPositiveValueCount(long positiveValueCount) {
		this.positiveValueCount = positiveValueCount;
	}
	@XmlElement
	public void setNegativeValueCount(long negativeValueCount) {
		this.negativeValueCount = negativeValueCount;
	}

	/**
	 * @return the avg
	 */
	public double getAvg() {
		return avg;
	}

	/**
	 * @param avg the avg to set
	 */
	@XmlElement
	public void setAvg(double avg) {
		this.avg = avg;
	}

	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	@XmlElement
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	@XmlElement
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * @return the deviation
	 */
	public double getDeviation() {
		return deviation;
	}

	/**
	 * @param deviation the deviation to set
	 */
	@XmlElement
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}
	public boolean isCountNA() {
		return countNA;
	}
	@XmlElement
	public void setCountNA(boolean countNA) {
		this.countNA = countNA;
	}
	
	public boolean isUniqueValueCountNA() {
		return uniqueValueCountNA;
	}
	@XmlElement
	public void setUniqueValueCountNA(boolean uniqueValueCountNA) {
		this.uniqueValueCountNA = uniqueValueCountNA;
	}
	
	public boolean isNullCountNA() {
		return nullCountNA;
	}
	@XmlElement
	public void setNullCountNA(boolean nullCountNA) {
		this.nullCountNA = nullCountNA;
	}

	public boolean isEmptyCountNA() {
		return emptyCountNA;
	}
	@XmlElement
	public void setEmptyCountNA(boolean emptyCountNA) {
		this.emptyCountNA = emptyCountNA;
	}

	public boolean isZeroCountNA() {
		return zeroCountNA;
	}
	@XmlElement
	public void setZeroCountNA(boolean zeroCountNA) {
		this.zeroCountNA = zeroCountNA;
	}

	public boolean isPositiveValueCountNA() {
		return positiveValueCountNA;
	}
	@XmlElement
	public void setPositiveValueCountNA(boolean positiveValueCountNA) {
		this.positiveValueCountNA = positiveValueCountNA;
	}

	public boolean isNegativeValueCountNA() {
		return negativeValueCountNA;
	}
	@XmlElement
	public void setNegativeValueCountNA(boolean negativeValueCountNA) {
		this.negativeValueCountNA = negativeValueCountNA;
	}

	public boolean isAvgNA() {
		return avgNA;
	}
	@XmlElement
	public void setAvgNA(boolean avgNA) {
		this.avgNA = avgNA;
	}

	public boolean isMinNA() {
		return minNA;
	}
	@XmlElement
	public void setMinNA(boolean minNA) {
		this.minNA = minNA;
	}

	public boolean isMaxNA() {
		return maxNA;
	}
	@XmlElement
	public void setMaxNA(boolean maxNA) {
		this.maxNA = maxNA;
	}

	public boolean isDeviationNA() {
		return deviationNA;
	}
	@XmlElement
	public void setDeviationNA(boolean deviationNA) {
		this.deviationNA = deviationNA;
	}

	public double getQ1() {
		return q1;
	}

	public void setQ1(double q1) {
		this.q1 = q1;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public double getQ3() {
		return q3;
	}

	public void setQ3(double q3) {
		this.q3 = q3;
	}

	public boolean isQ1NA() {
		return q1NA;
	}

	@XmlElement
	public void setQ1NA(boolean q1NA) {
		this.q1NA = q1NA;
	}

	public boolean isMedianNA() {
		return medianNA;
	}

	@XmlElement
	public void setMedianNA(boolean medianNA) {
		this.medianNA = medianNA;
	}

	public boolean isQ3NA() {
		return q3NA;
	}

	@XmlElement
	public void setQ3NA(boolean q3NA) {
		this.q3NA = q3NA;
	}

	public Object getTop01Value() {
		return top01Value;
	}

	public void setTop01Value(Object top01Value) {
		this.top01Value = top01Value;
	}

	public long getTop01Count() {
		return top01Count;
	}

	public void setTop01Count(long top01Count) {
		this.top01Count = top01Count;
	}

	public Object getTop02Value() {
		return top02Value;
	}

	public void setTop02Value(Object top02Value) {
		this.top02Value = top02Value;
	}

	public long getTop02Count() {
		return top02Count;
	}

	public void setTop02Count(long top02Count) {
		this.top02Count = top02Count;
	}

	public Object getTop03Value() {
		return top03Value;
	}

	public void setTop03Value(Object top03Value) {
		this.top03Value = top03Value;
	}

	public long getTop03Count() {
		return top03Count;
	}

	public void setTop03Count(long top03Count) {
		this.top03Count = top03Count;
	}

	public Object getTop04Value() {
		return top04Value;
	}

	public void setTop04Value(Object top04Value) {
		this.top04Value = top04Value;
	}

	public long getTop04Count() {
		return top04Count;
	}

	public void setTop04Count(long top04Count) {
		this.top04Count = top04Count;
	}

	public Object getTop05Value() {
		return top05Value;
	}

	public void setTop05Value(Object top05Value) {
		this.top05Value = top05Value;
	}

	public long getTop05Count() {
		return top05Count;
	}

	public void setTop05Count(long top05Count) {
		this.top05Count = top05Count;
	}

	public Object getTop06Value() {
		return top06Value;
	}

	public void setTop06Value(Object top06Value) {
		this.top06Value = top06Value;
	}

	public long getTop06Count() {
		return top06Count;
	}

	public void setTop06Count(long top06Count) {
		this.top06Count = top06Count;
	}

	public Object getTop07Value() {
		return top07Value;
	}

	public void setTop07Value(Object top07Value) {
		this.top07Value = top07Value;
	}

	public long getTop07Count() {
		return top07Count;
	}

	public void setTop07Count(long top07Count) {
		this.top07Count = top07Count;
	}

	public Object getTop08Value() {
		return top08Value;
	}

	public void setTop08Value(Object top08Value) {
		this.top08Value = top08Value;
	}

	public long getTop08Count() {
		return top08Count;
	}

	public void setTop08Count(long top08Count) {
		this.top08Count = top08Count;
	}

	public Object getTop09Value() {
		return top09Value;
	}

	public void setTop09Value(Object top09Value) {
		this.top09Value = top09Value;
	}

	public long getTop09Count() {
		return top09Count;
	}

	public void setTop09Count(long top09Count) {
		this.top09Count = top09Count;
	}

	public Object getTop10Value() {
		return top10Value;
	}

	public void setTop10Value(Object top10Value) {
		this.top10Value = top10Value;
	}

	public long getTop10Count() {
		return top10Count;
	}

	public void setTop10Count(long top10Count) {
		this.top10Count = top10Count;
	}

	public boolean isTop01_valNA() {
		return top01_valNA;
	}

	public void setTop01_valNA(boolean top01_valNA) {
		this.top01_valNA = top01_valNA;
	}

	public boolean isTop01_countNA() {
		return top01_countNA;
	}

	public void setTop01_countNA(boolean top01_countNA) {
		this.top01_countNA = top01_countNA;
	}

	public boolean isTop02_valNA() {
		return top02_valNA;
	}

	public void setTop02_valNA(boolean top02_valNA) {
		this.top02_valNA = top02_valNA;
	}

	public boolean isTop02_countNA() {
		return top02_countNA;
	}

	public void setTop02_countNA(boolean top02_countNA) {
		this.top02_countNA = top02_countNA;
	}

	public boolean isTop03_valNA() {
		return top03_valNA;
	}

	public void setTop03_valNA(boolean top03_valNA) {
		this.top03_valNA = top03_valNA;
	}

	public boolean isTop03_countNA() {
		return top03_countNA;
	}

	public void setTop03_countNA(boolean top03_countNA) {
		this.top03_countNA = top03_countNA;
	}

	public boolean isTop04_valNA() {
		return top04_valNA;
	}

	public void setTop04_valNA(boolean top04_valNA) {
		this.top04_valNA = top04_valNA;
	}

	public boolean isTop04_countNA() {
		return top04_countNA;
	}

	public void setTop04_countNA(boolean top04_countNA) {
		this.top04_countNA = top04_countNA;
	}

	public boolean isTop05_valNA() {
		return top05_valNA;
	}

	public void setTop05_valNA(boolean top05_valNA) {
		this.top05_valNA = top05_valNA;
	}

	public boolean isTop05_countNA() {
		return top05_countNA;
	}

	public void setTop05_countNA(boolean top05_countNA) {
		this.top05_countNA = top05_countNA;
	}

	public boolean isTop06_valNA() {
		return top06_valNA;
	}

	public void setTop06_valNA(boolean top06_valNA) {
		this.top06_valNA = top06_valNA;
	}

	public boolean isTop06_countNA() {
		return top06_countNA;
	}

	public void setTop06_countNA(boolean top06_countNA) {
		this.top06_countNA = top06_countNA;
	}

	public boolean isTop07_valNA() {
		return top07_valNA;
	}

	public void setTop07_valNA(boolean top07_valNA) {
		this.top07_valNA = top07_valNA;
	}

	public boolean isTop07_countNA() {
		return top07_countNA;
	}

	public void setTop07_countNA(boolean top07_countNA) {
		this.top07_countNA = top07_countNA;
	}

	public boolean isTop08_valNA() {
		return top08_valNA;
	}

	public void setTop08_valNA(boolean top08_valNA) {
		this.top08_valNA = top08_valNA;
	}

	public boolean isTop08_countNA() {
		return top08_countNA;
	}

	public void setTop08_countNA(boolean top08_countNA) {
		this.top08_countNA = top08_countNA;
	}

	public boolean isTop09_valNA() {
		return top09_valNA;
	}

	public void setTop09_valNA(boolean top09_valNA) {
		this.top09_valNA = top09_valNA;
	}

	public boolean isTop09_countNA() {
		return top09_countNA;
	}

	public void setTop09_countNA(boolean top09_countNA) {
		this.top09_countNA = top09_countNA;
	}

	public boolean isTop10_valNA() {
		return top10_valNA;
	}

	public void setTop10_valNA(boolean top10_valNA) {
		this.top10_valNA = top10_valNA;
	}

	public boolean isTop10_countNA() {
		return top10_countNA;
	}

	public void setTop10_countNA(boolean top10_countNA) {
		this.top10_countNA = top10_countNA;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(avg);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result
				+ ((columnType == null) ? 0 : columnType.hashCode());
		result = prime * result + (int) (count ^ (count >>> 32));
		temp = Double.doubleToLongBits(deviation);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (emptyCount ^ (emptyCount >>> 32));
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));

		temp = Double.doubleToLongBits(q1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(median);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(q3);
		result = prime * result + (int) (temp ^ (temp >>> 32));

		result = prime * result
				+ (int) (negativeValueCount ^ (negativeValueCount >>> 32));
		result = prime * result + (int) (nullCount ^ (nullCount >>> 32));
		result = prime * result
				+ (int) (positiveValueCount ^ (positiveValueCount >>> 32));
		result = prime * result
				+ (int) (uniqueValueCount ^ (uniqueValueCount >>> 32));
		result = prime * result + (int) (zeroCount ^ (zeroCount >>> 32));
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
		ColumnValueAnalysisResult other = (ColumnValueAnalysisResult) obj;
		if (Double.doubleToLongBits(avg) != Double.doubleToLongBits(other.avg))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (columnType == null) {
			if (other.columnType != null)
				return false;
		} else if (!columnType.equals(other.columnType))
			return false;
		if (count != other.count)
			return false;
		if (Double.doubleToLongBits(deviation) != Double
				.doubleToLongBits(other.deviation))
			return false;
		if (Double.doubleToLongBits(q1) != Double.doubleToLongBits(other.q1))
			return false;
		if (Double.doubleToLongBits(median) != Double.doubleToLongBits(other.median))
			return false;
		if (Double.doubleToLongBits(q3) != Double.doubleToLongBits(other.q3))
			return false;
		if (emptyCount != other.emptyCount)
			return false;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
			return false;
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
			return false;
		if (negativeValueCount != other.negativeValueCount)
			return false;
		if (nullCount != other.nullCount)
			return false;
		if (positiveValueCount != other.positiveValueCount)
			return false;
		if (uniqueValueCount != other.uniqueValueCount)
			return false;
		if (zeroCount != other.zeroCount)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ColumnValueAnalysisResult [columnName=");
		builder.append(columnName);
		builder.append(", columnType=");
		builder.append(columnType);
		builder.append(", count=");
		builder.append(count);
		builder.append(", uniqueValueCount=");
		builder.append(uniqueValueCount);
		builder.append(", nullCount=");
		builder.append(nullCount);
		builder.append(", emptyCount=");
		builder.append(emptyCount);
		builder.append(", zeroCount=");
		builder.append(zeroCount);
		builder.append(", positiveValueCount=");
		builder.append(positiveValueCount);
		builder.append(", negativeValueCount=");
		builder.append(negativeValueCount);
		builder.append(", avg=");
		builder.append(avg);
		builder.append(", min=");
		builder.append(min);
		builder.append(", max=");
		builder.append(max);
		builder.append(", deviation=");
		builder.append(deviation);

		builder.append(", q1=");
		builder.append(q1);
		builder.append(", median=");
		builder.append(median);
		builder.append(", q3=");
		builder.append(q3);

		builder.append(", countNA=");
		builder.append(countNA);
		builder.append(", uniqueValueCountNA=");
		builder.append(uniqueValueCountNA);
		builder.append(", nullCountNA=");
		builder.append(nullCountNA);
		builder.append(", emptyCountNA=");
		builder.append(emptyCountNA);
		builder.append(", zeroCountNA=");
		builder.append(zeroCountNA);
		builder.append(", positiveValueCountNA=");
		builder.append(positiveValueCountNA);
		builder.append(", negativeValueCountNA=");
		builder.append(negativeValueCountNA);
		builder.append(", avgNA=");
		builder.append(avgNA);
		builder.append(", minNA=");
		builder.append(minNA);
		builder.append(", maxNA=");
		builder.append(maxNA);
		builder.append(", deviationNA=");
		builder.append(deviationNA);
		builder.append("]");
		return builder.toString();
	}

	
}
