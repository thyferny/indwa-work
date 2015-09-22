package com.alpine.datamining.api.impl.hadoop.explorer.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HadoopHistogramSnapShotList {

	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "hadoobBinHistogramList")
	// XmlElement sets the name of the entities
	@XmlElement(name = "HadoopHistogramSnapShot")
	private List<HadoopHistogramSnapShot> hadoopHistogramSnapShotList;

	public void setHadoopHistogramSnapShotList(List<HadoopHistogramSnapShot> hadoopHistogramSnapShotList) {
		this.hadoopHistogramSnapShotList = hadoopHistogramSnapShotList;
	}

	public List<HadoopHistogramSnapShot> getBooksList() {
		return hadoopHistogramSnapShotList;
	}
} 