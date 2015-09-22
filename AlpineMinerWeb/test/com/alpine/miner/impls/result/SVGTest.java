/**
 * ClassName :SVGTest.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.result;

 
import java.io.File;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;

 

/**
 * @author zhaoyong
 *
 */
public class SVGTest {
	private static final String SVG_FILE="/home/zhaoyong/dev/workspace/AlpineMinerWeb/test/com/alpine/miner/impls/result/test.svg";


public static void main (String args[]) throws  Exception{
	SVGConverter svgConverter = new SVGConverter();
	svgConverter.setDestinationType(DestinationType.PNG);
	svgConverter.setSources(new String[]{ new File(SVG_FILE).toURI().toString() });
	String imgDest =SVG_FILE+".png";
	svgConverter.setDst(new File(imgDest ));
	svgConverter.execute();
}
}
