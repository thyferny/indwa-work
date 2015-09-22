/**
 * ClassName AnalysisErrorException.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.io.PrintStream;

import com.alpine.datamining.exception.WrongUsedException;

/**
 * use this exception to tell the user (API invoker) of the Miner Engine
 * that there are some error happened during mining
 * 
 * @author John Zhao
 *
 */
public class AnalysisException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4990463564644255536L;
	private String errorMsg;

	public AnalysisException(Exception e){
		super(e.getMessage(),e);
	}

	public AnalysisException(String errorMsg) {
		super(errorMsg);
		this.errorMsg=errorMsg;
	}
	public AnalysisException(String errorMsg, Exception e) {
		super(errorMsg,e);
		this.errorMsg=errorMsg;
	}
	
    public String getFullMessage() {
    	StringBuffer sb= new StringBuffer();
    	
		  if(getCause() !=null&&getCause() instanceof WrongUsedException){
			  WrongUsedException wrongUsedException=(WrongUsedException)getCause() ;
	 
			  sb.append("Error Source: "+wrongUsedException.getOperator().getClass()+"\n");
			  sb.append("Error Name:   "+wrongUsedException.getName()+"\n");
//			  sb.append("Error Name:   "+userError.getErrorName()+"\n");
//			  sb.append("Error Message:"+ userError.getDescrition()+"\n");
			  sb.append("Error Message: "+wrongUsedException.getErrorMessage() +"\n");
			   
		  }else{
			  
			  if(this.errorMsg!=null){
				  sb.append(errorMsg+"\n");
			  }else{
				  sb.append(getMessage()+"\n");
			  }
			 
		  }
		  return sb.toString();
    }
	
	  public void printStackTrace(PrintStream s) {
		  if(getCause() !=null&&getCause() instanceof WrongUsedException){
			  WrongUsedException wrongUsedException=(WrongUsedException)getCause() ;
			  if(this.errorMsg!=null){
				  s.print(errorMsg+"\n");
			  }
			  s.print("Error Source: "+wrongUsedException.getOperator().getClass()+"\n");
			  s.print("Error Name:   "+wrongUsedException.getName()+"\n");
//			  s.print("Error Name:   "+userError.getErrorName()+"\n");
//			  s.print("Error Message:"+ userError.getDescrition()+"\n");
			  s.print("Error Message: "+wrongUsedException.getErrorMessage() +"\n");
			  wrongUsedException.printStackTrace(s);
		  }else{
			  
			  if(this.errorMsg!=null){
				  s.print(errorMsg+"\n");
			  }
			  super.printStackTrace(s);
		  }
	  }
		
		public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}

}
