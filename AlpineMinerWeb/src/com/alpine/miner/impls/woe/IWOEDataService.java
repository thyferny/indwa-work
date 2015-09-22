/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IWOEDataService
 * Jan 9, 2012
 */
package com.alpine.miner.impls.woe;

import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.woe.impl.WOEDataServiceImpl;
import com.alpine.miner.workflow.operator.Operator;

/**
 * @author Gary
 *
 */
public interface IWOEDataService {
	
	public IWOEDataService INSTANCE = new WOEDataServiceImpl();

	/**
	 * auto group and auto calculate
	 * @param operator
	 * @param woeModel
	 * @param user
	 * @param resourceType
	 * @return
	 * @throws AnalysisException
	 */
	List<WoeCalculateElement> autoCalculate(Operator operator, WoeCalculateElement[] elements, String user, ResourceType resourceType) throws AnalysisException;
	
	/**
	 * manual calculate single Element
	 * @param operator
	 * @param elements
	 * @param user
	 * @param resourceType
	 * @return
	 * @throws AnalysisException
	 */
	WoeCalculateElement calculate(Operator operator, WoeCalculateElement[] elements, String user, ResourceType resourceType) throws AnalysisException;
}
