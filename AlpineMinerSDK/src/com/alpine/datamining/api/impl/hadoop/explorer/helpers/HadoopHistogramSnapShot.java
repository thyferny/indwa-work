package com.alpine.datamining.api.impl.hadoop.explorer.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;

@XmlRootElement(name = "HadoopHistogramSnapShot")
@XmlType(propOrder = { "tableName", "columnName","stepSize", "max","min" })
public class HadoopHistogramSnapShot {
		private double stepSize;
		private String tableName;
		private String columnName;
		private double max;
		private double min;
		private AnalysisColumnBin desiredBin;
		
		public HadoopHistogramSnapShot(AnalysisColumnBin bin, String pureFileName){
			if(null==bin||null==pureFileName){
				throw new IllegalArgumentException("AnalysisColumnBin/FileName must not be null ");
			}
			this.desiredBin=bin;
			this.tableName=pureFileName;
			this.max=bin.getMax();
			this.min=bin.getMin();
			
		}
		
		public HadoopHistogramSnapShot() {
			
		}

		public AnalysisColumnBin getDesiredBin() {
			return desiredBin;
		}
		@XmlTransient
		public void setDesiredBin(AnalysisColumnBin desiredBin) {
			this.desiredBin = desiredBin;
		}

		public double getMax() {
			return max;
		}

		public double getMin() {
			return min;
		}

		public double getStepSize() {
			return stepSize;
		}
		@XmlElement
		public void setStepSize(double stepSize) {
			this.stepSize = stepSize;
		}
		public String getTableName() {
			return tableName;
		}
		@XmlElement
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		
		public String getColumnName() {
			return columnName;
		}
		@XmlElement
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		@XmlElement
		public void setMax(double max) {
			this.max=max;
			
		}
		@XmlElement
		public void setMin(double min) {
			this.min=min;
		}

		@Override
		public String toString() {
			return String
					.format("HadoopHistogramSnapShot [stepSize=%s, tableName=%s, columnName=%s, max=%s, min=%s, desiredBin=%s]",
							stepSize, tableName, columnName, max, min,
							desiredBin);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((columnName == null) ? 0 : columnName.hashCode());
			long temp;
			temp = Double.doubleToLongBits(max);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(min);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(stepSize);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((tableName == null) ? 0 : tableName.hashCode());
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
			HadoopHistogramSnapShot other = (HadoopHistogramSnapShot) obj;
			if (columnName == null) {
				if (other.columnName != null)
					return false;
			} else if (!columnName.equals(other.columnName))
				return false;
			if (Double.doubleToLongBits(max) != Double
					.doubleToLongBits(other.max))
				return false;
			if (Double.doubleToLongBits(min) != Double
					.doubleToLongBits(other.min))
				return false;
			if (Double.doubleToLongBits(stepSize) != Double
					.doubleToLongBits(other.stepSize))
				return false;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			return true;
		}
}