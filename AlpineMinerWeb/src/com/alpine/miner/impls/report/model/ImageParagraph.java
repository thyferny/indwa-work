/**
 * ClassName :ImageParagraph.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

/**
 * @author zhaoyong
 *
 */
public class ImageParagraph extends Paragraph{
	String imagePath;
	
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * @param id
	 * @param title
	 * @param content
	 */
	
	protected ImageParagraph(String id, String title, String imagepath) { 
		super(id, title, null);
		this.imagePath=imagepath;
	}


}
