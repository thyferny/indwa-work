/**
 * ClassName MultiTextAndTableOutPutHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.util.List;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.MultiDataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.workflow.util.ToHtmlWriter;

public class MultiTextAndTableOutPutHTMLExporter extends TextAndTableOutPutHTMLExporter {
	
	public static final int MAX_ROWS = 200;
	//for all table
	public static final int MAX_COLS = 6;

	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, String rootPath) throws Exception {
		
		MultiDataTextAndTableListVisualizationOutPut out=(MultiDataTextAndTableListVisualizationOutPut)visualizationOutPut;
		List<DataTextAndTableListVisualizationOutPut> outputList=out.getTextAndTableListOutput();
		StringBuffer sb=new StringBuffer();
		String[] avaiableValues=out.getMultiTextAndTableListEntity().getAvaiableValue();
		int i=0;
		for(DataTextAndTableListVisualizationOutPut output:outputList){
			ToHtmlWriter htmlWriter=new ToHtmlWriter();
			htmlWriter.writeH3(avaiableValues[i]);
			sb.append(htmlWriter.toString());
			sb.append(super.export(output, tempFileList, rootPath));
			i++;
		}
		return sb;
	}

}
