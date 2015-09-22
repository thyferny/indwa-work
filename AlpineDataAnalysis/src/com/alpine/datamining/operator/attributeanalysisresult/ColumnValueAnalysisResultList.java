package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class ColumnValueAnalysisResultList {

	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "ColumnValueAnalysisResultElement")
	// XmlElement sets the name of the entities
	@XmlElement(name = "ColumnValueAnalysisResult")
	private List<ColumnValueAnalysisResult> hadoopColumnValueAnalysisResultList;

	public List<ColumnValueAnalysisResult> getTheHadoopColumnValueAnalysisResultList() {
		return hadoopColumnValueAnalysisResultList;
	}
	@XmlTransient
	public void setTheHadoopColumnValueAnalysisResultList(
			List<ColumnValueAnalysisResult> hadoopColumnValueAnalysisResultList) {
		this.hadoopColumnValueAnalysisResultList = hadoopColumnValueAnalysisResultList;
	}
	public ColumnValueAnalysisResultList(){
		
	}
	public ColumnValueAnalysisResultList(List<ColumnValueAnalysisResult>  theList){
		hadoopColumnValueAnalysisResultList=theList;
	}
	@Override
	public String toString(){
		if (null==hadoopColumnValueAnalysisResultList){
			return null;
		}
		StringBuilder sb=new StringBuilder();
		for(ColumnValueAnalysisResult el:hadoopColumnValueAnalysisResultList){
			if(null==el){
				continue;
			}
			sb.append(el.toString());
		}
		return sb.toString();
		
	}
} 