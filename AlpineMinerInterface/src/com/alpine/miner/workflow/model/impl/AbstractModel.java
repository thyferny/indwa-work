/**
 * ClassName AbstractModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.alpine.miner.workflow.model.UIModel;
/**
 * 
 * @author jimmy
 *
 */
public class AbstractModel implements UIModel{
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	/**
	 * add property change listener to operator model
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener){
		listeners.addPropertyChangeListener(listener);
	}
	public void firePropertyChange(String propName,Object oldValue,Object newValue){
		listeners.firePropertyChange(propName,oldValue,newValue);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener){
		listeners.removePropertyChangeListener(listener);
	}
}
