
package com.alpine.datamining.operator.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class PowerSet<ItemT> implements Iterable<Collection<ItemT>>, Iterator<Collection<ItemT>>{

    private ArrayList<ItemT> set;
    
	private boolean[] subSetIndicator;
	
    
	public PowerSet(Collection<ItemT> collection) {
		set = new ArrayList<ItemT>(collection);
	}

	public Iterator<Collection<ItemT>> iterator() {
		return new PowerSet<ItemT>(set);
	}
	
	public Collection<ItemT> getComplement(Collection<ItemT> collection) {
		Collection<ItemT> complement = new ArrayList<ItemT>(set.size() - collection.size());
		Iterator<ItemT> iterator = set.iterator();
		for (ItemT item: collection) {
			while(iterator.hasNext()) {
				ItemT currentSetItem = iterator.next();
				if (currentSetItem == item) {
					break;
				} else {
					complement.add(currentSetItem);
				}
			}
		}
		while(iterator.hasNext()) {
			complement.add(iterator.next());
		}
		return complement;
	}

	public boolean hasNext() {
		if (subSetIndicator == null) {
			return true;
		} else {
			for (int i = 0; i < subSetIndicator.length; i++) {
				if (!subSetIndicator[i]) {
					return true;
				}
			}
			return false;
		}
	}

	public Collection<ItemT> next() {
		// do initialising
		if (subSetIndicator == null) {
			subSetIndicator = new boolean[set.size()];
		} else {
			// increase binary counter
			for (int i = 0; i < subSetIndicator.length; i++) {
				if (subSetIndicator[i]) {
					// flipping 1 from right to left
					subSetIndicator[i] = false;
				} else {
					// found zero: flip to 1
					subSetIndicator[i] = true;
					break;
				}
			}
		}
		// generating subset
		ArrayList<ItemT> subset = new ArrayList<ItemT>();
		for (int i = 0; i < subSetIndicator.length; i++) {
			if (subSetIndicator[i]) {
				subset.add(set.get(i));
			}
		}
		return subset;
	}

    
    public void remove() {
        throw new UnsupportedOperationException("The 'remove' operation is not supported by power sets!");
    }
}
