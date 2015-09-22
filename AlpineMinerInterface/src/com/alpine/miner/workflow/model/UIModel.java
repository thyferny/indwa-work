/**
 * ClassName UIModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model;

import java.beans.PropertyChangeListener;

public interface UIModel {

	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void firePropertyChange(String propName,Object oldValue,Object newValue);
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
