package com.alpine.datamining.api.impl.visual.resource;

import java.awt.Font;
import java.util.Locale;

public class VisualResource {

	private static Font ChartFont=null ;
	private static   Locale local; 
	
	public static Locale getLocal() {
		return local;
	}

	public static void setLocale(Locale local) {
		VisualResource.local = local;
	}
	static int size = 12;
	public static Font getChartFont(){
		
		if(ChartFont != null)
			return ChartFont;
		ChartFont = new Font(VisualLanguagePack.getMessage(VisualLanguagePack.CHARACTER_FONT,local),Font.BOLD,size) ;
		return ChartFont;
	}
}
