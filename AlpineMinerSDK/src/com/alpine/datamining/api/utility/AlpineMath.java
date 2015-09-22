package com.alpine.datamining.api.utility;

import java.text.DecimalFormat;

import com.alpine.datamining.api.resources.AlpineMinerConfig;

public class AlpineMath {
	private static final double MIN_POW = 0.00000001;
	private static final double MAX_POW = 9999999; 
	private static final double MIN_POW_NAGTIVE = -0.00000001;
	private static final double MAX_POW_NAGTIVE = -9999999; 
	static int pow =0;
	static String powPlusminus = "+";
	static String numberMinus="";
	
	static DecimalFormat decFormat; 
	
	static {
		decFormat = new DecimalFormat();
		decFormat.setGroupingUsed(false);
		decFormat.setMinimumFractionDigits(AlpineMinerConfig.DECIMAL_PRECISION_DIGITS);
		decFormat.setMaximumFractionDigits(AlpineMinerConfig.DECIMAL_PRECISION_DIGITS);
	}
	static DecimalFormat dataFormat = new DecimalFormat("########0.00000000");
	
	
	public static String powExpression(double value){
		if(Double.isInfinite(value)||Double.isNaN(value)){
			return String.valueOf(value);
		}
	//	Float.parseFloat(String.valueOf(value)).
		else if(value==0){
			return "0";
		}
		else if((value>=MIN_POW&&value<=MAX_POW)||(value>=MAX_POW_NAGTIVE&&value<=MIN_POW_NAGTIVE)){
			
			return dataFormat.format(value);
		}
		else{//  value!=0  && -0.0000001<value<-0.0000001   || >9999999  || <-999999
		
			if(value<0){
				numberMinus = "-";
			}else{
				numberMinus = "";
			}
			pow =0;
			double d_pow = calculatePow(Math.abs(value));
			DecimalFormat df= new DecimalFormat("#.000");
			DecimalFormat dfe= new DecimalFormat("00");
			if(pow<0){
				powPlusminus = "-";
			}else{
				powPlusminus = "+";
			}
			if(d_pow<0){
				return numberMinus+df.format(d_pow)+"e"+powPlusminus+dfe.format(Math.abs(pow));
			}else{
				return numberMinus+df.format(d_pow)+"e"+powPlusminus+dfe.format(Math.abs(pow));
			}
		}
		
	}
	
	private static double calculatePow(double d){
		if(d>=10){
			d = d/10;
			pow++;
			d=calculatePow(d);
		}else if(d<1 && d>0){
			d = d*10;
			pow--;
			d=calculatePow(d);
		}
		return d;
	}

}
