
package com.alpine.datamining.db;

import com.alpine.datamining.utility.DataType;




public class ColumnFactory {
	
	public static Column createColumn(String name, int valueType) {
		if (DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.BINOMINAL)||
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.NOMINAL) || 
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.TIME)||
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.DATE)||
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.DATE_TIME) ||
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.BOOLEAN) ||
				DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.OTHER)) {
			return new NominalColumn(name, valueType);
		} else if (DataType.COLUMN_VALUE_TYPE.is(valueType, DataType.NUMERICAL )) {
			return new NumericColumn(name, valueType);
		} else {
			throw new RuntimeException("ColumnFactory: cannot create column with value type '" + DataType.COLUMN_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
		}
	}


	
	public static Column createColumn(Column column, String functionName) {
		Column result = (Column) column.clone();
		if (functionName == null) {
			result.setName(column.getName());
		} else {
			result.setName(functionName + "(" + column.getName() + ")");
		}
		return result;
	}

}





