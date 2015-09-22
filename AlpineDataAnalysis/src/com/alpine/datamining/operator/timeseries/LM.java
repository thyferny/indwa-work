package com.alpine.datamining.operator.timeseries;

public class LM {

	static public LMRet dqrls(double[][] x, int n, int p, double[][] y, int ny,
			double tol, double[][] b, double[][] rsd, double[][] qty,
			int k/* 1 */, int[]jpvt/* 1..p */, double[] qraux, double[][] work) {

		int i, info = 0, j, jj, kk;

		int[]pk = new int[1];
		 dqrdc2(x,n,n,p,tol,pk,qraux,jpvt,work);
		 k = pk[0];

		if (k > 0) {
			for (jj = 0; jj < ny; jj++) {
				 dqrsl(x,n,n,k,qraux,y[jj],rsd[jj],qty[jj],
						 b[jj],rsd[jj],rsd[jj],1110,info);
			}
		} else {
			for (i = 0; i < n; i++) {
				for (jj = 0; jj < ny; jj++) {
					rsd[jj][i] = y[jj][i];
				}
			}
		}
		kk = k + 1;
		for (j = kk - 1; j <p; j++) {
			for (jj = 0; j < ny; j++) {
				b[jj][j] = 0.0;
			}
		}
		LMRet ret = new LMRet();
		ret.setCoefficient(b[0]);
		ret.setSes(rsd[0]);

		double rss = 0.0;
		for(i = 0 ; i < rsd.length; i++){
			for(j = 0; j < rsd[i].length; j++){
				rss += rsd[i][j]*rsd[i][j];
			}
		}
		int rdf = n - p;
		double resvar = rss/rdf;
		double R = Math.abs(1.0/x[0][0]);
		ret.setSes(new double[]{Math.sqrt(resvar)*R}); 
		if (Double.isNaN(ret.getSes()[0])){
			ret.getSes()[0] = 1.0;
		}
		return ret;
	}
	
	static double dnrm2(int n, double[] x, int xStart) {
		double sumSquare = 0.0;
		for(int i = 0 ;i < n ; i++){
			sumSquare += x[xStart + i] * x[xStart + i];
		}
		return Math.sqrt(sumSquare);
	}
	static double dsign(double a, double b) {
		double abs = Math.abs(a);
		if (b >= 0) {
			return abs;
		} else {
			return -abs;
		}
	}

	static double ddot(int n, double[] x, int xStart, double[] y, int yStart) {
		double result = 0.0;
		for (int i = 0; i < n; i++) {
			result += x[xStart + i] * y[yStart + i];
		}
		return result;
	}

	static void daxpy(int n, double a, double[] x, int xStart, double[] y, int yStart) {
		for (int i = 0; i < n; i++) {
			y[yStart + i] = x[xStart + i] * a + y[yStart + i];
		}
	}
	static void dcopy(int n, double[] x, int xStart, double[] y, int yStart) {
		for (int i = 0; i < n; i++) {
			y[yStart + i] = x[xStart + i];
		}
	}

	static void dscal(int n,double scale, double[]x, int xStart){
		for(int i = 0; i < n; i++){
			x[xStart + i] = x[xStart + i] * scale;
		}
	}

