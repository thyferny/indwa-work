/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowCategoryForm
 * Feb 23, 2012
 */
package com.alpine.miner.impls.categorymanager.model;

/**
 * @author Gary
 * Transform client and server data.
 */
public class FlowCategoryForm {

	FlowCategory categoryInfo;
	
	FlowBasisInfo[] operateFlowInfoArray;

	public FlowCategory getCategoryInfo() {
		return categoryInfo;
	}

	public void setCategoryInfo(FlowCategory categoryInfo) {
		this.categoryInfo = categoryInfo;
	}

	public FlowBasisInfo[] getOperateFlowInfoArray() {
		return operateFlowInfoArray;
	}

	public void setOperateFlowInfoArray(FlowBasisInfo[] operateFlowInfoArray) {
		this.operateFlowInfoArray = operateFlowInfoArray;
	}

}
