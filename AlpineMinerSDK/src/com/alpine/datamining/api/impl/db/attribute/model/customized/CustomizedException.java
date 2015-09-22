package com.alpine.datamining.api.impl.db.attribute.model.customized;

public class CustomizedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8313764745752159241L;
	
	public static final int UDF_NODE_NULL = 1001;
	
	public static final int UDF_NAME_NULL = 1002;
	
	public static final int OPERATOR_NAME_NULL = 1003;
	
	public static final int UDF_ALREADY_EXISTS = 1004;
	
	public static final int OUTPUT_NODE_NULL = 1005;
	
	public static final int OUTPUT_COLUMN_NULL = 1006;
	
	public static final int COLUMN_NAME_NULL = 1007;
	
	public static final int OUTPUT_NAME_VALIDATION_NULL = 1008;
	
	public static final int COLUMN_TYPE_NULL = 1009;
	
	public static final int COLUMN_TYPE_INVALID = 1010;
	
	public static final int PARA_NODE_NULL = 1011;
	
	public static final int PARA_NAME_NULL = 1012;
	
	public static final int POSITION_NULL = 1013;
	
	public static final int DUPULICATED_PARA = 1014;
	
	public static final int DUPULICATED_POSITION = 1015;
	
	public static final int MISSING_POSITION = 1016;
	
	public static final int PARA_ALREADY_EXISTS = 1017;
	
	private int error_id;

	private String message;

	public CustomizedException(String message, int id) {
		super(message) ;
		this.error_id= id;
		this.message=message;
	}

	public int getError_id() {
		return error_id;
	}

	public void setError_id(int errorId) {
		error_id = errorId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
