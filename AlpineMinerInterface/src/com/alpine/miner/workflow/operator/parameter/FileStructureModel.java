/**
 * ClassName FileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-24
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;


/**
 * @author Jeff Dong
 *
 */
public interface FileStructureModel extends ParameterObject, AnalysisFileStructureModel{

	public FileStructureModel clone() throws CloneNotSupportedException ;
	
}
