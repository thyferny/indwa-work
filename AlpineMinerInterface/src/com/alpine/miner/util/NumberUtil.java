/**
 * ClassName NumberUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.util;

public class NumberUtil {
	public static boolean isInteger(String str,
			int min,boolean isMinOpenInterval,
			int max,boolean isMaxOpenInterval) {
		try {
			int integer=Integer.parseInt(str);
			if(isMinOpenInterval&&isMaxOpenInterval){
				return integer<max&&integer>min;
			}else if(isMinOpenInterval&&!isMaxOpenInterval){
				return integer<=max&&integer>min;
			}else if(!isMinOpenInterval&&isMaxOpenInterval){
				return integer<max&&integer>=min;
			}else if(!isMinOpenInterval&&!isMaxOpenInterval){
				return integer<=max&&integer>=min;
			}else{
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isInteger(String str) {
		try {
			Integer.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static boolean isNumber(String str) {
		try {
			Double.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isNumber(String str,
			double min,boolean isMinOpenInterval,
			double max,boolean isMaxOpenInterval) {
		try {
			double number=Double.parseDouble(str);
			if(isMinOpenInterval&&isMaxOpenInterval){
				return number<max&&number>min;
			}else if(isMinOpenInterval&&!isMaxOpenInterval){
				return number<=max&&number>min;
			}else if(!isMinOpenInterval&&isMaxOpenInterval){
				return number<max&&number>=min;
			}else if(!isMinOpenInterval&&!isMaxOpenInterval){
				return number<=max&&number>=min;
			}else{
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static boolean isFloat(String str) {
		try {
			if(isInteger(str)){
				return false;
			}
			Double.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
