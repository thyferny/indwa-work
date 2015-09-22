package com.alpine.utility.hadoop.pig;

import java.net.URL;
import java.net.URLClassLoader;

import sun.misc.VM;
/**
 * this will be useful when we support multiple version of hadoop
 * But now, it does not work.*/
public class HadoopClassLoader extends URLClassLoader {
 
	private URL[] urls;
    private boolean initialized = false;

	public HadoopClassLoader(URL[] urls ) {
		super(urls,ClassLoader.getSystemClassLoader());
		this.urls = urls;
		initialized = true;
		 
	}
	//this is very important
    protected synchronized Class<?> loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
	// First, check if the class has already been loaded
	Class c = findLoadedClass(name);
	if (c == null) {
	    try {
	    	   c = findClass(name);
	    } catch (ClassNotFoundException e) {
	        // If still not found, then invoke findClass in order
	        // to find the class.
	     
			if (getParent() != null) {
			    c = getParent().loadClass(name );
			}  
	    }
	}
	if (resolve) {
	    resolveClass(c);
	}
	return c;
    }
    
 
    private void check() {
	if (!initialized) {
	    throw new SecurityException("ClassLoader object not initialized");
	}
    }
	
    private boolean checkName(String name) {
	if ((name == null) || (name.length() == 0))
   	    return true;
	if ((name.indexOf('/') != -1)
	    || (!VM.allowArraySyntax() && (name.charAt(0) == '[')))
   	    return false;
 	return true;
    }
 
}
