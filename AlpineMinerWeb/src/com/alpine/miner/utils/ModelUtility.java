/**
 * ClassName :ModelUtility.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.utils;

import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author zhaoyong
 *
 */
public class ModelUtility {
	public static String getAlorithmModel(EngineModel engineModel) {
		if (engineModel != null&&engineModel.getModelType()!=null) {
			String algorithmName = engineModel.getModelType();

			if (algorithmName.contains(".")) {
				algorithmName = algorithmName.substring(
						algorithmName.lastIndexOf(".") + 1,
						algorithmName.length());
			}
			return algorithmName;
		} else {
			return null;
		}
	}
	
 

}
