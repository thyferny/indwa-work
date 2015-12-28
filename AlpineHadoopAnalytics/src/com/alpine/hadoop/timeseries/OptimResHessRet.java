
package com.alpine.hadoop.timeseries;

import java.io.Serializable;

import com.alpine.hadoop.util.Matrix;



public class OptimResHessRet  implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private OptimRet res;
	private Matrix hess;
	public OptimRet getRes() {
		return res;
	}
	public void setRes(OptimRet res) {
		this.res = res;
	}
	public Matrix getHess() {
		return hess;
	}
	public void setHess(Matrix hess) {
		this.hess = hess;
	}

}
