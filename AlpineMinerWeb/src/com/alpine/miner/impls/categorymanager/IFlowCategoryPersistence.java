/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ICategoryPersistence
 * Feb 17, 2012
 */
package com.alpine.miner.impls.categorymanager;

import java.util.List;
import java.util.Map;

import com.alpine.miner.impls.categorymanager.exception.FlowCategoryException;
import com.alpine.miner.impls.categorymanager.impl.FlowCategoryPersistenceFileImpl;
import com.alpine.miner.impls.categorymanager.model.FlowBasisInfo;
import com.alpine.miner.impls.categorymanager.model.FlowCategory;
import com.alpine.miner.impls.categorymanager.model.FlowDisplayModel;

/**
 * Personal Flow category management
 * @author Gary
 *
 */
public interface IFlowCategoryPersistence {
	
	IFlowCategoryPersistence INSTANCE = new FlowCategoryPersistenceFileImpl();

	/**
	 * To create new category in personal scope
	 * @param c
	 * @throws FlowCategoryException
	 */
	void createCategory(FlowCategory c)throws FlowCategoryException;
	
	/**
	 * To update category information
	 * @param c
	 * @throws FlowCategoryException
	 */
	void updateCategory(FlowCategory c)throws FlowCategoryException;
	
	/**
	 * To remove a category(so remove all of flows under the category) in personal scope
	 * @param c
	 * @throws FlowCategoryException
	 */
	void removeCategory(FlowCategory c)throws FlowCategoryException;
	
	/**
	 * move some flows into a category
	 * @param target
	 * @param flowmodelArray
	 * @return successful flow mapping information
	 * @throws FlowCategoryException
	 */
	Map<String, FlowDisplayModel> moveFlow(FlowCategory target, FlowDisplayModel[] flowmodelArray)throws FlowCategoryException;
	
	/**
	 * build root category tree Information
	 * @param userSign
	 * @return
	 * @throws FlowCategoryException
	 */
	FlowCategory buildRootCategory(String userSign)throws FlowCategoryException;
	
	/**
	 * get child category by parent key
	 * @param parentKey
	 * @return
	 * @throws FlowCategoryException
	 */
	List<FlowCategory> getChildrenCategory(String parentKey)throws FlowCategoryException;
	
	/**
	 * get all of flows by user name
	 * @param userSign
	 * @return
	 * @throws FlowCategoryException
	 */
	List<FlowBasisInfo> getAllFlowInfo(String userSign)throws FlowCategoryException;

	/**
	 * @param userName
	 * @param flowBasisKey
	 * @return
	 * @throws Exception
	 */
	String getFlowAbsolutelyPath(String flowBasisKey)
			throws Exception;
}