	public static void dqrdc2(double[][] x, int ldx, int n, int p, double tol, int[] k,
			double[] qraux, int[] jpvt, double[][] work) {
		int i, j, l, lp1, lup;// ,k;
		double  tt, ttt;
		double  nrmxl, t;
		for (j = 0; j < p; j++) {
			qraux[j] = dnrm2(n, x[j], 0);
			work[0][j] = qraux[j];
			work[1][j] = qraux[j];
			if (work[1][j] == 0.0)
				work[1][j] = 1.0;
		}
		lup = Math.min(n, p);
		k[0] = p + 1;
		for (l = 0; l < lup; l++) {
			if (!(l +1 >= k[0] || qraux[l] >= work[1][l] * tol)) {
				lp1 = l + 1;
				for (i = 0; i < n; i++) {
					t = x[l][i];
					for (j = lp1; j < p; j++) {
						x[j - 1][i] = x[j][i];
					}
					x[p - 1][i] = t;
				}
				i = jpvt[l];
				t = qraux[l];
				tt = work[0][l];
				ttt = work[1][l];
				for (j = lp1; j < p; j++) {
					jpvt[j - 1] = jpvt[j];
					qraux[j - 1] = qraux[j];
					work[0][j - 1] = work[0][j];
					work[1][j - 1] = work[1][j];
				}
				jpvt[p - 1] = i;
				qraux[p - 1] = t;
				work[0][p - 1] = tt;
				work[1][p - 1] = ttt;
				k[0] = k[0] - 1;
			}
			if (!(l == n - 1)) {
				nrmxl = dnrm2(n - l, x[l], l);
				if (!(nrmxl == 0.0)) {
					if (x[l][l] != 0.0)
						nrmxl = dsign(nrmxl, x[l][l]);
					dscal(n-l, 1.0/nrmxl, x[l], l);
					x[l][l] = 1.0 + x[l][l];
					lp1 = l + 1;
					if (!(p < lp1 + 1)) {
						for (j = lp1 ; j < p; j++) {
							t = -ddot(n - l, x[l], l, x[j], l) / x[l][l];
							daxpy(n - l, t, x[l], l, x[j], l);
							if (!(qraux[j] == 0.0)) {
								tt = 1.0 - (Math.abs(x[j][l]) / qraux[j])
										* (Math.abs(x[j][l]) / qraux[j]);
								tt = Math.max(tt, 0.0);
								t = tt;
								if (!(Math.abs(t) < 1e-6)) {
									qraux[j] = qraux[j] * Math.sqrt(t);
								} else {
									qraux[j] = dnrm2(n - l - 1, x[j], l+1);
									work[0][j] = qraux[j];
								}
							}
						}
					}
					qraux[l] = x[l][l];
					x[l][l] = -nrmxl;
				}
			}
		}
		k[0] = Math.min(k[0] - 1, n);
		return;
	}
	public static void dqrsl(double[][] x, int ldx, int n, int k, double[] qraux,
			double[] y, double[] qy, double[] qty, double[] b, double[] rsd,
			double[] xb, int job, int info) {
		int i, j, jj, ju, kp1;
		double  t, temp;
		boolean cb, cqy, cqty, cr, cxb;
		info = 0;
		cqy = (job / 10000 != 0);
		cqty = ((job % 10000) != 0);
		cb = ((job % 1000) / 100 != 0);
		cr = ((job % 100) / 10 != 0);
		cxb = ((job % 10) != 0);
		ju = (Math.min(k, n - 1));
		if (!(ju != 0)) {
			if (cqy) qy[0] = y[0];
			if (cqty) qty[0] = y[0];
			if (cxb) xb[0] = y[0];
			if (!(!cb)){
				if (!(x[0][0] != 0.0)){
					info = 1;
				}else{
					b[0] = y[0]/x[0][0];
				}
			}
			if (cr) rsd[0] = 0.0;
		} else {
			if (cqy)
				dcopy(n, y, 0, qy, 0);
			if (cqty)
				dcopy(n, y, 0, qty, 0);
			if (!(!cqy)) {
				for (jj = 0; jj < ju; jj++) {
					j = ju - jj - 1;
					if (!(qraux[j] == 0.0)) {
						temp = x[j][j];
						x[j][j] = qraux[j];
						t = -ddot(n - j , x[j], j, qy, j) / x[j][j];
						daxpy(n - j , t, x[j], j, qy, j);
						x[j][j] = temp;
					}
				}
			}
			if (!(!cqty)) {
				for (j = 0; j < ju; j++) {
					if (!(qraux[j] == 0.0)) {
						temp = x[j][j];
						x[j][j] = qraux[j];
						t = -ddot(n - j, x[j], j, qty, j) / x[j][j];
						daxpy(n - j , t, x[j], j, qty, j);
						x[j][j] = temp;
					}
				}
			}
			if (cb)
				dcopy(k, qty, 0, b, 0);
			kp1 = k + 1;
			if (cxb)
				dcopy(k, qty, 0, xb, 0);
			if (cr && k < n)
				dcopy(n - k, qty, kp1 - 1, rsd, kp1 - 1);
			if (!(!cxb || kp1> n)) {
				for (i = kp1 - 1; i < n; i++) {
					xb[i] = 0.0;
				}
			}
			if (!(!cr)) {
				for (i = 0; i < k; i++) {
					rsd[i] = 0.0;
				}
			}
		}
		if (!(!cb)) {
			for (jj = 0; jj < k; jj++) {
				j = k - jj - 1;
				if (!(x[j][j] != 0.0)) {
					info = j;
					break;
				}
				b[j] = b[j] / x[j][j];
				if (!(j == 0)) {
					t = -b[j];
					daxpy(j - 1 + 1, t, x[j], 0, b, 0);
				}
			}
		}
		if (!(!cr && !cxb)) {

			for (jj = 0; jj < ju; jj++) {
				j = ju - jj - 1;
				if (!(qraux[j] == 0.0)) {
					temp = x[j][j];
					x[j][j] = qraux[j];
					if (!(!cr)) {
						t = -ddot(n - j, x[j], j, rsd, j) / x[j][j];
						daxpy(n - j, t, x[j], j, rsd, j);
					}
					if (!(!cxb)) {
						t = -ddot(n - j, x[j], j, xb, j) / x[j][j];
						daxpy(n - j, t, x[j], j, xb, j);
					}
					x[j][j] = temp;
				}
			}
		}
		return;
	}
}
