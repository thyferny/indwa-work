package com.alpine.datamining.api.impl.db.table;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.alpine.utility.file.StringUtil;
@XmlRootElement(name = "ScatterMatrixColumnPairs")
public class ScatterMatrixColumnPairs {
	
	private String columnX;
	
	private String columnY;
	public ScatterMatrixColumnPairs(){
		
	}
	public ScatterMatrixColumnPairs(String columnX, String columnY) {
		super();
		this.columnX = columnX;
		this.columnY = columnY;
	}

	public String getColumnX() {
		return columnX;
	}
	@XmlElement
	public void setColumnX(String columnX) {
		this.columnX = columnX;
	}
	
	public String getColumnY() {
		return columnY;
	}
	@XmlElement
	public void setColumnY(String columnY) {
		this.columnY = columnY;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScatterMatrixColumnPairs [columnX=");
		builder.append(columnX);
		builder.append(", columnY=");
		builder.append(columnY);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = 1;
		if (this.getColumnX() != null) {
			hash = hash + this.getColumnX().hashCode();
		}
		if (this.getColumnY() != null) {
			hash = hash + this.getColumnY().hashCode();
		}		
		return hash;
	}	
 
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ScatterMatrixColumnPairs)){
			return false;
		}else if(!StringUtil.isEmpty(((ScatterMatrixColumnPairs)obj).getColumnX())
				&&((ScatterMatrixColumnPairs)obj).getColumnX().equals(columnX)
				&&!StringUtil.isEmpty(((ScatterMatrixColumnPairs)obj).getColumnY())
				&&((ScatterMatrixColumnPairs)obj).getColumnY().equals(columnY)){
			return true;
		}else{
			return false;
		}
	}

}
