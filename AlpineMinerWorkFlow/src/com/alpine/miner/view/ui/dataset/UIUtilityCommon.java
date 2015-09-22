package com.alpine.miner.view.ui.dataset;
import java.util.Random;

import org.eclipse.swt.graphics.Color;

public class UIUtilityCommon {
	private static Color[] colorConstants = {ColorConstants.red,ColorConstants.green,ColorConstants.blue,ColorConstants.orange,ColorConstants.cyan,ColorConstants.pink,ColorConstants.gray,ColorConstants.magenta,ColorConstants.black};
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
		Color cc = new Color(null,r,g,b);
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
}
