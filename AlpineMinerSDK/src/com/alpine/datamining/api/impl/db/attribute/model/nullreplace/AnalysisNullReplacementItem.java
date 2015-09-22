package com.alpine.datamining.api.impl.db.attribute.model.nullreplace;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;
import com.alpine.utility.file.StringUtil;

public class AnalysisNullReplacementItem{

	public static final String TAG_NAME="NullReplacementItem";
	
	protected static final String ATTR_COLUMNNAME = "columnName";

	protected static final String ATTR_NULL = "nullValue";

	protected static final String ATTR_TYPE = "nullType";

	//robbie, please add default value here to support the old flow
	protected static final String DEFAULT_TYPE = "value";
	
	
	protected String columnName;
	protected String value;
	protected String type = DEFAULT_TYPE;
	
    public AnalysisNullReplacementItem(String columnName, String value) {
		this.columnName = columnName;
		this.value = value;
		this.type = DEFAULT_TYPE;
	} 

    public AnalysisNullReplacementItem(String columnName, String value, String type) {
		this.columnName = columnName;
		this.value = value;
		if(StringUtil.isEmpty(type)){
			type = DEFAULT_TYPE;// to adapter old flow.
		}
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisNullReplacementItem(columnName,value,type);
	}
	
	public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof AnalysisNullReplacementItem){
			 AnalysisNullReplacementItem column = (AnalysisNullReplacementItem) obj;
			 return ModelUtility.nullableEquales(columnName, column.getColumnName())
			    && ModelUtility.nullableEquales(value, column.getValue())
                   && ModelUtility.nullableEquales(type, column.getType());
		 }else{
			 return false;
		 }
	}
	 
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ATTR_COLUMNNAME).append(":").append(columnName).append(",");
		sb.append(ATTR_NULL).append(":").append(value).append(",");
        sb.append(ATTR_TYPE).append(":").append(type);
		return sb.toString();
	}
}
