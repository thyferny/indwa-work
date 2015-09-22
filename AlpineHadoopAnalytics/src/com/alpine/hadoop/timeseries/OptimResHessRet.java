/**
 * 
 * ClassName OptimResHessRet.java
 *
 * Version information: 1.00
 *
 * Date: Nov 5, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.timeseries;

import java.io.Serializable;

import com.alpine.hadoop.util.Matrix;

/**
* @author Shawn 
* 
*/

public class OptimResHessRet  implements Serializable{
	/**
	 * 
	 */
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
