package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BinHistogranAnalysisList {

	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "hadoobBinHistogramList")
	// XmlElement sets the name of the entities
	@XmlElement(name = "BinHistogramAnalysisResult")
	private List<BinHistogramAnalysisResult> hadoopHistogramSnapShotList;

	public void setHadoopHistogramSnapShotList(List<BinHistogramAnalysisResult> hadoopHistogramSnapShotList) {
		this.hadoopHistogramSnapShotList = hadoopHistogramSnapShotList;
	}

	public List<BinHistogramAnalysisResult> getBooksList() {
		return hadoopHistogramSnapShotList;
	}
} 