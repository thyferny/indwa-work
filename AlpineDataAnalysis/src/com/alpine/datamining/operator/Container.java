/**
 * ClassName Container.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.utility.Tools;


/**
 * 
 * @author Eason
 * 
 * 
 */
public class Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7448881930208584737L;
	private List<ConsumerProducer> cpObject;


	public Container() {
		this(new ConsumerProducer[0]);
	}


	public Container(Collection<? extends ConsumerProducer> objectCollection) {
		cpObject = new ArrayList<ConsumerProducer>(objectCollection.size());
		cpObject.addAll(objectCollection);
	}
    
	public Container(ConsumerProducer... objectArray) {
		cpObject = new ArrayList<ConsumerProducer>(objectArray.length);
		for (int i = 0; i < objectArray.length; i++)
			cpObject.add(objectArray[i]);
	}

	public String toString() {
		StringBuffer result = new StringBuffer("Container (" + cpObject.size() + " objects):" + Tools.getLineSeparator());
		Iterator<ConsumerProducer> i = cpObject.iterator();
		while (i.hasNext()) {
			ConsumerProducer current = (ConsumerProducer) i.next();
            if (current != null)
                result.append(current.toString() + Tools.getLineSeparator() + (current.getSource() != null ? "(created by " + current.getSource() + ")" + Tools.getLineSeparator() : ""));
		}
		return result.toString();
	}

	public int size() {
		return cpObject.size();
	}
	

	public ConsumerProducer getElementAt(int index) {
		return cpObject.get(index);
	}
	
	public ConsumerProducer removeElementAt(int index) {
		return cpObject.remove(index);
	}
	

	public ConsumerProducer[] getArrays() {
		return cpObject.toArray(new ConsumerProducer[cpObject.size()]);
	}

	/** Gets the first ConsumerProducer which is of class cls. */
    public <T extends ConsumerProducer> T get(Class<T> cls) {
		return getInput(cls, 0, false);
	}

	/** Gets the nr-th ConsumerProducer which is of class cls. */
    public <T extends ConsumerProducer> T get(Class<T> cls, int nr) {
		return getInput(cls, nr, false);
	}

	/**
	 * Removes the first ConsumerProducer which is of class cls. The removed object is
	 * returned.
	 */
    public <T extends ConsumerProducer> T remove(Class<T> cls){
		return getInput(cls, 0, true);
	}

	/**
	 * Removes the nr-th ConsumerProducer which is of class cls. The removed object is
	 * returned.
	 */
	public <T extends ConsumerProducer> T remove(Class<T> cls, int nr)   {
		return getInput(cls, nr, true);
	}

	/**
	 * Returns true if this Container containts an ConsumerProducer of the desired
	 * class.
	 */
	public boolean contains(Class<? extends ConsumerProducer> cls) {
		try {
			getInput(cls, 0, false);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/**
	 * Gets the nr-th ConsumerProducer which is of class cls.
	 */
	@SuppressWarnings("unchecked")
	private <T extends ConsumerProducer> T getInput(Class<T> cls, int nr, boolean remove) {
		int n = 0;
		Iterator<ConsumerProducer> i = cpObject.iterator();
		while (i.hasNext()) {
			ConsumerProducer object = i.next();
			if ((object != null) && (cls.isAssignableFrom(object.getClass()))) {
				if (n == nr) {
					if (remove)
						i.remove();
					return (T)object;
				} else {
					n++;
				}
			}
		}
		throw new RuntimeException("Wrong class type!");
	}

	/**
	 * Creates a new Container by adding all IOObjects of this container to
	 * the given ConsumerProducer.
	 */
	public Container append(ConsumerProducer object) {
		return append(new ConsumerProducer[] { object });
	}

	public Container append(ConsumerProducer[] output) {
		List<ConsumerProducer> newObjects = new LinkedList<ConsumerProducer>();
		for (int i = 0; i < output.length; i++)
			newObjects.add(output[i]);
		newObjects.addAll(cpObject);
		return new Container(newObjects);
	}


	/** Appends this container's IOObjects to output. */
	public Container append(Collection<ConsumerProducer> output) {
		List<ConsumerProducer> newObjects = new LinkedList<ConsumerProducer>();
		newObjects.addAll(output);
		newObjects.addAll(cpObject);
		return new Container(newObjects);
	}

	/** Copies the contents of this Container by invoking the method copy of all IOObjects. */
	public Container copy() {
		List<ConsumerProducer> clones = new LinkedList<ConsumerProducer>();
		Iterator<ConsumerProducer> i = cpObject.iterator();
		while (i.hasNext()) {
			clones.add((i.next()).copy());
		}
		return new Container(clones);
	}

	/** Removes all Objects from this Container. */
	public void removeAll() {
		cpObject.clear();		
	}
}



