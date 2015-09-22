/**
 * VisualUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.resource;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.alpine.datamining.api.impl.visual.TreeVisualizationOutPut;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jimmy
 *
 */
public class VisualUtility {

	public static  void saveFigureAsJPG(TreeVisualizationOutPut out,String filePathName){
        ImageLoader il = new ImageLoader();
        Image img = out.getImg();
        if(img != null){
        	il.data = new ImageData[]{ img.getImageData() };
        }
        il.save(filePathName, SWT.IMAGE_JPEG);
	}
	private static Color[] colorConstants = {Color.red,Color.green,Color.blue,Color.orange,Color.CYAN,Color.pink,Color.gray,Color.magenta,Color.black};
	private static long coefficient = (long) 0.3;
	public static Color[] getRandomColor(int num){
		Random random = new Random(coefficient);
		Color[] colors = new Color[num]; 
		if(num<=colorConstants.length){
			for(int i=0;i<num;i++){
				colors[i] = colorConstants[i];
			}
		}else{
			for(int i=0;i<num;i++){
				if(i<colorConstants.length){
					colors[i] = colorConstants[i];
				}else{
					Color cc = createColor(random);
					colors[i] = cc;
				}
			}
		}
		return colors;
	}
	
	private static Color createColor(Random random){
		int r = (int)(random.nextDouble()*256);
		int g = (int)(random.nextDouble()*256);
		int b = (int)(random.nextDouble()*256);
		Color cc = new Color(r,g,b);
		boolean isSameColor = false;
		for(Color color:colorConstants){
			if(color.equals(cc)){
				isSameColor = true;
			}
		}
		if(isSameColor){
			Color col = createColor(random);
			return col;
		}else{
			return cc;
		}
		
	}
	
	
	private static Shape shapeScatter = new Ellipse2D.Double(-1.0, -1.0, 2.5, 2.5);
	public static Shape getScatterPoint(){
		return shapeScatter;
	}
	private static Shape shapeCenter = new Ellipse2D.Double(-7.5, -7.7, 15.0, 15.0);
	public static Shape getCenterPoint(){
		return shapeCenter;
	}
	
	private static Shape shapeCommonEllipse = new Ellipse2D.Double(-3.0,-3.0, 6.0, 6.0);
	public static Shape getCommonElliipseShape(){
		return shapeCommonEllipse;
	}
	
	private static Font treeFont=new Font(null, "Arial", 10, java.awt.Font.BOLD);
	public static Font getTreeFont(){
		return treeFont;
	}
	
	private static String leafImageName="/icons/leaf.png";
	private static String notLeafImageName="/icons/notLeaf.png";
	private static String nnNodeImageName="/icons/node.png";
	private static String nnEndNodeImageName="/icons/endNode.png";
	
	private static Image leafImage=new Image(null, VisualUtility.class.getResourceAsStream(leafImageName));
	private static Image notLeafImage=new Image(null, VisualUtility.class.getResourceAsStream(notLeafImageName));
	private static Image nnNodeImage=new Image(null, VisualUtility.class.getResourceAsStream(nnNodeImageName));
	private static Image nnEndNodeImage=new Image(null, VisualUtility.class.getResourceAsStream(nnEndNodeImageName));
	
	public static Image getLeafImage(){
		return leafImage;
	}
	
	public static Image getNotLeafImage(){
		return notLeafImage;
	}
	
	public static Image getnnNodeImage(){
		return nnNodeImage;
	}
	
	public static Image getnnEndNodeImage(){
		return nnEndNodeImage;
	}
	
	public static String resizeLabel(String label) {
		if(StringUtil.isEmpty(label)){
			label= "";
		}
		if(label.length()>14){
			label=label.substring(0,10)+"...";
		}
		return label;
	}
}
