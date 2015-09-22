/**
 * 
 */
package com.alpine.miner.impls.web.resource;

import java.util.LinkedList;
import java.util.List;

import com.alpine.miner.impls.resource.DbConnectionInfo;

/**
 * @author sam_zang
 *
 */
public class DBConnInfoTree extends DbConnectionInfo {
	
	public DBConnInfoTree() {
		super();
		this.children = new LinkedList<Child>();
	}
	
	/**
	 * @param g
	 */
	public DBConnInfoTree(String g) {
		super();
		this.children = new LinkedList<Child>();
		this.setId(g);
		this.setKey(g);
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
	public void addChildren(List<DbConnectionInfo> children) {
		if(children!=null){
			for (DbConnectionInfo info : children) {
				Child c = new Child(info.getKey());
				this.children.add(c);
			}
		}
	}

}
