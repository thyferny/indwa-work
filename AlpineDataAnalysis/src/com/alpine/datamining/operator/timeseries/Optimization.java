package com.alpine.datamining.operator.timeseries;

import com.alpine.datamining.tools.matrix.Matrix;

public class Optimization {
	double  big   =       1.0e+35 ;  


	double  E1 = 1.7182818 ; 


	static double  stepredn = 0.2;
	static double acctol	= 0.0001;
	static double reltest	= 10.0;

	static double fminfn(int n, double []p, OptStruct ex, double[] z,XReg xReg)
	{
		double s; double[]x;
		int i;
		double val;
		double [] parscale = ex.getParscale();
		x = new double[n];
		for (i = 0; i < n; i++) {
			if (Double.isInfinite(p[i])){
//			if (Double.isNaN(p[i])){
				return Double.NaN;
			}// error(_("non-finite value supplied by optim"));
			x[i] = p[i] * parscale[i];
		}
		s = fcall(x,ex.getArma(), ex.getNcond(),z,  xReg);
		val = (s)/(ex.getFnscale());
		return val;
	}
	static void fmingr(int n, double []p, double []df, OptStruct ex, double[] z,XReg xReg)
	{
		double s; double[] x;
		int i;
		double val1, val2, eps, epsused, tmp;
		OptStruct OS = (OptStruct) ex;
		x = new double[n];
		for (i = 0; i < n; i++) (x)[i] = p[i] * (OS.getParscale()[i]);
		if(OS.getUsebounds() == 0) {
			for (i = 0; i < n; i++) {
				eps = OS.getNdeps()[i];
				(x)[i] = (p[i] + eps) * (OS.getParscale()[i]);
				s = fcall(x,OS.getArma(), OS.getNcond(),z,  xReg);
				val1 = (s)/(OS.getFnscale());
				(x)[i] = (p[i] - eps) * (OS.getParscale()[i]);
				s = fcall(x,OS.getArma(), OS.getNcond(),z,  xReg);
				val2 = (s)/(OS.getFnscale());
				df[i] = (val1 - val2)/(2 * eps);
				if(Double.isInfinite(df[i])){
					return;
				}
				//error(("non-finite finite-difference value [%d]"), i+1);\
				(x)[i] = p[i] * OS.getParscale()[i];
			}
		} else { 
			for (i = 0; i < n; i++) {
				epsused = eps = OS.getNdeps()[i];
				tmp = p[i] + eps;
				if (tmp > OS.getUpper()[i]) {
					tmp = OS.getUpper()[i];
					epsused = tmp - p[i] ;
				}
				(x)[i] = tmp * (OS.getParscale()[i]);
				s = fcall(x,OS.getArma(), OS.getNcond(),z, xReg);
				val1 = (s)/(OS.getFnscale());
				tmp = p[i] - eps;
				if (tmp < OS.getLower()[i]) {
					tmp = OS.getLower()[i];
					eps = p[i] - tmp;
				}
				(x)[i] = tmp * (OS.getParscale()[i]);
				s = fcall(x,OS.getArma(), OS.getNcond(),z, xReg);
				val2 = (s)/(OS.getFnscale());
				df[i] = (val1 - val2)/(epsused + eps);

				if(Double.isInfinite(df[i])){
					return;
				}
				//error(("non-finite finite-difference value [%d]"), i+1);\
				(x)[i] = p[i] * OS.getParscale()[i];
			}
		}
	}
	static double[][] Lmatrix(int n)
	{   
		int   i; 
		double [][]m;

		m = new double[n][];
		for (i = 0; i < n; i++)
			m[i] = new double[i+1];
		return m;
	}   

	private static double fcall(double[] x, int[] arma, int ncond, double[]z, XReg xReg){
		return ARIMAR.armaCSS (z,x,null,arma, ncond,  xReg);
	}

