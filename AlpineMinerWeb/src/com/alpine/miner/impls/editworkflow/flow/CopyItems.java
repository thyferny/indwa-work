/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * CopyItems.java
 */
package com.alpine.miner.impls.editworkflow.flow;

import java.util.Collection;

import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;

/**
 * @author Gary
 * Aug 6, 2012
 */
public class CopyItems {

	private Collection<UIOperatorModel> copyOperatorSet;
	private Collection<UIOperatorConnectionModel> copyConnectionSet;
	
	public CopyItems(Collection<UIOperatorModel> copyOperatorSet, Collection<UIOperatorConnectionModel> copyConnectionSet){
		this.copyOperatorSet = copyOperatorSet;
		this.copyConnectionSet = copyConnectionSet;
	}
	
	public Collection<UIOperatorModel> getCopyOperatorSet() {
		return copyOperatorSet;
	}
	public Collection<UIOperatorConnectionModel> getCopyConnectionSet() {
		return copyConnectionSet;
	}
}
