package com.alpine.datamining.api.impl.visual.widgets;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.tools.StringHandler;

public class ScatterMatrixPlotChart extends VisualizationChart {
    private static final Logger itsLogger =Logger.getLogger(ScatterMatrixPlotChart.class);

    public static int IMAGESIZE=120;
	
	public static int IMAGEINTERVAL=10;
	
	private static Font corrFont=new Font(null, "Arial", 16, SWT.NONE);
	
	private static Font columnNameFont=new Font(null, "Arial", 10, SWT.BOLD);
	
	private static Border border= new LineBorder();
	
	private FreeformLayeredPane scatterMatrixChart = null;
	
	private AnalyzerOutPutScatterMatrix model;
	
	private String tempFolderName;
	
	private static final int ENGLISH_UNIT = 6;
	private static final int CHINESE_UNIT = 15;
	
	public void createChart(final AnalyzerOutPutScatterMatrix model, Map<ScatterMatrixColumnPairs,JFreeChart> imageMap) {
		this.model=model;
		scatterMatrixChart = new FreeformLayeredPane();
		scatterMatrixChart.setLayoutManager(new FreeformLayout());
		scatterMatrixChart.setBorder(new MarginBorder(5));
		scatterMatrixChart.setBackgroundColor(ColorConstants.white);
		scatterMatrixChart.setOpaque(true);
		scatterMatrixChart.setFocusTraversable(true);
		
		tempFolderName = System.getProperty("java.io.tmpdir")+File.separator+System.currentTimeMillis();
		
		File imageTempFolder = new File(tempFolderName);
		if(!imageTempFolder.exists()){
			imageTempFolder.mkdir();
		}
		
		String[] columnNames = model.getColumnNames();
		
		showWidth=columnNames.length*(IMAGESIZE+IMAGEINTERVAL)+IMAGEINTERVAL;
		showHeight=columnNames.length*(IMAGESIZE+IMAGEINTERVAL)+IMAGEINTERVAL;
			
		List<Double> corrList = model.getCorrList();
		int corrIndex=0;
		for (int row=0; row<columnNames.length; row++) {
			for (int col=0; col<columnNames.length; col++) {
				if(row<col){//set correlation	
					String corr = (null==corrList.get(corrIndex))?"N/A":AlpineMath.doubleExpression(corrList.get(corrIndex));
					Label label = new Label(corr);
					label.setFont(corrFont);
					label.setBorder(border);
					label.setLocation(new Point(IMAGEINTERVAL+col*(IMAGESIZE+IMAGEINTERVAL), IMAGEINTERVAL+row*(IMAGESIZE+IMAGEINTERVAL)));
					label.setSize(IMAGESIZE, IMAGESIZE);
					
					ScatterMatrixToolTip toolTip = new ScatterMatrixToolTip();	
					toolTip.setMessage(StringHandler.doubleQ(columnNames[row])+"/"+StringHandler.doubleQ(columnNames[col]));
					
					String message=VisualLanguagePack.getMessage(VisualLanguagePack.SCATTER_MATRIX_CORRELATION,Locale.getDefault());
					message = NLSUtility.bind(message, StringHandler.doubleQ(columnNames[row]), StringHandler.doubleQ(columnNames[col]));
					message+=corr;
					toolTip.setDisplayMessage(message);
					toolTip.setX(0);
					toolTip.setY(0);
					if (Locale.getDefault().equals(Locale.CHINA)) {
						toolTip.setWidth(message.length()*CHINESE_UNIT+10);
					}else{
						toolTip.setWidth(message.length()*ENGLISH_UNIT+10);
					}
					
					toolTip.setHeight(20);
					toolTip.setBounds();
					label.setToolTip(toolTip);
					scatterMatrixChart.add(label);
					corrIndex++;
				}else if(row>col){//set image
					ScatterMatrixColumnPairs columnParis =new ScatterMatrixColumnPairs(columnNames[col],columnNames[row]);
					String imageName=imageTempFolder+File.separator+columnParis.getColumnX()+"_"+columnParis.getColumnY()+".png";
			
					JFreeChart chart = imageMap.get(columnParis);
					
					if(chart==null){
						continue;
					}
					try {
						ChartUtilities.saveChartAsPNG(new File(imageName),chart, IMAGESIZE, IMAGESIZE);
					} catch (IOException e) {
						itsLogger.error(e.getMessage(),e);
					}
					
					Image image = new Image(null, imageName);
					ScatterMatrixImageFigure imageFigure = new ScatterMatrixImageFigure(image);
					imageFigure.setColumnPair(columnParis);
					imageFigure.setLocation(new Point(IMAGEINTERVAL+col*(IMAGESIZE+IMAGEINTERVAL), IMAGEINTERVAL+row*(IMAGESIZE+IMAGEINTERVAL)));
					imageFigure.setSize(IMAGESIZE, IMAGESIZE);
					imageFigure.setBorder(border);
					
					ScatterMatrixToolTip toolTip = new ScatterMatrixToolTip();	
					toolTip.setMessage(StringHandler.doubleQ(columnNames[col])+"/"+StringHandler.doubleQ(columnNames[row]));
					
					String message=VisualLanguagePack.getMessage(VisualLanguagePack.SCATTER_MATRIX_IMAGE,Locale.getDefault());
					message = NLSUtility.bind(message, StringHandler.doubleQ(columnNames[col]), StringHandler.doubleQ(columnNames[row]));
					toolTip.setDisplayMessage(message);
					toolTip.setX(0);
					toolTip.setY(0);
					if (Locale.getDefault().equals(Locale.CHINA)) {
						toolTip.setWidth(message.length()*CHINESE_UNIT+10);
					}else{
						toolTip.setWidth(message.length()*ENGLISH_UNIT+10);
					}
					toolTip.setHeight(20);
					toolTip.setBounds();
					imageFigure.setToolTip(toolTip);
					
					scatterMatrixChart.add(imageFigure);					
				}else{//set name
					Label label = new Label(columnNames[col]);
					label.setFont(columnNameFont);
					
					label.setBorder(border);
					label.setLocation(new Point(IMAGEINTERVAL+col*(IMAGESIZE+IMAGEINTERVAL), IMAGEINTERVAL+row*(IMAGESIZE+IMAGEINTERVAL)));
					label.setSize(IMAGESIZE, IMAGESIZE);
					
					ScatterMatrixToolTip toolTip = new ScatterMatrixToolTip();	
					toolTip.setMessage(columnNames[col]);
					
					toolTip.setDisplayMessage(columnNames[col]);
					toolTip.setX(0);
					toolTip.setY(0);
					if (Locale.getDefault().equals(Locale.CHINA)) {
						toolTip.setWidth(columnNames[col].length()*CHINESE_UNIT+10);
					}else{
						toolTip.setWidth(columnNames[col].length()*(ENGLISH_UNIT+1)+10);
					}
					toolTip.setHeight(20);
					toolTip.setBounds();
					label.setToolTip(toolTip);
					
					scatterMatrixChart.add(label);
				}
			}
			
		}
	}
	
	public FreeformLayeredPane getChart() {
		return scatterMatrixChart;
	}

	public AnalyzerOutPutScatterMatrix getModel() {
		return model;
	}

	public String getTempFolderName() {
		return tempFolderName;
	}
	
	@SuppressWarnings("unchecked")
	public Image getImage(){
		Image img = new Image(null, showWidth, showHeight);
		GC gc = new GC(img);
		Graphics graphics = new SWTGraphics(gc);
		List<IFigure> list = scatterMatrixChart.getChildren();
		for (IFigure f : list) {
			if (f.isVisible()) {
				f.paint(graphics);
			}
		}
		graphics.dispose();
		gc.dispose();
		return img;
	}
	
}
