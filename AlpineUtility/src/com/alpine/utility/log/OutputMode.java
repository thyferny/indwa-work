/**
 * ClassName OutputMode.java
 *
 * Version information:1.00
 *
 * Data:2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.log;

public class OutputMode {
	public  final static OutputMode CONSOLE= new OutputMode(1);
	public  final static OutputMode FILE= new OutputMode(2);
	public  final static OutputMode DailyRollingFile= new OutputMode(3);
	public  final static OutputMode RollingFile= new OutputMode(4);
	public  final static OutputMode Writer= new OutputMode(5);
	
	public int id;
	private OutputMode(int id)
	{
		this.id=id;
	}
	public String toString()
	{
		switch(id)
		{
		case 1:return "Console";
		case 2:return "File";
		case 3:return "DailyRolling";
		case 4:return "RollingFile";
		case 5:return "Writer";
		default : return "Console";
		}
		
			
	}
	
}
