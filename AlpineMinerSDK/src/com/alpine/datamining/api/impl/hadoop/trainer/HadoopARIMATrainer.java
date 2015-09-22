/**
 * 

 * ClassName HadoopARIMATrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-11-5
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */

package com.alpine.datamining.api.impl.hadoop.trainer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelTrainer;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopARIMARunner;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.timeseries.KalmanForeRet;
import com.alpine.datamining.operator.timeseries.MakeARIMARet;

/**
 * @author Peter
 * 
 * 
 */

public class HadoopARIMATrainer extends AbstractHadoopModelTrainer{

	private static Logger itsLogger = Logger
			.getLogger(HadoopARIMATrainer.class);
	
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {

		hadoopRunner=new HadoopARIMARunner(getContext(),getName());
		
		try {
			return (Model) hadoopRunner.runAlgorithm(source);
		} catch (Exception e) {
			throw new AnalysisException(e.getLocalizedMessage());
		}
	}
	
    public static KalmanForeRet KalmanForecast( int nAhead, MakeARIMARet mod)
    {//uncorrelated to database,direct use
        double[]a = null;
        double[][] P = null;
        a = mod.getA();
        P = mod.getP();
        KalmanForeRet x =  KalmanFore(nAhead, mod.getZ(), a, P,
                   mod.getT(), mod.getV(), mod.getH(), true);//, PACKAGE = "stats")
        return x;
    }
    private static KalmanForeRet KalmanFore(int nAhead, double[] z, double[] a,
			double[][] p2, double[][] t, double[][] v2, double h, boolean fast) {//uncorrelated to database,direct use
    	double[]p0 = new double[p2.length * p2[0].length];
    	for(int i = 0; i < p2.length; i++){
    		for(int j = 0; j < p2[0].length; j++){
    			p0[i + j*p2.length]= p2[i][j];
    		}
    	}
    	double[]sT = new double[t.length * t[0].length];
    	for(int i = 0; i < t.length; i++){
    		for(int j = 0; j < t[0].length; j++){
    			sT[i + j*t.length]= t[i][j];
    		}
    	}
    	double[]sV = new double[v2.length * v2[0].length];
    	for(int i = 0; i < v2.length; i++){
    		for(int j = 0; j < v2[0].length; j++){
    			sV[i + j*v2.length]= v2[i][j];
    		}
    	}
    	double[] aCopy = new double[a.length];
    	System.arraycopy(a,0,aCopy,0,a.length);
    	return KalmanFore(nAhead, z, aCopy, p0, sT, sV,
 	            h,  fast);
	}

	static KalmanForeRet
	KalmanFore(int nahead, double[] sZ, double[] sa0, double[] sP0, double[] sT, double[] sV,
	           double sh, boolean fast)
	{
		KalmanForeRet res = new KalmanForeRet(); double[]forecasts; double[]se;
	    int  n = (nahead), p = (sa0.length);
	    double []Z = (sZ); double[]a = (sa0);double[]P = (sP0); double[]T = (sT);
	        double[]V = (sV); double h = (sh);
	    int i, j, k, l;
	    double fc, tmp; double[]mm; double[]anew;double[]Pnew;
	 
	    anew = new double[p];
	    Pnew = new double[p*p];
	    mm = new double[p*p];
	    forecasts = new double[n];
	    se = new double[n];
	    if (!(fast)){
	        a=(sa0);
	        P=(sP0);
	    }
	    for (l = 0; l < n; l++) {
	        fc = 0.0;
	        for (i = 0; i < p; i++) {
	            tmp = 0.0;
	            for (k = 0; k < p; k++)
	                tmp += T[i + p * k] * a[k];
	            anew[i] = tmp;
	            fc += tmp * Z[i];
	        }
	        for (i = 0; i < p; i++)
	            a[i] = anew[i];
	        (forecasts)[l] = fc;
	 
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                tmp = 0.0;
	                for (k = 0; k < p; k++)
	                    tmp += T[i + p * k] * P[k + p * j];
	                mm[i + p * j] = tmp;
	            }
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                tmp = V[i + p * j];
	                for (k = 0; k < p; k++)
	                    tmp += mm[i + p * k] * T[j + p * k];
	                Pnew[i + p * j] = tmp;
	            }
	        tmp = h;
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                P[i + j * p] = Pnew[i + j * p];
	                tmp += Z[i] * Z[j] * P[i + j * p];
	            }
	        (se)[l] = tmp;
	    }
	    res.setForecasts(forecasts);
	    res.setSe(se);
	    return res;
	}

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.ARIMA_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.ARIMA_TRAIN_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

}
