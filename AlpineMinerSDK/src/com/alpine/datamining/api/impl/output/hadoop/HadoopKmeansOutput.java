/**
 * ClassName HadoopKmeansOutput.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output.hadoop;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansOutput extends AbstractAnalyzerOutPut {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6608943232992437609L;
	
	private ClusterOutputModel clusterModel;

	
	public HadoopKmeansOutput(ClusterOutputModel clusterModel) {
		super();
		this.clusterModel = clusterModel;
	}

	public ClusterOutputModel getClusterModel() {
		return clusterModel;
	}

	public void setClusterModel(ClusterOutputModel clusterModel) {
		this.clusterModel = clusterModel;
	}

 
	
}
