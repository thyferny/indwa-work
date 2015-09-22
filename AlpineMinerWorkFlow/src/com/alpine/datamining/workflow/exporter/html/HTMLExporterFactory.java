/**
 * ClassName  HTMLExporterFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.awt.Font;
import java.util.HashMap;
import java.util.Locale;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.ClusterScatterVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTableVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DropDownAndTableListVisualizationOutput;
import com.alpine.datamining.api.impl.visual.DropDownListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.JFreeChartImageVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.MultiChartImageVisualizationOutput;
import com.alpine.datamining.api.impl.visual.MultiDataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.SplitModelLirTableVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.TextVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.TreeVisualizationOutPut;

 

/**
 * @author John Zhao
 *
 */
public class HTMLExporterFactory {
	
	
//	public static Font chapterFont_zh_CN=null;
//	public static Font chapterFont_en_US=null;
//	public static Font contentFont_zh_CN=null;
//	public static Font contentFont_en_US=null;
//	public static Font sectionFont_zh_CN=null;
//	public static Font sectionFont_en_US=null;
//	public static Font tableLinkFont_zh_CN=null;
//	public static Font tableLinkFont_en_US=null;
	
	private static HTMLExporterFactory instance =null;
	
	private HashMap<String, VisualOutPutHTMLExporter> exporterMap;
//	private Locale defaultLocale;
	
	public static synchronized HTMLExporterFactory getInstance(){
		if(instance==null){
			instance=new HTMLExporterFactory();
		}
		return instance;
	}
	
	private HTMLExporterFactory() {

		initExporters();
	}

	private void initExporters() {
		exporterMap = new HashMap<String, VisualOutPutHTMLExporter>();
		exporterMap.put(DataTableVisualizationOutPut.class.getName(),new DataTableHTMLExporter());
		exporterMap.put(JFreeChartImageVisualizationOutPut.class.getName(),
				new JFreeChartImageHTMLExporter());
		exporterMap.put(TextVisualizationOutPut.class.getName(),
				new TextOutPutHTMLExporter());
		exporterMap.put( TreeVisualizationOutPut.class.getName(),
				new DecisionTreeHTMLExporter());
		exporterMap.put(DataTextAndTableListVisualizationOutPut.class.getName(),new TextAndTableOutPutHTMLExporter());
		exporterMap.put(ClusterScatterVisualizationOutPut.class.getName(),new ClusterJFreeChartImageHTMLExporter());

		exporterMap.put(DropDownListVisualizationOutPut.class.getName(), new DropDownListChartImageHTMLExporter());
		
		exporterMap.put(DropDownAndTableListVisualizationOutput.class.getName(), new DropDownTableHTMLExporter());
		exporterMap.put(MultiDataTextAndTableListVisualizationOutPut.class.getName(), new MultiTextAndTableOutPutHTMLExporter());
		exporterMap.put(MultiChartImageVisualizationOutput.class.getName(), new MultiChartImageOutPutHTMLExporter());
		exporterMap.put(SplitModelLirTableVisualizationOutPut.class.getName(), new DataTableHTMLExporter());
	}



	public VisualOutPutHTMLExporter getVisualOutPutExporter(VisualizationOutPut vOutPut){
		
		VisualOutPutHTMLExporter exporter=exporterMap.get(vOutPut.getClass().getName());
		if(exporter==null){
			throw new RuntimeException("VisualOutPutHTMLExporter not defined for :"+vOutPut.getClass().getName());
		}else{
			return exporter;
		}
		
	}
	
//	/**
//	 * @return
//	 */
//	public   Font getSectionFont() {
//
//		if (Locale.CHINA.equals(defaultLocale)||
//				Locale.getDefault().equals(Locale.CHINA)) {
//				return sectionFont_zh_CN;
//		} else {
//			return	sectionFont_en_US;
//		}
//		 
//
//	}

//	public   Font getContentFont() {
//		if (Locale.CHINA.equals(defaultLocale)
//				||Locale.getDefault().equals(Locale.CHINA)) {
//			return contentFont_zh_CN;
// 
//		} else {
//			return  contentFont_en_US;
//		}
// 
//	}
//
//	/**
//	 * @return
//	 */
//	public   Font getChapterFont() {
//		if (Locale.CHINA.equals(defaultLocale)||
//				Locale.getDefault().equals(Locale.CHINA)) {
//				return chapterFont_zh_CN;
//		 
//		} else {
//
//			return chapterFont_en_US;
//		}
//	}

	/**
	 * @param default1
	 */
//	public void setLocale(Locale defaultLocale) {
//		this.defaultLocale=defaultLocale;
//		
//	}

	/**
	 * @return
	 */
//	public Font getTableLinkFont() {
//		if (Locale.CHINA.equals(defaultLocale)||
//				Locale.getDefault().equals(Locale.CHINA)) {
//				return tableLinkFont_zh_CN;
//		 
//		} else {
//
//			return tableLinkFont_en_US;
//		}
//	}
}
