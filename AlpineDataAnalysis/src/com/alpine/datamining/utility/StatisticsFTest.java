/**
* ClassName StatisticsFInspection.java
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
public class StatisticsFTest {

	public static double fTest(double F, double n1, double n2) {
		
		double x;
		double p = 0;

		if (n1 <= 0 || n2 <= 0)
			p = -1;

		if (F > 0) {
			x = F / (F + n2 / n1);
			p = betaInc(x, n1 / 2, n2 / 2);
		}
		return (1 - p);
	}

	static	double betainv(double betaP, double betaA, double betaB) {

		int countMaxLimit = 100;
		int countMax = 0;
		double betaX, betaXNew, betaY, betaH, pBeta, logKernA, logKernB;
		double crit = 1.818989403545857e-012; 
		if (betaP == 0)
			betaX = 0;
		if (betaP == 1)
			betaX = 1;

		betaX = betaA / (betaA + betaB);
		if (betaX < crit)
			betaX = crit;
		if (betaX > 1 - crit)
			betaX = 1 - crit;
		betaH = 1;

		while ((betaH > (crit * Math.abs(betaX))) && (betaH > crit)
				&& (countMax < countMaxLimit)) {
			countMax = countMax + 1;

			if (betaX > 1)
				betaP = 1;
			pBeta = betaInc(betaX, betaA, betaB);
			if (pBeta > 1)
				pBeta = 1;

			logKernA = (betaA - 1) * Math.log(betaX);
			if ((betaA == 1) && (betaX == 0))
				logKernA = 0;
			logKernB = (betaB - 1) * Math.log(1 - betaX);
			if ((betaB == 1) && (betaX == 1))
				logKernB = 0;
			betaY = Math.exp(logKernA + logKernB - Math.log(beta(betaA, betaB)));

			betaH = (pBeta - betaP) / betaY;
			betaXNew = betaX - betaH;

			if (betaXNew <= 0)
				betaXNew = betaX / 10;
			if (betaXNew >= 1)
				betaXNew = 1 - (1 - betaX) / 10;

			betaX = betaXNew;
		}
		return betaX;
	}

	static	double beta(double z, double w) {
		int betaM, betaN, betaK, betaL, betaJ;
		double betaH[] = new double[10];
		double betaBB[] = new double[10];
		double betaHH, betaT1, betaS1, betaEP, betaS, betaX, betaT2, betaG = 0;
		double betaEPS = 1e-10;

		betaM = 1;
		betaN = 1;
		betaHH = 1;
		betaH[0] = betaHH;
		betaT1 = 0;
		betaS1 = betaT1;
		betaBB[0] = betaS1;
		betaEP = 1.0 + betaEPS;

		while ((betaEP >= betaEPS) && (betaM <= 9)) {
			betaS = 0.0;
			for (betaK = 0; betaK <= betaN - 1; betaK++) {
				betaX = (betaK + 0.5) * betaHH;
				// s=s+f(x,z,w);
				betaS = betaS + Math.pow(betaX, z - 1) * Math.pow(1 - betaX, w - 1);
			}
			betaT2 = (betaT1 + betaHH * betaS) / 2.0;
			betaM = betaM + 1;
			betaH[betaM - 1] = betaH[betaM - 2] / 2.0;
			betaG = betaT2;
			betaL = 0;
			for (betaJ = 2; betaJ <= betaM; betaJ++) {
				betaS = betaG - betaBB[betaJ - 2];
				if (betaL == 0)
					if (Math.abs(betaS) + 1.0 == 1.0)
						betaL = 1;
					else
						betaG = (betaH[betaM - 1] - betaH[betaJ - 2]) / betaS;
			}
			betaBB[betaM - 1] = betaG;
			if (betaL != 0)
				betaBB[betaM - 1] = 1.0e+35;
			betaG = betaBB[betaM - 1];
			for (betaJ = betaM; betaJ >= 2; betaJ--)
				betaG = betaBB[betaJ - 2] - betaH[betaJ - 2] / betaG;
			betaEP = Math.abs(betaG - betaS1);
			betaS1 = betaG;
			betaT1 = betaT2;
			betaHH = betaHH / 2.0;
			betaN = betaN + betaN;
		}
		return betaG;
	}

	static double betaInc(double betaX, double betaA, double betaB) {

		double betaY, betaBT, betaAAA;

		if (betaX == 0 || betaX == 1)
			betaBT = 0;
		else {
			betaAAA = gamma(betaA + betaB) - gamma(betaA) - gamma(betaB);
			betaBT = Math.exp(betaAAA + betaA * Math.log(betaX) + betaB * Math.log(1 - betaX));
		}
		if (betaX < (betaA + 1) / (betaA + betaB + 2))
			betaY = betaBT * betaCf(betaA, betaB, betaX) / betaA;
		else
			betaY = 1 - betaBT * betaCf(betaB, betaA, 1 - betaX) / betaB;

		return betaY;
	}

	static double betaCf(double betaA, double betaB, double betaX) {
		int betaCount, betaCountMax = 100;
		double betaEPS = 0.0000001;
		double betaAM = 1;
		double betaBM = 1;
		double betaAZ = 1;
		double betaQAB;
		double betaQAP;
		double betaQAM;
		double betaBZ, betaEM, betaTEM, betaD, betaAP, betaBP, betaAAP, betaBPP, betaAOLD;

		betaQAB = betaA + betaB;
		betaQAP = betaA + 1;
		betaQAM = betaA - 1;
		betaBZ = 1 - betaQAB * betaX / betaQAP;

		for (betaCount = 1; betaCount <= betaCountMax; betaCount++) {
			betaEM = betaCount;
			betaTEM = betaEM + betaEM;
			betaD = betaEM * (betaB - betaCount) * betaX / ((betaQAM + betaTEM) * (betaA + betaTEM));
			betaAP = betaAZ + betaD * betaAM;
			betaBP = betaBZ + betaD * betaBM;
			betaD = -(betaA + betaEM) * (betaQAB + betaEM) * betaX / ((betaA + betaTEM) * (betaQAP + betaTEM));
			betaAAP = betaAP + betaD * betaAZ;
			betaBPP = betaBP + betaD * betaBZ;
			betaAOLD = betaAZ;
			betaAM = betaAP / betaBPP;
			betaBM = betaBP / betaBPP;
			betaAZ = betaAAP / betaBPP;
			betaBZ = 1;
			if (Math.abs(betaAZ - betaAOLD) < betaEPS * Math.abs(betaAZ))
				return betaAZ;
		}
		return betaAZ;
	}

	static double gamma(double gammaxx) {
		double gammaCoefConst[] = new double[7];
		double gammaStep = 2.50662827465;
		double gammaHALF = 0.5;
		double gammaONE = 1;
		double gammaFPF = 5.5;
		double gammaSER, gammaTemp, gammaX, gammaY;
		int gammaJ;

		gammaCoefConst[1] = 76.18009173;
		gammaCoefConst[2] = -86.50532033;
		gammaCoefConst[3] = 24.01409822;
		gammaCoefConst[4] = -1.231739516;
		gammaCoefConst[5] = 0.00120858003;
		gammaCoefConst[6] = -0.00000536382;

		gammaX = gammaxx - gammaONE;
		gammaTemp = gammaX + gammaFPF;
		gammaTemp = (gammaX + gammaHALF) * Math.log(gammaTemp) - gammaTemp;
		gammaSER = gammaONE;
		for (gammaJ = 1; gammaJ <= 6; gammaJ++) {
			gammaX = gammaX + gammaONE;
			gammaSER = gammaSER + gammaCoefConst[gammaJ] / gammaX;
		}
		gammaY = gammaTemp + Math.log(gammaStep * gammaSER);

		return gammaY;
	}
}
