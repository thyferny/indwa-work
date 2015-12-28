
package com.alpine.hadoop.timeseries;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;

import com.alpine.hadoop.TimeSeriesKeySet;
import com.alpine.hadoop.util.Matrix;
 



public class TimeSeriesReducer extends
		Reducer<LongSort, Text, Text, Text> implements
		Tool {
	
	private Configuration conf;
 	HashMap<String, Double> groupRecord = new HashMap<String,Double>();
	private int p = 0, q = 0, d = 0;
	private double[] bestPhi;
	private double[] bestTheta;
	private double sigma2; 
	private double likelihood;
	private int[] arma;
	private int ncxreg  = 0 ;
	long[] lastIdData=null;
	long[] idData=null;
	double[] lastData=null;
	double[] analyticData=null;
	private int lengthOfWindow;
	private int lengthOfLastData=50;
	
	
	@Override
	public void reduce(LongSort key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {
	 try{
		int maxGetSize=lengthOfWindow>lengthOfLastData ? lengthOfWindow:lengthOfLastData;
		int index=0;
		double[] tmpAnalyticData= new double[lengthOfWindow];
		double[] tmpLastData= new double[lengthOfLastData];
		long[] tempIdData=new long[lengthOfWindow];
		long[] tempLastIdData= new long[lengthOfLastData];
		  lastIdData=null;
		  idData=null;
		  lastData=null;
		  analyticData=null;
		String groupValue=null;
		for(Text value: values)
		{
			if(index>=maxGetSize)
			{
				break;
			}
			else{
				String[] idValuePair=value.toString().split(",");
				double parsedValue=Double.parseDouble(idValuePair[1].trim());
				long parsedId=Long.parseLong(idValuePair[0].trim());
			 
					groupValue=idValuePair[2];
			 
				if(index<lengthOfWindow){
					tmpAnalyticData[lengthOfWindow-index-1]=parsedValue;
					tempIdData[lengthOfWindow-index-1]=parsedId;
				}
				if(index<lengthOfLastData){
					tmpLastData[lengthOfLastData-index-1]=parsedValue;
					tempLastIdData[lengthOfLastData-index-1]=parsedId;
				}
				index++;
			}
		}
		if(index<lengthOfLastData)
		{
			lastData=new double[index];
			lastIdData=new long[index];
			System.arraycopy(tmpLastData, lengthOfLastData-index, lastData, 0, index);
			System.arraycopy(tempLastIdData, lengthOfLastData-index, lastIdData, 0, index);
		}else{
			lastData=tmpLastData;
			lastIdData=tempLastIdData;
		}
		if(index<lengthOfWindow)
		{
			analyticData=new double[index];
			idData=new long[index];
			System.arraycopy(tmpAnalyticData, lengthOfWindow-index, analyticData, 0, index);
			System.arraycopy(tempIdData, lengthOfWindow-index, idData, 0, index);
		}else{
			analyticData=tmpAnalyticData;
			idData=tempIdData;
		}
		bestPhi = new double[p];
		bestTheta = new double[q];
		ARIMARet res = arima();
		if(res==null){
			context.write(new Text(groupValue), new Text ("small:datasize")); 
			return;
		}
		for(int i = 0; i < p; i++){
			bestPhi[i] = res.getCoef()[i];
		}
		for(int i = 0; i < q; i++){
			bestTheta[i] = res.getCoef()[p+i];
		}
		double intercept = Double.NaN; 
		if(ncxreg > 0)
		{
			intercept = res.getCoef()[res.getCoef().length - 1];
		}
		Map<Long, Integer> intervalMap = new HashMap<Long, Integer>();
		for(int j = 0; j < idData.length-1; j++){
			if (intervalMap.containsKey(idData[j+1]-idData[j])){
				intervalMap.put(idData[j+1]-idData[j], intervalMap.get(idData[j+1]-idData[j]) + 1);
			}else{
				intervalMap.put(idData[j+1]-idData[j], 1);
			}
		}

		Iterator<Entry<Long, Integer>> it = intervalMap.entrySet().iterator();  
		long maxInterval=0;
		int max = 0;
		while (it.hasNext()) {
		        Map.Entry<Long, Integer> entry = it.next();
		        Long tempInterval = entry.getKey();
		        if(tempInterval==0)
		        {
		        	context.write(new Text(groupValue),new Text("error: duplicate value"));
					return ;
		        }
		        Integer value = entry.getValue();
		        if (value > max){
		        	max = value;
		        	maxInterval = tempInterval;
		        }
		} 
		
		if(intervalMap.containsKey(0))
		{
			context.write(new Text(groupValue),new Text("error: duplicate value"));
			return ;
		}
		context.write(new Text(groupValue), new Text ("p:"+p)); 
		context.write(new Text(groupValue), new Text ("d:"+d));
		context.write(new Text(groupValue), new Text ("q:"+q));
		context.write(new Text(groupValue), new Text ("bestPhi:"+Arrays.toString(bestPhi)));
		context.write(new Text(groupValue), new Text ("bestTheta:"+Arrays.toString(bestTheta)));
		context.write(new Text(groupValue), new Text ("intercept:"+intercept));
		context.write(new Text(groupValue), new Text ("VarCoef:"+Arrays.toString(res.getVarCoef())));
		context.write(new Text(groupValue), new Text ("sigma2:"+sigma2));
		context.write(new Text(groupValue), new Text ("ncxreg:"+ncxreg));
		context.write(new Text(groupValue), new Text ("likelihood:"+likelihood));
		context.write(new Text(groupValue), new Text ("data:"+Arrays.toString(lastData)));//TODO  here ,just last 50 data or so , not total , you can verify it
		context.write(new Text(groupValue), new Text ("Residuals:"+Arrays.toString(res.getResiduals())));
		context.write(new Text(groupValue), new Text ("arma:"+Arrays.toString(arma)));
		MakeARIMARet model=res.getModel();
		context.write(new Text(groupValue), new Text ("MakeARIMARet.A:"+Arrays.toString(model.getA())));
		context.write(new Text(groupValue), new Text ("MakeARIMARet.Coefs:"+Arrays.toString(model.getCoefs())));
		context.write(new Text(groupValue), new Text ("MakeARIMARet.Delta:"+Arrays.toString(model.getDelta())));
		context.write(new Text(groupValue), new Text ("MakeARIMARet.Phi:"+Arrays.toString(model.getPhi())));
		context.write(new Text(groupValue), new Text ("MakeARIMARet.Z:"+Arrays.toString(model.getZ())));
		String tmp="";
		for(int i=0;i<model.getP().length;i++){
			if(i==0){
				tmp=Arrays.toString(model.getP()[i]);
			}
			else{
				tmp=tmp+";"+Arrays.toString(model.getP()[i]);
			}
		}
		context.write(new Text(groupValue), new Text ("MakeARIMARet.P:"+tmp));
		for(int i=0;i<model.getPn().length;i++){
			if(i==0){
				tmp=Arrays.toString(model.getPn()[i]);
			}
			else{
				tmp=tmp+";"+Arrays.toString(model.getPn()[i]);
			}
		}
		context.write(new Text(groupValue), new Text ("MakeARIMARet.Pn:"+tmp));
		for(int i=0;i<model.getT().length;i++){
			if(i==0){
				tmp=Arrays.toString(model.getT()[i]);
			}
			else{
				tmp=tmp+";"+Arrays.toString(model.getT()[i]);
			}
		}
		context.write(new Text(groupValue), new Text ("MakeARIMARet.T:"+tmp));
		for(int i=0;i<model.getV().length;i++){
			if(i==0){
				tmp=Arrays.toString(model.getV()[i]);
			}
			else{
				tmp=tmp+";"+Arrays.toString(model.getV()[i]);
			}
		}
		context.write(new Text(groupValue), new Text ("MakeARIMARet.V:"+tmp));
		context.write(new Text(groupValue), new Text ("MakeARIMARet.H:"+model.getH()));
		context.write(new Text(groupValue), new Text ("idData:"+Arrays.toString(lastIdData)));
		context.write(new Text(groupValue), new Text ("interval:"+maxInterval));
	 }catch(Exception e)
	 {
		 System.out.println(e);
	 }
 	}

	
	private ARIMARet arima() 
	{
		double[] x = analyticData;
		int[]order = new int[]{p,d,q};
		int[]seasonalOrder = new int[]{0,0,0};
		int seasonalPeriod = 1;
		int n = x.length;
		arma = new int[]{order[0],order[2],seasonalOrder[0],seasonalOrder[2],seasonalPeriod,
				order[1],seasonalOrder[1]};
		int narma = 0;
		for(int i = 0; i < 4; i++){
			narma += arma[i];
		}
		double[]delta = new double[1];
		delta[0] = 1;
		double[] tmp = new double[]{1,-1};
		for(int i = 0; i < order[1]; i++){
			delta = TSconv(delta, tmp);
		}
		tmp = new double[2+seasonalPeriod - 1];
		tmp[0] = 1;
		tmp[tmp.length - 1]= -1;
		for(int i =0; i < seasonalPeriod - 1; i++){
			tmp[i] = 0;
		}
		for(int i = 0; i < seasonalOrder[1]; i++){
			delta = TSconv(delta, tmp);
		}
		double[] deltaOld = delta;
		delta = new double[deltaOld.length - 1];
		for(int i = 0; i < delta.length; i++){
			delta[i] = -deltaOld[i + 1];
		}
	    int nUsed = n;

		nUsed = n - delta.length;
		int nd = order[1] + seasonalOrder[1];
		ncxreg = 0;
		boolean includeMean = true;//false;
		double [] xreg = null;
		if (includeMean && (nd == 0)) {
		    xreg = new double[n];
		    for(int i = 0; i < n ; i++){
		    	xreg[i] = 1;
		    }
		    ncxreg = ncxreg + 1;
		}

		int ncond = order[1] + seasonalOrder[1] * seasonalPeriod;
	    int ncond1 = order[0] + seasonalPeriod * seasonalOrder[0];

	    Integer nCond = null;

	    if (nCond != null) 
	    	ncond  = ncond + Math.max(nCond, ncond1);
	    else 
	    	ncond = ncond + ncond1;
	    double[] fixed = new double[narma + ncxreg];
	    for(int i = 0; i < fixed.length; i++){
	    	fixed[i] = Double.NaN;
	    }
	    boolean [] mask = new boolean[fixed.length];
	    for(int i = 0; i < mask.length; i++){
	    	if (Double.isNaN(fixed[i])){
	    		mask[i] = true;
	    	}else{
	    		mask[i] = false;
	    	}
	    }
	    boolean noOptim = false;

	    double[] init0 = new double[narma];
	    double[] parscale = new double[narma];
	    for(int i = 0; i < parscale.length; i++){
	    	parscale[i] = 1;
	    }
	    
	    if (ncxreg != 0) {
	        double[][] lmxreg = new double[1][];
	        lmxreg[0] = new double[xreg.length];
	        for(int i = 0; i < lmxreg[0].length; i++){
	        	lmxreg[0][i] = xreg[i];
	        }
	        int lmp = lmxreg.length;
	        double [][]lmy = new double[1][];
	        lmy[0] = new double[x.length];
	        System.arraycopy(x, 0, lmy[0],0,lmy[0].length);
	        int lmny = lmy.length;

	        double [][] work = new double[2][lmp];

	        double[][]b = new double[lmny][lmp];
	        double[][]residuals = new double[lmy.length][lmy[0].length];
	        double[][]effects = new double[lmy.length][lmy[0].length];
	        double[]qraux = new double[lmp];
	        for(int i = 0; i < lmy.length; i++){
	        	System.arraycopy(lmy[i], 0, residuals[i],0,residuals[i].length);
	        	System.arraycopy(lmy[i], 0, effects[i],0,effects[i].length);
	        }
	        int []jpvt = new int[lmp];
	        for(int i = 0; i < jpvt.length; i++){
	        	jpvt[i] = i+1;
	        }
			LMRet lmRet = LM.dqrls(lmxreg , n, 1, lmy, 1, 1e-07, b ,residuals, effects, 1, jpvt, qraux, work);       

			//	        n.used <- sum(!is.na(resid(fit))) - length(Delta)
			double coefFit = lmRet.getCoefficient()[0];
			double[] init0New = new double[init0.length + 1];
			System.arraycopy(init0, 0, init0New, 0, init0.length);
			
			init0New[init0.length] = coefFit;
			init0= init0New;
			double ses = lmRet.getSes()[0];
			double [] parscaleNew = new double[parscale.length + 1];
			System.arraycopy(parscale, 0, parscaleNew, 0, parscale.length);
			parscaleNew[parscale.length] = 10*ses;
			parscale = parscaleNew;
	    }
	    
	    double[] init = init0;
	    double [] coef = fixed;
	    XReg xReg = new XReg();
	    xReg.setNcxreg(ncxreg);
	    xReg.setNarma(narma);
	    xReg.setXreg(xreg);
	    if (ncond >= n){
	    	
	    	//TODO
//			throw new WrongUsedException(this,AlpineAnalysisErrorName.DATASET_TOO_SMALL);
	    }
	    OptimResHessRet res = Optimization.optim(init, 
				arma,
				ncond,
	    		true,
	    		x,
	    		xReg,
	    		parscale); 
	    
        coef = res.getRes().getPar();
        TransParsRet trarma = ARIMA_transPars(coef, arma, false);
        double kappa= 1e6;
		MakeARIMARet mod = makeARIMA(coef,trarma.getsPhi(), trarma.getsTheta(), delta, kappa);
        if(ncxreg > 0) {
        	for (int i = 0; i < x.length; i++){
        		x[i] = x[i] - xreg[i] * coef[narma];
        	}
        }
		double [] P = new double[mod.getP().length * mod.getP()[0].length];
		for(int i = 0; i < mod.getP().length;  i++){
			for (int j = 0; j < mod.getP()[0].length; j++){
				P[i*mod.getP()[0].length + j] = mod.getP()[i][j];
			}
		}
		double [] Pn = new double[mod.getPn().length * mod.getPn()[0].length];
		for(int i = 0; i < mod.getPn().length;  i++){
			for (int j = 0; j < mod.getPn()[0].length; j++){
				Pn[i*mod.getPn()[0].length + j] = mod.getPn()[i][j];
			}
		}

		ARIMA_Like(x, mod.getPhi(), mod.getTheta(), mod.getDelta(),mod.getA(),P,Pn,0,true);
				
		CSSRet val = 
		ARIMA_CSS(x, arma,trarma,
				ncond, true);
		sigma2 = val.getValue();
		Matrix var = null;
		if(noOptim){
//			var = 0;
		}else{
//			try {
				try {
					var = res.getHess().times(nUsed).SVDInverse();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					return null;
//					e.printStackTrace();
				}
//			} catch (OperatorException e) {
//				logger.error("svd exception:"+e.getLocalizedMessage());  //TODO
//			}
		}
		double[] varCoef = new double[coef.length];
		String varString = new String();
		if(var != null){
			for(int i = 0; i < var.getColumnDimension(); i++){
					varCoef[i] = Math.sqrt(var.get(i,i));
					varString+=","+varCoef[i];
			}
		}
		double value  = 2 * nUsed * res.getRes().getValue() + nUsed + nUsed * Math.log(2 * Math.PI);
	    double aic = Double.NaN;

	    ARIMARet aRIMARes = new ARIMARet();
	    aRIMARes.setCoef(coef);
	    aRIMARes.setSigma2(sigma2);
	    aRIMARes.setLoglik(-0.5 * value);
	    likelihood = aRIMARes.getLoglik() ;
	    aRIMARes.setAic(aic);
	    aRIMARes.setArma(arma);
	    aRIMARes.setX(x) ;
	    aRIMARes.setnCond(ncond);
	    aRIMARes.setModel(mod);
	    aRIMARes.setVarCoef(varCoef);
	    aRIMARes.setResiduals(val.getsResid());
	    return aRIMARes;
	}
	
	
	double[] TSconv(double[] a, double[] b)
	{   
	    int i, j, na, nb, nab;
	    double[] ab;
	    double []ra; double[]rb; double[]rab;

	    na = a.length;
	    nb = b.length;
	    nab = na + nb - 1;
	    ab = new double[nab];
	    ra = (a); rb = (b); rab = (ab);
	    for (i = 0; i < nab; i++) rab[i] = 0.0;
	    for (i = 0; i < na; i++)
	        for (j = 0; j < nb; j++)
	            rab[i + j] += ra[i] * rb[j];
	    return (ab);
	}
	
	static TransParsRet ARIMA_transPars(double[] sin, int[] sarma, boolean strans)
	{
		TransParsRet res = new TransParsRet();
		int []arma = (sarma); boolean trans = (strans);
	    int mp = arma[0], mq = arma[1], msp = arma[2], msq = arma[3],
	        ns = arma[4], i, j, p = mp + ns * msp, q = mq + ns * msq, v;
	    double []in = (sin); double[]params = (sin); double[]phi;double []theta;
	    res.setsPhi(new double [ p]);
	    res.setsTheta(new double[q]);
	    phi = res.getsPhi();
	    theta = res.getsTheta();
	    if (trans) {
	        int n = mp + mq + msp + msq;

	        params = new double[n];
	        for (i = 0; i < n; i++) params[i] = in[i];
	        if (mp > 0) partrans(mp, in, params);
	        v = mp + mq;
	        double[] in_v = new double[in.length - v];//?????
	        double[] params_v = new double[params.length - v];
	        if (msp > 0) partrans(msp, in_v, params_v);
	    }
	    if (ns > 0) {
	        
	        for (i = 0; i < mp; i++) phi[i] = params[i];
	        for (i = 0; i < mq; i++) theta[i] = params[i + mp];
	        for (i = mp; i < p; i++) phi[i] = 0.0;
	        for (i = mq; i < q; i++) theta[i] = 0.0;
	        for (j = 0; j < msp; j++) {
	            phi[(j + 1) * ns - 1] += params[j + mp + mq];
	            for (i = 0; i < mp; i++)
	                phi[(j + 1) * ns + i] -= params[i] * params[j + mp + mq];
	        }
	        for (j = 0; j < msq; j++) {
	            theta[(j + 1) * ns - 1] += params[j + mp + mq + msp];
	            for (i = 0; i < mq; i++)
	                theta[(j + 1) * ns + i] += params[i + mp] *
	                    params[j + mp + mq + msp];
	        }
	    } else {
	        for (i = 0; i < mp; i++) phi[i] = params[i];
	        for (i = 0; i < mq; i++) theta[i] = params[i + mp];
	    }
	    return res;
	}

	MakeARIMARet makeARIMA (double[] coefs, double[] phi, double[] theta, double[]Delta, double kappa)
	{
	    int p = (phi.length); int q = (theta.length);
	    int r = Math.max(p, q + 1); int d = (Delta.length);
	    int rd = r + d;
	    double [] Z = new double[r+Delta.length];
	    Z[0] = 1.0;
	    for(int i = 0; i < r-1; i++){
	    	Z[i + 1] = 0.0;
	    }
	    for(int i = 0; i < Delta.length; i++){
	    	Z[i + r] = Delta[i];
	    }

	    double[][]T = new double[rd][rd];
	    if(p > 0){
	    	for(int i = 0; i < p; i++){
	    		T[i][0] = phi[i];
	    }
	    }
	    if(r > 1) {
	    	for(int ind = 1; ind < r; ind++){
	    		T[ind-1][ind]= 1;
	    	}
	    }
	    if(d > 0) {
	        T[r] = Z;
	        if(d > 1) {
	            for(int ind = r+1; ind < r+d; ind++){
	            	T[ind][ind-1]= 1;
	            }
	        }
	    }
	    if(q < r - 1){
	    	double []thetaBack = theta;
	    	theta = new double [thetaBack.length + r - 1 -q];
	    	for(int i = 0; i < thetaBack.length; i++){
	    		theta[i] = thetaBack[i];
	    	}
	    	for(int i = 0; i < r-1-q; i++){
	    		theta[i+thetaBack.length] = 0;
	    	}
	    }
	    double []R = new double[1 + theta.length + d];
	    R[0] = 1;
	    System.arraycopy(theta, 0, R, 1, theta.length);
	    for(int i = 0; i < d; i++){
	    	int begin = 1;
	    	if (theta != null && theta.length != 0){
	    		begin += theta.length;
	    	}
	    	R[i+begin] = 0;
	    }
	    double[][]V = new double[R.length][R.length];
	    for(int i = 0; i < R.length; i++){
	    	for(int j = 0; j < R.length; j++){
	    		V[i][j] = R[i]*R[j];
	    	}
	    }
	    double h = 0.;
	    double[] a = new double[rd];
	    double[][]Pn = new double[rd][rd];
	    double[][]P = new double[rd][rd];
	    if(r > 1){ 
	    	double[][] q0= 	getQ0(phi, theta);
	    	for(int i = 0; i < r; i++){
	    		for(int j = 0; j < r; j++){
	    			Pn[i][j] = q0[i][j];
	    		}
	    	}
	    }
	    else{
	    	if(p > 0)
	    	{
	    		Pn[0][0] = 1/(1 - phi[0]*phi[0]);
	    	}else{
	    		Pn[0][0] = 1;
	    	}
	    }
	    if(d > 0) {
	    	for(int i = r; i < d + r; i++){
	    		for(int j = r; j < d + r; j++){
	    			Pn[i][j]= kappa;
	    		}
	    	}
	    }
	    MakeARIMARet res = new MakeARIMARet();
	    res.setPhi(phi);
	    res.setTheta(theta);
	    res.setDelta(Delta);
	    res.setZ(Z);
	    res.setA(a);
	    res.setP(P) ;
	    res.setT(T);
	    res.setV(V);
	    res.setH(h);
	    res.setPn(Pn);
	    res.setCoefs(coefs);
	    return res;

	    }

	
	
	LikeRet
	ARIMA_Like(double [] sy, double [] sPhi, double [] sTheta, double [] sDelta,
			double [] sa, double [] sP, double [] sPn, int sUP, boolean giveResid)
	{
		LikeRet res = new LikeRet();
	    double[] nres, sResid = null ;
	    int  n = (sy.length), rd = (sa.length), p = (sPhi.length),
	        q = (sTheta.length), d = (sDelta.length), r = rd - d;
	    double []y = (sy), a = (sa), P = (sP), Pnew = (sPn);
	    double [] phi = (sPhi), theta = (sTheta), delta = (sDelta);
	    double sumlog = 0.0, ssq = 0, resid, gain, tmp, vi;double []anew, mm = null,
	        M;         
	    int i, j, k, l, nu = 0; 
	    boolean useResid = (giveResid);
	    double []rsResid = null ;
	                
	    anew = new double[rd];
	    M = new double[rd];
	    if (d > 0) mm = new double[rd * rd];;
	                
	    if (useResid) { 
	        sResid = new double[n];
	        rsResid = (sResid);
	    }                   
	                        
	    for (l = 0; l < n; l++) {
	        for (i = 0; i < r; i++) {
	            tmp = (i < r - 1) ? a[i + 1] : 0.0;
	            if (i < p) tmp += phi[i] * a[0];
	            anew[i] = tmp; 
	        }               
	        if (d > 0) {
	            for (i = r + 1; i < rd; i++) anew[i] = a[i - 1];
	            tmp = a[0];
	            for (i = 0; i < d; i++) tmp += delta[i] * a[r + i];
	            anew[r] = tmp;
	        }       
	        if (l > (sUP)) {
	            if (d == 0) {
	                for (i = 0; i < r; i++) {
	                    vi = 0.0;
	                    if (i == 0) vi = 1.0; else if (i - 1 < q) vi = theta[i - 1];
	                    for (j = 0; j < r; j++) {
	                        tmp = 0.0;
	                        if (j == 0) tmp = vi; else if (j - 1 < q) tmp = vi * theta[j - 1];
	                        if (i < p && j < p) tmp += phi[i] * phi[j] * P[0];
	                        if (i < r - 1 && j < r - 1) tmp += P[i + 1 + r * (j + 1)];
	                        if (i < p && j < r - 1) tmp += phi[i] * P[j + 1];
	                        if (j < p && i < r - 1) tmp += phi[j] * P[i + 1];
	                        Pnew[i + r * j] = tmp;
	                    }
	                }
	            } else {
	                
	                for (i = 0; i < r; i++)
	                    for (j = 0; j < rd; j++) {
	                        tmp = 0.0;
	                        if (i < p) tmp += phi[i] * P[rd * j];
	                        if (i < r - 1) tmp += P[i + 1 + rd * j];
	                        mm[i + rd * j] = tmp;
	                    }
	                for (j = 0; j < rd; j++) {
	                    tmp = P[rd * j];
	                    for (k = 0; k < d; k++)
	                        tmp += delta[k] * P[r + k + rd * j];
	                    mm[r + rd * j] = tmp;
	                }
	                for (i = 1; i < d; i++)
	                    for (j = 0; j < rd; j++)
	                        mm[r + i + rd * j] = P[r + i - 1 + rd * j];

	                
	                for (i = 0; i < r; i++)
	                    for (j = 0; j < rd; j++) {
	                        tmp = 0.0;
	                        if (i < p) tmp += phi[i] * mm[j];
	                        if (i < r - 1) tmp += mm[rd * (i + 1) + j];
	                        Pnew[j + rd * i] = tmp;
	                    }
	                for (j = 0; j < rd; j++) {
	                    tmp = mm[j];
	                    for (k = 0; k < d; k++)
	                        tmp += delta[k] * mm[rd * (r + k) + j];
	                    Pnew[rd * r + j] = tmp;
	                }
	                for (i = 1; i < d; i++)
	                    for (j = 0; j < rd; j++)
	                        Pnew[rd * (r + i) + j] = mm[rd * (r + i - 1) + j];
	                
	                for (i = 0; i <= q; i++) {
	                    vi = (i == 0) ? 1. : theta[i - 1];
	                    for (j = 0; j <= q; j++)
	                        Pnew[i + rd * j] += vi * ((j == 0) ? 1. : theta[j - 1]);
	                }
	            }
	        }
	        if (!Double.isNaN(y[l])) {
	            resid = y[l] - anew[0];
	            for (i = 0; i < d; i++)
	                resid -= delta[i] * anew[r + i];

	            for (i = 0; i < rd; i++) {
	                tmp = Pnew[i];
	                for (j = 0; j < d; j++)
	                    tmp += Pnew[i + (r + j) * rd] * delta[j];
	                M[i] = tmp;
	            }

	            gain = M[0];
	            for (j = 0; j < d; j++) gain += delta[j] * M[r + j];
	            if(gain < 1e4) {
	                nu++;
	                ssq += resid * resid / gain;
	                sumlog += Math.log(gain);
	            }
	            if (useResid) rsResid[l] = resid / Math.sqrt(gain);
	            for (i = 0; i < rd; i++)
	                a[i] = anew[i] + M[i] * resid / gain;
	            for (i = 0; i < rd; i++)
	                for (j = 0; j < rd; j++)
	                    P[i + j * rd] = Pnew[i + j * rd] - M[i] * M[j] / gain;
	        } else {
	            for (i = 0; i < rd; i++) a[i] = anew[i];
	            for (i = 0; i < rd * rd; i++) P[i] = Pnew[i];
	            if (useResid) rsResid[l] = Double.NaN;
	        }
	    }

	    if (useResid) {
	        nres = new double[3];
	        (nres)[0] = ssq;
	        (nres)[1] = sumlog;
	        (nres)[2] = (double) nu;
	        res.setNres(nres);
	        res.setsResid(sResid);
	        return res;
	    } else {
	        nres = new double[3];
	        (nres)[0] = ssq;
	        (nres)[1] = sumlog;
	        (nres)[2] = (double) nu;
	        res.setNres(nres);
	        return res;
	    }
	}
	
	
	
	static CSSRet
	ARIMA_CSS(double[]  sy, int[]  sarma, TransParsRet trarma,
			int sncond, boolean  giveResid)
	{
		double[]   sResid;
	    double ssq = 0.0; double []y = (sy);double tmp;
	    double []phi = (trarma.getsPhi());double []theta = (trarma.getsTheta()); double[]w; double[]resid;
	    int n = (sy.length);int[] arma = (sarma); int p = (trarma.getsPhi().length);
	        int q = (trarma.getsTheta().length); int ncond = (sncond);
	    int l, i, j, ns, nu = 0;
	    boolean useResid = (giveResid);

	    w = new double[n];
	    for (l = 0; l < n; l++) w[l] = y[l];
	    for (i = 0; i < arma[5]; i++)
	        for (l = n - 1; l > 0; l--) w[l] -= w[l - 1];
	    ns = arma[4];
	    for (i = 0; i < arma[6]; i++)
	        for (l = n - 1; l >= ns; l--) w[l] -= w[l - ns];
	    sResid = new double[n];
	    resid = (sResid);
	    if (useResid) for (l = 0; l < ncond && l < n; l++) resid[l] = 0;

	    for (l = ncond; l < n; l++) {
	        tmp = w[l];
	        for (j = 0; j < p; j++) tmp -= phi[j] * w[l - j - 1];
	        for (j = 0; j < Math.min(l - ncond, q); j++)
	            tmp -= theta[j] * resid[l - j - 1];
	        resid[l] = tmp;
	        if (!Double.isNaN(tmp)) {
	            nu++;
	            ssq += tmp * tmp;
	        }
	    }
	    CSSRet res = new CSSRet();
	    res.setUseResid(useResid);
	    if (useResid) {
	        res.setValue(ssq/nu);
	        res.setsResid(sResid);
	        return res;
	    } else {
	    	res.setValue(ssq/nu);
	    }
	    return res;
	}
	
	
	static void partrans(int p, double []raw, double []newdata)
	{
	    int j, k;
	    double a;
	    int n = 0;
	    if (raw.length > newdata.length){
	    	n = raw.length;
	    }else{
	    	n = newdata.length;
	    }
	    double work[] = new double[n];

//	    if(p > 100) error(_("can only transform 100 pars in arima0"));
	    
	    for(j = 0; j < p; j++) work[j] = newdata[j] = Math.tanh(raw[j]);
	    
	    for(j = 1; j < p; j++) {
	        a = newdata[j];
	        for(k = 0; k < j; k++)
	            work[k] -= a * newdata[j - k - 1];
	        for(k = 0; k < j; k++) newdata[k] = work[k];
	    }
	}

	
	double[][] getQ0(double[] sPhi, double[] sTheta)
	{
	    double[][] res;
	    int  p = (sPhi.length), q = (sTheta.length);
	    double []V; double []phi = (sPhi); double[]theta = (sTheta);

	    double []P; double[]xnext; double[] xrow; double[]rbar; double[]thetab;
	    int r = Math.max(p, q + 1);
	    int np = r * (r + 1) / 2, nrbar = np * (np - 1) / 2;
	    int indi, indj, indn;
	    double phii, phij, ynext, bi, vi, vj;
	    int   i, j, ithisr, ind, npr, ind1, ind2, npr1, im, jm;

	    //	    if(r > 350) error(_("maximum supported lag is 350"));
	    thetab = new double[np];
	    xnext = new double[np];
	    xrow = new double[np];
	    rbar = new double[nrbar];
	    V = new double[np];

	    for (ind = 0, j = 0; j < r; j++) {
		vj = 0.0;
		if (j == 0) vj = 1.0; else if (j - 1 < q) vj = theta[j - 1];
		for (i = j; i < r; i++) {
		    vi = 0.0;
		    if (i == 0) vi = 1.0; else if (i - 1 < q) vi = theta[i - 1];
		    V[ind++] = vi * vj;
		}
	    }

	    res = new double[r][r];
	    P = new double[r*r];
	    if (r == 1) {
		P[0] = 1.0 / (1.0 - phi[0] * phi[0]);
		res[0][0] = P[0];
		return res;
	    }
	    if (p > 0) {
	
		for (i = 0; i < nrbar; i++) rbar[i] = 0.0;
		for (i = 0; i < np; i++) {
		    P[i] = 0.0;
		    thetab[i] = 0.0;
		    xnext[i] = 0.0;
		}
		ind = 0;
		ind1 = -1;
		npr = np - r;
		npr1 = npr + 1;
		indj = npr;
		ind2 = npr - 1;
		for (j = 0; j < r; j++) {
		    phij = (j < p) ? phi[j] : 0.0;
		    xnext[indj++] = 0.0;
		    indi = npr1 + j;
		    for (i = j; i < r; i++) {
			ynext = V[ind++];
			phii = (i < p) ? phi[i] : 0.0;
			if (j != r - 1) {
			    xnext[indj] = -phii;
			    if (i != r - 1) {
				xnext[indi] -= phij;
				xnext[++ind1] = -1.0;
			    }
			}
			xnext[npr] = -phii * phij;
			if (++ind2 >= np) ind2 = 0;
			xnext[ind2] += 1.0;
			inclu2(np, xnext, xrow, ynext, P, rbar, thetab);
			xnext[ind2] = 0.0;
			if (i != r - 1) {
			    xnext[indi++] = 0.0;
			    xnext[ind1] = 0.0;
			}
		    }
		}

		ithisr = nrbar - 1;
		im = np - 1;
		for (i = 0; i < np; i++) {
		    bi = thetab[im];
		    for (jm = np - 1, j = 0; j < i; j++)
			bi -= rbar[ithisr--] * P[jm--];
		    P[im--] = bi;
		}

	

		ind = npr;
		for (i = 0; i < r; i++) xnext[i] = P[ind++];
		ind = np - 1;
		ind1 = npr - 1;
		for (i = 0; i < npr; i++) P[ind--] = P[ind1--];
		for (i = 0; i < r; i++) P[i] = xnext[i];
	    } else {

	

		indn = np;
		ind = np;
		for (i = 0; i < r; i++)
		    for (j = 0; j <= i; j++) {
			--ind;
			P[ind] = V[ind];
			if (j != 0) P[ind] += P[--indn];
		    }
	    }
	    
	    for (i = r - 1, ind = np; i > 0; i--)
		for (j = r - 1; j >= i; j--)
		    P[r * i + j] = P[--ind];
	    for (i = 0; i < r - 1; i++)
		for (j = i + 1; j < r; j++)
		    P[i + r * j] = P[j + r * i];
	    for(i = 0; i < r; i++){
	    	for(j = 0; j < r; j++){
	    		res[i][j] = P[i*r+j];
	    	}
	    }
	    return res;
	}

	static void
	inclu2(int np, double []xnext, double []xrow, double ynext,
	       double []d, double []rbar, double []thetab)
	{
	    double cbar, sbar, di, xi, xk, rbthis, dpi;
	    int i, k, ithisr;

	

	    for (i = 0; i < np; i++) xrow[i] = xnext[i];

	    for (ithisr = 0, i = 0; i < np; i++) {
		if (xrow[i] != 0.0) {
		    xi = xrow[i];
		    di = d[i];
		    dpi = di + xi * xi;
		    d[i] = dpi;
		    cbar = di / dpi;
		    sbar = xi / dpi;
		    for (k = i + 1; k < np; k++) {
			xk = xrow[k];
			rbthis = rbar[ithisr];
			xrow[k] = xk - xi * rbthis;
			rbar[ithisr++] = cbar * rbthis + sbar * xk;
		    }
		    xk = ynext;
		    ynext = xk - xi * thetab[i];
		    thetab[i] = cbar * thetab[i] + sbar * xk;
		    if (di == 0.0) return;
		} else
		    ithisr = ithisr + np - i - 1;
	    }
	}

	
	public void setConf(Configuration conf) {
		this.conf = conf;
		p=Integer.parseInt(conf.get(TimeSeriesKeySet.autoregressive));
		q=Integer.parseInt(conf.get(TimeSeriesKeySet.movingaverage));
		d=Integer.parseInt(conf.get(TimeSeriesKeySet.integrated));
		lengthOfWindow=Integer.parseInt(conf.get(TimeSeriesKeySet.lengthOfWindow));
		lengthOfLastData=Integer.parseInt(conf.get(TimeSeriesKeySet.lastData));
//		p=autoregressive;
//		q=movingaverage;
//		d=integrated;
	}

	public Configuration getConf() {
		return conf; 
	}

	public int run(String[] args) throws Exception {
		return 0;
	}
}
