
package com.alpine.datamining.operator.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


public class ItemSet implements Comparable<ItemSet>, Cloneable {

	private ArrayList<Item> items;

	private long frequency;

	public ItemSet() {
		this.items = new ArrayList<Item>(1);
	}

	public ItemSet(ArrayList<Item> items, long frequency) {
		this.items = items;
		Collections.sort(this.items);
		this.frequency = frequency;
	}

	public void addItem(Item item, long frequency) {
		items.add(item);
		Collections.sort(this.items);
		this.frequency = frequency;
	}

	public Collection<Item> getItems() {
		return items;
	}

    public Item getItem(int index) {
        return items.get(index);
    }
    
    public int getNumberOfItems() {
        return items.size();
    }
    
	public long getFrequency() {
		return frequency;
	}

	
	public int compareTo(ItemSet o) {
		// compare size
		Collection<Item> hisItems = o.getItems();
		if (items.size() < hisItems.size()) {
			return -1;
		} else if (items.size() > hisItems.size()) {
			return 1;
		} else {
			// compare items
			Iterator<Item> iterator = hisItems.iterator();
			for (Item myCurrentItem : this.items) {
				int relation = myCurrentItem.compareTo(iterator.next());
				if (relation != 0) {
					return relation;
				}
			}
			// equal sets
			return 0;
		}
	}

	
	public boolean equals(Object o) {
		if (o instanceof ItemSet) {
			return (this.compareTo((ItemSet) o) == 0);
		}
		return false;
	}

	public int hashCode() {
		return items.hashCode();
	}

	
	public String getItemsAsString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			buffer.append(iterator.next().toString());
			if (iterator.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}

	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			buffer.append(iterator.next().toString());
			if (iterator.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(", frequency: ");
		buffer.append(frequency);
		return buffer.toString();
	}

	public Object clone() {
		return new ItemSet(new ArrayList<Item>(items), frequency);
	}
}
