/**
* ClassName StatisticsChiSquareTest.java
*
* Version information: 1.00
*
* Data: 27 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.utility;

/**
 * @author Shawn
 *
 */
public class StatisticsChiSquareTest {

	public static double chiSquareTest(double gammaA,double gammaX)
	{
		int gammaN;
		double gammaP,gammaQ,gammaD,gammaS,gammaS1;
		double gammaP0,gammaQ0,gammaP1,gammaQ1,gammaQQ;
		if((gammaA<=0.0)||(gammaX<=0.0))
		{
			
			return (-1.0);
		}
		if(gammaX+1.0==1.0) return (0.0);
		if(gammaX>1.0e+35) return (1.0);
		gammaQ=Math.log(gammaX);  gammaQ=gammaA*gammaQ;  gammaQQ=Math.exp(gammaQ);
		if(gammaX<1.0+gammaA)
		{
			gammaP=gammaA; gammaD=1.0/gammaA; gammaS=gammaD;
			for ( gammaN=1; gammaN<=100; gammaN++)
			{
				gammaP=1.0+gammaP; gammaD=gammaD*gammaX/gammaP; gammaS=gammaS+gammaD;
				if(Math.abs(gammaD)<Math.abs(gammaS)*1.0e-07)
				{
					gammaS=gammaS*Math.exp(-gammaX)*gammaQQ/gamma1(gammaA);
					return(gammaS);
				}
			}
		}
		else
		{
			gammaS=1.0/gammaX; gammaP0=0.0; gammaP1=1.0; gammaQ0=1.0; gammaQ1=gammaX;
			for(gammaN=1; gammaN<=100; gammaN++)
			{
				gammaP0=gammaP1+(gammaN-gammaA)*gammaP0; gammaQ0=gammaQ1+(gammaN-gammaA)*gammaQ0;
				gammaP=gammaX*gammaP0+gammaN*gammaP1; gammaQ=gammaX*gammaQ0+gammaN*gammaQ1;
				if(Math.abs(gammaQ)+1.0!=1.0)
				{
					gammaS1=gammaP/gammaQ; gammaP1=gammaP; gammaQ1=gammaQ;
					if(Math.abs((gammaS1-gammaS)/gammaS1)<1.0e-07)
					{
						gammaS=gammaS1*Math.exp(-gammaX)*gammaQQ/gamma1(gammaA);
						return(1.0-gammaS);
					}
					gammaS=gammaS1;
				}
				gammaP1=gammaP;gammaQ1=gammaQ;
			}
		}
	
		gammaS=1.0-gammaS*Math.exp(-gammaX)*gammaQQ/gamma1(gammaA);
		return (gammaS);
	}
	
	
		
	static double gamma1(double gammaXX)
	{
	    double gammaCoefConst[]=new double[7];
	    double gammaStep=2.50662827465;
	    double gammaHALF=0.5;
	    double gammaONE=1;
	    double gammaFPF=5.5;
	    double gammaSER,gammaTemp,gamma1x,gamma1y;
	    int j;

	    gammaCoefConst[1]=76.18009173;
	    gammaCoefConst[2]=-86.50532033;
	    gammaCoefConst[3]=24.01409822;
	    gammaCoefConst[4]=-1.231739516;
	    gammaCoefConst[5]=0.00120858003;
	    gammaCoefConst[6]=-0.00000536382;

	    gamma1x=gammaXX-gammaONE;
	    gammaTemp=gamma1x+gammaFPF;
	    gammaTemp=(gamma1x+gammaHALF)*Math.log(gammaTemp)-gammaTemp;
	    gammaSER=gammaONE;
	    for(j=1;j<=6;j++)
	    {
			gamma1x=gamma1x+gammaONE;
			gammaSER=gammaSER+gammaCoefConst[j]/gamma1x;
	    }
	    gamma1y=gammaTemp+Math.log(gammaStep*gammaSER);

	    return Math.exp(gamma1y);
	}
	
}
