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
import com.alpine.datamining.api.impl.visual.ClusterScatterVisualizationOutPut;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;
import org.apache.log4j.Logger;

/**
 * @author John Zhao
 *
 */
public class ClusterJFreeChartImageHTMLExporter implements VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(ClusterJFreeChartImageHTMLExporter.class);

    /* (non-Javadoc)
	 * @see com.alpine.datamining.exporter.VisualOutPutExporter#export(com.alpine.datamining.api.VisualizationOutPut)
	 */
	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList,String rootPath)  throws  Exception {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		
		ClusterScatterVisualizationOutPut out=(ClusterScatterVisualizationOutPut)visualizationOutPut;
		String imagePath = getImage(out,tempFileList,rootPath );
 
		htmlWriter.writeImg(imagePath);
//		result.append("<p><img src=\"").append(imagePath).append("\"></p>");
		return  htmlWriter.toStringBuffer();
 
	}

	/**
	 * @param out
	 * @param tempFileList 
	 * @return
	 */
	private String getImage(ClusterScatterVisualizationOutPut out, List<String> tempFileList, String rootPath )   throws  Exception {
		//String tempDir=System.getProperty("java.io.tmpdir"); 
		int i = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, i);
		String name = System.currentTimeMillis()+".jpg";
		String fileName=curdir+File.separator+name;
		ClusterScatterEntity entity= (ClusterScatterEntity)out.getVisualizationObject();
				//500*350 is the fit
				int defaultWidth=500;
				int defaultHeight=350;
				//always use this 
		ChartUtilities.saveChartAsJPEG(new File(fileName),(JFreeChart)entity.getJfreechart(), defaultWidth, defaultHeight);
		itsLogger.debug("ClusterJFreeChartImageHTMLExporter export to:"+fileName);
			
		String imageFile = "."+File.separator+name;
		return imageFile;
	}
}

