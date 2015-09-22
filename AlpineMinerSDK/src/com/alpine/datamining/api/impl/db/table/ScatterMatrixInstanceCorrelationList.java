package com.alpine.datamining.api.impl.db.table;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class ScatterMatrixInstanceCorrelationList {

	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "ScatterMatrixInstanceCorrelationElement")
	// XmlElement sets the name of the entities
	@XmlElement(name = "ScatterMatrixInstanceCorrelation")
	private List<ScatterMatrixInstanceCorrelation> scatterMatrixInstanceCorrelationList;
	
	public ScatterMatrixInstanceCorrelationList(){
	}
	
	public List<ScatterMatrixInstanceCorrelation> getScatterMatrixInstanceCorrelationList() {
		return scatterMatrixInstanceCorrelationList;
	}

	@XmlTransient
	public void setScatterMatrixInstanceCorrelationList(
			List<ScatterMatrixInstanceCorrelation> scatterMatrixInstanceCorrelationList) {
		this.scatterMatrixInstanceCorrelationList = scatterMatrixInstanceCorrelationList;
	}


	
	
	@Override
	public String toString(){
		if (null==scatterMatrixInstanceCorrelationList){
			return null;
		}
		StringBuilder sb=new StringBuilder();
		for(ScatterMatrixInstanceCorrelation el:scatterMatrixInstanceCorrelationList){
			if(null==el){
				continue;
			}
			sb.append(el.toString());
		}
		return sb.toString();
		
	}
} 