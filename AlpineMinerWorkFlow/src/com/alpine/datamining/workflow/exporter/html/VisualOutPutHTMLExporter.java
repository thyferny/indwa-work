/**
 * ClassName  VisulOutPutExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.awt.Color;
import java.util.List;

import com.alpine.datamining.api.VisualizationOutPut;

/**
 * @author John Zhao
 *
 */
public interface VisualOutPutHTMLExporter {
	public static Color table_header_color=new Color(210, 210, 210);

	/**
	 * @param visualizationOutPut
	 * @param tempFileList 
	 * @return
	 */
	public StringBuffer export(VisualizationOutPut visualizationOutPut, 
			List<String> tempFileList,String rootPath) throws Exception;

}
