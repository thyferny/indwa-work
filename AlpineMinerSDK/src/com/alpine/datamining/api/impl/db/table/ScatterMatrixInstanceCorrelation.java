package com.alpine.datamining.api.impl.db.table;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ScatterMatrixInstanceCorrelation")
public class ScatterMatrixInstanceCorrelation {
	
	private ScatterMatrixColumnPairs scatterMatrixColumPair;
	private Double  count;
	private Double  sumSquareX;
	private Double  sumSquareY;
	private Double  sumxy;
	private Double  sumx;
	private Double  sumy;
	private Double correlationValue;
	
	
	public ScatterMatrixInstanceCorrelation(ScatterMatrixColumnPairs columnPairs) {
		this.scatterMatrixColumPair=columnPairs;
	}
	
	public ScatterMatrixInstanceCorrelation(String columnX,String columnY) {
		this.scatterMatrixColumPair= new ScatterMatrixColumnPairs(columnX,columnY);
	}
	public ScatterMatrixInstanceCorrelation() {
	}
	

	public ScatterMatrixColumnPairs getScatterMatrixColumPair() {
		return scatterMatrixColumPair;
	}


	@XmlElement
	public void setScatterMatrixColumPair(
			ScatterMatrixColumnPairs scatterMatrixColumPair) {
		this.scatterMatrixColumPair = scatterMatrixColumPair;
	}



	public Double getCount() {
		return count;
	}


	@XmlElement
	public void setCount(Double count) {
		this.count = count;
	}



	public Double getSumSquareX() {
		return sumSquareX;
	}


	@XmlElement
	public void setSumSquareX(Double sumSquareX) {
		this.sumSquareX = sumSquareX;
	}



	public Double getSumSquareY() {
		return sumSquareY;
	}


	@XmlElement
	public void setSumSquareY(Double sumSquareY) {
		this.sumSquareY = sumSquareY;
	}



	public Double getSumxy() {
		return sumxy;
	}


	@XmlElement
	public void setSumxy(Double sumxy) {
		this.sumxy = sumxy;
	}



	public Double getSumx() {
		return sumx;
	}


	@XmlElement
	public void setSumx(Double sumx) {
		this.sumx = sumx;
	}



	public Double getSumy() {
		return sumy;
	}


	@XmlElement
	public void setSumy(Double sumy) {
		this.sumy = sumy;
	}



	public Double getCorrelationValue() {
		return correlationValue;
	}


	@XmlElement
	public void setCorrelationValue(Double correlationValue) {
		this.correlationValue = correlationValue;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((correlationValue == null) ? 0 : correlationValue.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime
				* result
				+ ((scatterMatrixColumPair == null) ? 0
						: scatterMatrixColumPair.hashCode());
		result = prime * result
				+ ((sumSquareX == null) ? 0 : sumSquareX.hashCode());
		result = prime * result
				+ ((sumSquareY == null) ? 0 : sumSquareY.hashCode());
		result = prime * result + ((sumx == null) ? 0 : sumx.hashCode());
		result = prime * result + ((sumxy == null) ? 0 : sumxy.hashCode());
		result = prime * result + ((sumy == null) ? 0 : sumy.hashCode());
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
		ScatterMatrixInstanceCorrelation other = (ScatterMatrixInstanceCorrelation) obj;
		if (correlationValue == null) {
			if (other.correlationValue != null)
				return false;
		} else if (!correlationValue.equals(other.correlationValue))
			return false;
		if (count == null) {
			if (other.count != null)
				return false;
		} else if (!count.equals(other.count))
			return false;
		if (scatterMatrixColumPair == null) {
			if (other.scatterMatrixColumPair != null)
				return false;
		} else if (!scatterMatrixColumPair.equals(other.scatterMatrixColumPair))
			return false;
		if (sumSquareX == null) {
			if (other.sumSquareX != null)
				return false;
		} else if (!sumSquareX.equals(other.sumSquareX))
			return false;
		if (sumSquareY == null) {
			if (other.sumSquareY != null)
				return false;
		} else if (!sumSquareY.equals(other.sumSquareY))
			return false;
		if (sumx == null) {
			if (other.sumx != null)
				return false;
		} else if (!sumx.equals(other.sumx))
			return false;
		if (sumxy == null) {
			if (other.sumxy != null)
				return false;
		} else if (!sumxy.equals(other.sumxy))
			return false;
		if (sumy == null) {
			if (other.sumy != null)
				return false;
		} else if (!sumy.equals(other.sumy))
			return false;
		return true;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScatterMatrixInstanceCorrelation [scatterMatrixColumPair=");
		builder.append(scatterMatrixColumPair);
		builder.append(", count=");
		builder.append(count);
		builder.append(", sumSquareX=");
		builder.append(sumSquareX);
		builder.append(", sumSquareY=");
		builder.append(sumSquareY);
		builder.append(", sumxy=");
		builder.append(sumxy);
		builder.append(", sumx=");
		builder.append(sumx);
		builder.append(", sumy=");
		builder.append(sumy);
		builder.append(", correlationValue=");
		builder.append(correlationValue);
		builder.append("]");
		return builder.toString();
	}
	
	
}