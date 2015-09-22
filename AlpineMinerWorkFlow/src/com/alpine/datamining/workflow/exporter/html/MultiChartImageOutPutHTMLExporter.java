package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.MultiChartImageVisualizationOutput;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import org.apache.log4j.Logger;

public class MultiChartImageOutPutHTMLExporter implements
		VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(MultiChartImageOutPutHTMLExporter.class);

    @Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, String rootPath) throws Exception {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		MultiChartImageVisualizationOutput out=(MultiChartImageVisualizationOutput)visualizationOutPut;
		String imagePath = getImage(out,tempFileList,rootPath );
		htmlWriter.writeImg(imagePath);
		return   htmlWriter.toStringBuffer();
	}

	/**
	 * @param out
	 * @param tempFileList 
	 * @return
	 */
	private String getImage(MultiChartImageVisualizationOutput out, List<String> tempFileList, String rootPath )   throws  Exception {
		int i = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, i);
		String name = System.currentTimeMillis()+".jpg";
		String fileName=curdir+File.separator+name;

        ImageLoader il = new ImageLoader();
        Image img = out.getImage();
        if(img != null){
        	il.data = new ImageData[]{ img.getImageData() };
        }
        il.save(fileName, SWT.IMAGE_JPEG);
        
		itsLogger.debug("MultiChartImageOutPutHTMLExporter export to:"+fileName);
		String imageFile = "."+File.separator+name;
		return imageFile;
	}
	
}
