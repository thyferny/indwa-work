package com.alpine.utility.tools;

import java.text.DecimalFormat;

import com.alpine.utility.profile.ProfileUtility;

public class AlpineMath {

	static DecimalFormat decFormat; 
	static int DECIMAL_PRECISION=Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_DIGIT_PRECISION));
	static int bestDifferent=1000;
	static {
		decFormat = new DecimalFormat();
		decFormat.setGroupingUsed(false);
		decFormat.setMinimumFractionDigits(DECIMAL_PRECISION);
		decFormat.setMaximumFractionDigits(DECIMAL_PRECISION);
	}
	
	static public void setDecimalPrecision(int numDigits)
	{
		decFormat.setMinimumFractionDigits(numDigits);
		decFormat.setMaximumFractionDigits(numDigits);
	}
	
	public static String doubleExpression(double d){
		if(Double.isInfinite(d)||Double.isNaN(d)){
			return String.valueOf(d);
		}
		return decFormat.format(d);
	}
	
	public static long adjustUnits(double min,double max){
		Double different=max-min;
		if(different<bestDifferent){
			return 1;
		}
		long n=10;
		while(true){
			if(different/n>bestDifferent){
				n=n*10;
			}else{
				break;
			}
			
		}
		return n;

	}

    public static long adjustUnitsOverAMillion(double min, double max)
    {
        Double different=max-min;
        if(different<1000000){
            return 1;
        }
        long n=10;
        while(true){
            if(different/n>1000000){
                n=n*10;
            }else{
                break;
            }

        }
        return n;
    }
}
