/**
 * User: robbie
 * Date: 12/3/12
 * 2012
 */

define([
    "dojo/_base/array",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/util/DBDataTypeUtil"
],function(array, hadoopTypeUtil, dbTypeUtil){

    /*var generic = {
        text:   "VARCHAR",
        int:    "INTEGER",
        bigint: "BIGINT",
        dec:    "NUMERIC",
        null:   "null"
    };*/
    var generic = {
    	type_text:   "type_text",
        type_int:    "type_int",
        type_bigint: "type_bigint",
        type_dec:    "type_dec",
        type_null:   "type_null"
    };

    var _dbTypes = dbTypeUtil.dbTypes;
    var _hdTypes = hadoopTypeUtil.hadoopDatatype;

    var converter = {
    		type_text:   {db: _dbTypes.VARCHAR, hd: _hdTypes.CHARARRAY},
    		type_int:    {db: _dbTypes.INTEGER, hd: _hdTypes.LONG},//returning long for HD
    		type_bigint: {db: _dbTypes.BIGINT,  hd: _hdTypes.LONG},
    		type_dec:    {db: _dbTypes.NUMERIC, hd: _hdTypes.DOUBLE},
    		type_null:   {db: "null",           hd: "null"}
    };

    /* from XML/JSON helpers */
    /*function _getDataTypeByValue(value) {
        var isNumber = false;
        value = value+"";
        try {
            if (isNaN(value) == false) {
                isNumber = true;
            }
        } catch (e) {
            isNumber = false;
        }
        var dataType = "chararray";
        if (isNumber == true && value.indexOf(".") == -1) {
            dataType = "long";
        }
        if (isNumber == true && value.indexOf(".") != -1) {
            dataType = "double";
        }
        var reg = /^((\d+.?\d+)[Ee]{1}(\d+))$/ig;
        if(reg.test(value)==true){
            dataType = "double";
        }
        return dataType;
    }*/

    function _getGenericDataTypeByValue(value) {
        if (typeof value == 'undefined' || value === "") {return generic.type_null;}
        if (isNaN(value) || isNaN(parseFloat(value))){
            return generic.type_text;
        }
        if (value != parseInt(value) || /\./g.test(value)) {
            return generic.type_dec;
        } else {
            return (Math.abs(value) > 2147483647) ? generic.type_bigint : generic.type_int;
        }
    }

    function _getGenericTypeOfArray(dataRows, columnNumber, hasHeader) {
        var i = (hasHeader || false) ? 1 : 0;
        var isFloat = true;
        var isInt = true;
        var needsBigInt = false;
        var hasNonNull = false;
        var num, guess;
        var typeCol = generic.type_text;
        for (i; i < dataRows.length; i++) {
            num = dataRows[i][columnNumber];
            guess = _getGenericDataTypeByValue(num);
            switch (guess) {
                case generic.type_text:
                    isFloat = false;
                    isInt = false;
                    break;
                case generic.type_dec:
                    isInt = false;
                    _isNonNull();
                    break;
                case generic.type_bigint:
                    needsBigInt = true;
                    _isNonNull();
                    break;
                case generic.type_int:
                    _isNonNull();
                    break;
                default:
                    break;
            }
            if (!(isFloat || isInt)) {
                break;
            }
        }
        if (isInt) {
            typeCol = needsBigInt ? generic.type_bigint : generic.type_int;
        } else if (isFloat) {
            typeCol = generic.type_dec;
        }
        if (!hasNonNull) {
            typeCol = generic.type_text;
        }
        return typeCol;



        function _isNonNull() {
            hasNonNull = true;
        }
    }

    function _getTypeOfArray(dataRows, columnNumber, hasHeader, sys) {
        var sysType = sys == "hd" ? "hd" : "db";
        var type = _getGenericTypeOfArray(dataRows, columnNumber, hasHeader);
        return converter[type][sysType];
    }

    function _getTypeByValue(value, sys) {
        var sysType = sys == "hd" ? "hd" : "db";
        var genericType = _getGenericDataTypeByValue(value);
        return converter[genericType][sysType];
    }

    function _getTypeByXPath(typeObject,xpath) {
        if (typeObject[xpath] != null && typeObject[xpath].length > 0) {
            if (array.indexOf(typeObject[xpath],_hdTypes.CHARARRAY) != -1) {
                return _hdTypes.CHARARRAY;
            }
            if (array.indexOf(typeObject[xpath],_hdTypes.DOUBLE) != -1) {
                return _hdTypes.DOUBLE;
            }
            if (array.indexOf(typeObject[xpath],_hdTypes.LONG) != -1) {
                return _hdTypes.LONG;
            }
        }
        return _hdTypes.CHARARRAY;
    }

    return {
        getDataTypeByValue: _getTypeByValue,
        getTypeOfArray: _getTypeOfArray,
        getTypeByXPath: _getTypeByXPath
    };
});