	static void
	vmmin(int n0, double []b, double[] Fmin,
			int maxit, int trace, int []mask,
			double abstol, double reltol, int nREPORT, OptStruct ex,
			Integer fncount, Integer grcount, Integer fail , double[] z,XReg xReg)
	{
		boolean accpoint, enough;
		double []g, t, X, c; double[][]B;
		int   count, funcount, gradcount;
		double f, gradproj;
		int   i, j, ilast, iter = 0;
		double s, steplength;
		double D1, D2;
		int   n; int[]l;

		if (maxit <= 0) {
			fail = 0;
			Fmin[0] = fminfn(n0, b, ex,z,xReg);
			fncount = grcount = 0;
			return;
		}

		if (nREPORT <= 0){return;}
		//error(_("REPORT must be > 0 (method = \"BFGS\")"));
		l = new int[n0];//, sizeof(int));
		n = 0;
		for (i = 0; i < n0; i++) {if (mask[i] != 0) l[n++] = i;}
		g = new double[n0];
		t = new double[n];
		X = new double[n];
		c = new double[n];
		B = Lmatrix(n);
		f = fminfn(n0, b, ex,z, xReg);
		if (Double.isInfinite(f) || Double.isNaN(f))
		{
			return;
		}//error(_("initial value in 'vmmin' is not finite"));
		if (trace != 0) {}//Rprintf("initial  value %f \n", f);
		Fmin[0] = f;
		funcount = gradcount = 1;
		fmingr(n0, b, g, ex,z,xReg);
		iter++;
		ilast = gradcount;
		int loop = 0;
		do {
			loop++;
//			itsLogger.debug("vmmin:"+loop);
			if (ilast == gradcount) {
				for (i = 0; i < n; i++) {
					for (j = 0; j < i; j++) B[i][j] = 0.0;
					B[i][i] = 1.0;
				}
			}
			for (i = 0; i < n; i++) {
				X[i] = b[l[i]];
				c[i] = g[l[i]];
			}
			gradproj = 0.0;
			for (i = 0; i < n; i++) {
				s = 0.0;
				for (j = 0; j <= i; j++) s -= B[i][j] * g[l[j]];
				for (j = i + 1; j < n; j++) s -= B[j][i] * g[l[j]];
				t[i] = s;
				gradproj += s * g[l[i]];
			}

			if (gradproj < 0.0) {	
				steplength = 1.0;
				accpoint = false;
				do {
					count = 0;
					for (i = 0; i < n; i++) {
						b[l[i]] = X[i] + steplength * t[i];
						if (reltest + X[i] == reltest + b[l[i]]) 
							count++;
					}
					if (count < n) {
						f = fminfn(n0, b, ex,z,xReg);
						funcount++;
						accpoint = !Double.isInfinite(f) && !Double.isNaN(f) &&
						(f <= Fmin[0] + gradproj * steplength * acctol);
						if (!accpoint) {
							steplength *= stepredn;
						}
					}
				} while (!(count == n || accpoint));
				enough = (f > abstol) &&
				Math.abs(f - Fmin[0]) > reltol * (Math.abs(Fmin[0]) + reltol);
				
				if (!enough) {
					count = n;
					Fmin[0] = f;
				}
				if (count < n) {
					Fmin[0] = f;
					fmingr(n0, b, g, ex,z,xReg);
					gradcount++;
					iter++;
					D1 = 0.0;
					for (i = 0; i < n; i++) {
						t[i] = steplength * t[i];
						c[i] = g[l[i]] - c[i];
						D1 += t[i] * c[i];
					}
					if (D1 > 0) {
						D2 = 0.0;
						for (i = 0; i < n; i++) {
							s = 0.0;
							for (j = 0; j <= i; j++)
								s += B[i][j] * c[j];
							for (j = i + 1; j < n; j++)
								s += B[j][i] * c[j];
							X[i] = s;
							D2 += s * c[i];
						}
						D2 = 1.0 + D2 / D1;
						for (i = 0; i < n; i++) {
							for (j = 0; j <= i; j++)
								B[i][j] += (D2 * t[i] * t[j]
								                          - X[i] * t[j] - t[i] * X[j]) / D1;
						}
					} else {	
						ilast = gradcount;
					}
				} else {	
					if (ilast < gradcount) {
						count = 0;
						ilast = gradcount;
					}
				}
			} else {		
				count = 0;
				if (ilast == gradcount) count = n;
				else ilast = gradcount;
				
			}
			if (trace != 0 && (iter % nREPORT == 0)){}
			//			    Rprintf("iter%4d value %f\n", iter, f);
			if (iter >= maxit) break;
			if (gradcount - ilast > 2 * n)
				ilast = gradcount;	
		} while (count != n || ilast != gradcount);
		if (trace != 0) {
			{}//Rprintf("final  value %f \n", *Fmin);
			if (iter < maxit){}// Rprintf("converged\n");
			else {}//Rprintf("stopped after %i iterations\n", iter);
		}
		fail = (iter < maxit) ? 0 : 1;
		fncount = funcount;
		grcount = gradcount;
	}

	
	static double[][]do_optimhess(
			double[] par
			,double fnscale
			,double[] parscale
			,double[]ndeps
			,int[] arma
			,int ncond
			,double[] z
			,XReg xReg)
	{
		OptStruct OS; 
		int npar, i , j;
		double []dpar, df1, df2; double eps;

		OS = new OptStruct();
		OS.setUsebounds(0);
		OS.setArma(arma);
		OS.setNcond(ncond);
		npar = par.length;
		OS.setFnscale(fnscale);
		double [] tmp = parscale;
		if ((tmp.length) != npar)
		{return null;}//error(_("'parscale' is of the wrong length"));
		OS.setParscale(new double[npar]);
		for (i = 0; i < npar; i++) OS.getParscale()[i] = (tmp)[i];
		if ((ndeps.length) != npar){}// error(_("'ndeps' is of the wrong length"));
		OS.setNdeps(new double[npar]);
		for (i = 0; i < npar; i++) OS.getNdeps()[i] = (ndeps)[i];
		double[]ans = new double[npar*npar];
		dpar = new double[npar];
		for (i = 0; i < npar; i++)
			dpar[i] = (par)[i] / (OS.getParscale()[i]);
		df1 = new double[npar];
		df2 = new double[npar];
		for (i = 0; i < npar; i++) {
			eps = OS.getNdeps()[i]/(OS.getParscale()[i]);
			dpar[i] = dpar[i] + eps;
			fmingr(npar, dpar, df1, OS,z,xReg);
			dpar[i] = dpar[i] - 2 * eps;
			fmingr(npar, dpar, df2, OS,z,xReg);
			for (j = 0; j < npar; j++)
				(ans)[i * npar + j] = (OS.getFnscale()) * (df1[j] - df2[j])/
				(2 * eps * (OS.getParscale()[i]) * (OS.getParscale()[j]));
			dpar[i] = dpar[i] + eps;
		}
		double[][] res = new double[npar][npar];
		for(i = 0; i < npar; i++){
			for(j = 0; j < npar; j++){
				res[i][j] = ans[i*npar+j];
			}
		}
		return res;
	}	
	static OptimResHessRet	optim(double[]par, 
			int[] arma,
			int ncond,
			boolean hessian
			,double[] z
			,XReg xReg
			,double[] parscale
	)
	{
		double []lower; //= Double.
		double []upper; //= Double.POSITIVE_INFINITY
		OptimResHessRet optimResHess = new OptimResHessRet();
		int trace = 0; 
		int fnscale = 1;

		double[] ndeps = new double[par.length];
		for(int i = 0; i < par.length; i++){
			ndeps[i] = 1e-3;
		}
		int maxit = 100;
		double abstol = Double.NEGATIVE_INFINITY;///?????
		double reltol=Math.sqrt(Double.MIN_VALUE);//?????
//		double alpha = 1.0;
//		double beta = 0.5;
//		double gamma = 2.0;
//		int REPORT = 10;
//		int type = 1;
//		int lmm = 5;
//		double factr = 1e7;
//		int pgtol = 0;
//		int tmax = 10;
//		double temp = 10.0;

		int npar = par.length;
		lower = new double[npar];
		upper = new double[npar];
		for(int i = 0; i < npar; i++){
			lower[i] = Double.NEGATIVE_INFINITY;
			upper[i] = Double.POSITIVE_INFINITY;
		}
		//  if(!("parscale" %in% names(optim.control)))
		//      optim.control$parscale <- parscale[mask]
		OptimRet res  = do_optim(par, parscale, trace, fnscale, abstol, reltol,  maxit,ndeps, arma, ncond,z,xReg);
		optimResHess.setRes(res);
		Matrix hessMatrix = null;
		if (hessian) {
			double[][]hess = do_optimhess(
					par
					,fnscale
					,parscale
					,ndeps
					,arma
					,ncond
					,z
					,xReg);
			if (hess != null){
				hessMatrix = new Matrix(hess);

				hessMatrix = hessMatrix.plus(hessMatrix.transpose()).times(0.5);
			}
			optimResHess.setHess(hessMatrix);
		}
		return optimResHess;
	}


	
	static OptimRet do_optim(double[] par, double[] parscale,int trace, double fnscale, double abstol, double reltol, int maxit,double[] ndeps, int []arma, int ncond,double[] z, XReg xReg)//SEXP call, SEXP op, SEXP args, SEXP rho)
	{
		int nREPORT = 10;
		int i, npar=0; int[] mask;
		int fncount = 0, grcount = 0 ;
		int ifail = 0;
		double []dpar ; double[] val = new double[1];
		OptStruct OS = new OptStruct();
		OS.setUsebounds(0);
		OS.setArma(arma);
		OS.setNcond(ncond);
		npar = (par.length);
		dpar = new double[npar];
		OS.setFnscale(fnscale);
		double[] tmp = parscale;
//		if ((tmp.length) != npar)
//		{
//			return null;
//		}
		//error(_("'parscale' is of the wrong length"));
		OS.setParscale(new double[npar]);
		for (i = 0; i < npar; i++) OS.getParscale()[i] = (tmp)[i];
		for (i = 0; i < npar; i++){
			dpar[i] = (par)[i] / (OS.getParscale()[i]);
		}
		double[] value = new double[1];
		int[]counts = new int[2];
		int[]conv = new int[1];
//		if ((ndeps.length) != npar)
//		{return null;}//error(_("'ndeps' is of the wrong length"));
		OS.setNdeps(new double[npar]);
		for (i = 0; i < npar; i++) OS.getNdeps()[i] = (ndeps)[i];
		mask = new int[npar];
		for (i = 0; i < npar; i++) mask[i] = 1;
		vmmin(npar, dpar, val,  maxit, trace, mask, abstol,
				reltol, nREPORT, OS, fncount, grcount, ifail,z,xReg);
		for (i = 0; i < npar; i++)
			(par)[i] = dpar[i] * (OS.getParscale()[i]);
		value[0] = val[0] * (OS.getFnscale());
		OptimRet res = new OptimRet();
		(counts)[0] = fncount; (counts)[1] = grcount;
		(conv)[0] = ifail;
		res.setPar(par);
		res.setValue(value[0]);
		res.setCounts(counts);
		res.setConv(conv[0]);
		return res;
	}
}
