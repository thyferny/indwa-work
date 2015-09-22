/**
 * 
 */
package com.alpine.miner.impls.web.resource;

import java.util.LinkedList;
import java.util.List;

/**
 * @author sam_zang
 *
 */
public class FlowInfoTree extends FlowInfo {
	
	FlowInfoTree() {
		this.children = new LinkedList<Child>();
	}
	
	/**
	 * @param g
	 */
	public FlowInfoTree(String g) {
		this.children = new LinkedList<Child>();
		this.setId(g);
		this.setKey(g);
		this.setResourceType(ResourceType.Group);
	}

	class Child {
		/**
		 * @param id
		 */
		public Child(String id) {
			this._reference = id;
		}

		private Child() {
			
		}
		private String _reference;
	}

	private List<Child> children;

	/**
	 * @return the id
	 */

	/**
	 * @param children the children to set
	 */
	public void addChildren(List<FlowInfo> children) {
		if(children!=null){
			for (FlowInfo info : children) {
				info.setChild();
				Child c = new Child(info.getKey());
				this.children.add(c);
			}
		}
	}

}
