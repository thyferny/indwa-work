/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowCategoryManagerTest.java
 * 2012-2-20
 */
package com.alpine.miner.impls.categorymanager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.alpine.miner.impls.categorymanager.exception.FlowCategoryException;
import com.alpine.miner.impls.categorymanager.model.FlowCategory;
import com.alpine.miner.impls.categorymanager.model.FlowDisplayModel;
import com.alpine.miner.impls.flow.AbstractFlowTest;

/**
 * @author Gary
 */
public class FlowCategoryManagerTest extends AbstractFlowTest {

	private FlowCategory rootCateogry = new FlowCategory("junit", "junit");
	private String userSign = "junit";
	private IFlowCategoryPersistence store = IFlowCategoryPersistence.INSTANCE;
	
	@Test
	public void testCreateCategory() throws Exception{
		FlowCategory c1 = new FlowCategory("math");
		c1.setParentKey(rootCateogry.getKey());
		FlowCategory c1Duplicate = new FlowCategory("math");
		c1Duplicate.setParentKey(rootCateogry.getKey());
		FlowCategory c2 = new FlowCategory("finance");
		c2.setParentKey(rootCateogry.getKey());
		FlowCategory c3 = new FlowCategory("stock");
		c3.setParentKey(rootCateogry.getKey());
		store.createCategory(c1);
		store.createCategory(c2);
		store.createCategory(c3);
		try{
			store.createCategory(c1Duplicate);//will be failed.
			throw new RuntimeException();
		}catch(FlowCategoryException e){
			//if run here then allright.
		}
	}
	
	@Test
	public void testGetChildren() throws Exception{
		FlowCategory root = store.buildRootCategory(userSign);
		List<FlowDisplayModel> subItems = root.getSubItems();
		for(FlowDisplayModel f : subItems){
			System.out.println("key = " + f.getKey() + "	name = " + f.getName());
		}
		Assert.assertTrue(root.getSubItems().size() > 0);
	}

	@Test
	public void testRename() throws Exception{
		FlowCategory root = store.buildRootCategory(userSign);
		if(!(root.getSubItems().get(0) instanceof FlowCategory)){
			System.out.println("rename failed, because first item is not a category");
			return;
		}
		FlowCategory flowCategory = (FlowCategory) root.getSubItems().get(0);
		String oldName = flowCategory.getName();
		flowCategory.setName("new_" + oldName);
		store.updateCategory(flowCategory);
		root = store.buildRootCategory(userSign);
		
		Assert.assertTrue(oldName != ((FlowDisplayModel) root.getSubItems().get(0)).getName());
	}

	@Test
	public void testRemove() throws Exception{
		FlowCategory root = store.buildRootCategory(userSign);
		List<FlowDisplayModel> subItems = root.getSubItems();
		for(FlowDisplayModel item : subItems){
			if(!(item instanceof FlowCategory)){
				continue;
			}
			store.removeCategory((FlowCategory) item);
		}
		Assert.assertTrue(store.buildRootCategory(userSign).getSubItems().size() == 0);
	}
}
