package com.alpine.utility.exception;



public class EmptyFileException extends Exception{
	private static final long serialVersionUID = -4990463564644255536L;

	public EmptyFileException(Exception e){
		super(e.getMessage(),e);
	}

	public EmptyFileException(String cause) {
		super(cause);
	}
}
