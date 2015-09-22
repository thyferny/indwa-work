/**
 * ClassName  DataTableHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.JFreeChartImageVisualizationOutPut;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import org.apache.log4j.Logger;


/**
 * @author John Zhao
 *
 */
public class JFreeChartImageHTMLExporter implements VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(JFreeChartImageHTMLExporter.class);

    /* (non-Javadoc)
	 * @see com.alpine.datamining.exporter.VisualOutPutExporter#export(com.alpine.datamining.api.VisualizationOutPut)
	 */
	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList,String rootPath)  throws  Exception {
//		StringBuffer result= new StringBuffer();
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		JFreeChartImageVisualizationOutPut out=(JFreeChartImageVisualizationOutPut)visualizationOutPut;
		String imagePath = getImage(out,tempFileList,rootPath );
		// <p><img src="operator_icons/aggregate.gif"></p>
		htmlWriter.writeImg(imagePath);
//		result.append("<p><img src=\"").append(imagePath).append("\"></p>");
		return   htmlWriter.toStringBuffer();
 
	}

	/**
	 * @param out
	 * @param tempFileList 
	 * @return
	 */
	private String getImage(JFreeChartImageVisualizationOutPut out, List<String> tempFileList, String rootPath )   throws  Exception {
		int i = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, i);
		String name = System.currentTimeMillis()+".jpg";
		String fileName=curdir+File.separator+name;
 		JFreeChart jfreeChart= (JFreeChart)out.getVisualizationObject();
				//500*350 is the fit
				int defaultWidth=500;
				int defaultHeight=350;
		//always use this 
		ChartUtilities.saveChartAsJPEG(new File(fileName),jfreeChart, defaultWidth, defaultHeight);
		itsLogger.debug("JFreeChartImageHTMLExporter export to:"+fileName);
		String imageFile = "."+File.separator+name;
		return imageFile;
	}
}

