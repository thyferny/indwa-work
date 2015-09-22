/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopDataTypeUtil
 * Author: Will
 * Date: 12-8-2
 */
define(["dojo/_base/lang","dojo/_base/array"],function(lang,array){
    var numberTypes = ["Float","Numeric","Int","int","long","float","double","bytearray"];
    var numberTypes_db = ["Float","Numeric","Int","int","long","float","bigint","double","INTEGER","DECIMAL","DOUBLE PRECISION","BIGINT","SMALLINT","real"];
    var HADOOP_KEY_WORD = [
        "and", "any", "all", "arrange", "as", "asc", "AVG",
        "bag", "BinStorage","by","bytearray",
        "cache", "cat", "cd", "chararray", "cogroup", "CONCAT", "copyFromLocal", "copyToLocal","COUNT","cp","cross",
        "%declare", "%default", "define", "desc", "describe", "DIFF", "distinct", "double", "du", "dump",
        "e", "E", "eval", "exec", "explain",
        "f", "F", "filter", "flatten", "float", "foreach", "full",
        "generate", "group",
        "help",
        "if", "illustrate", "import", "inner", "input", "int", "into", "is",
        "join",
        "kill",
        "l", "L", "left", "limit", "load", "long", "ls",
        "map", "matches", "MAX, MIN", "mkdir", "mv",
        "not", "null",
        "onschema", "or", "order", "outer", "output",
        "parallel", "pig", "PigDump", "PigStorage", "pwd",
        "quit",
        "register", "right", "rm", "rmf", "run",
        "sample", "set", "ship", "SIZE", "split", "stderr", "stdin", "stdout", "store", "stream", "SUM",
        "TextLoader", "TOKENIZE", "through", "tuple",
        "union", "using"
    ];
    var hadoopDatatype = {
        CHARARRAY:"chararray",
        BYTEARRAY:"bytearray",
        INT:"int",
        LONG:"long",
        FLOAT:"float",
        DOUBLE:"double"
    };
    var allHadoopTypes = [
        hadoopDatatype.CHARARRAY,
        hadoopDatatype.INT,
        hadoopDatatype.LONG,
        hadoopDatatype.FLOAT,
        hadoopDatatype.DOUBLE,
        hadoopDatatype.BYTEARRAY
    ];
    var hadoopCompatibility = [
        [1,0,0,0,0,1],
        [0,1,1,1,1,1],
        [0,1,1,1,1,1],
        [0,1,1,1,1,1],
        [0,1,1,1,1,1],
        [1,1,1,1,1,1]
    ];

    /* value is position in allHadoopTypes
      * 0 hadoopDatatype.CHARARRAY
      * 1 hadoopDatatype.INT
      * 2 hadoopDatatype.LONG
      * 3 hadoopDatatype.FLOAT
      * 4 hadoopDatatype.DOUBLE
      * 5 hadoopDatatype.BYTEARRAY
      * */
    var hadoopCompatibilityType = [
        [0,0,0,0,0,0],
        [0,1,2,3,4,1],
        [0,2,2,3,4,2],
        [0,3,3,3,4,3],
        [0,4,4,4,4,4],
        [0,1,2,3,4,5]
    ];

    function _getAllHadoopTypes() {
        return allHadoopTypes;
    }

    function _getHadoopCompatibility(typeArray) {
        if (!lang.isArray(typeArray) || typeArray.length < 2) {
            return false;
        }
        for (var i=0; i<typeArray.length; i++) {
            for (var k=i+1; k<typeArray.length; k++) {
                if(!hadoopCompatibility[array.indexOf(allHadoopTypes, typeArray[i].toLowerCase())][array.indexOf(allHadoopTypes, typeArray[k].toLowerCase())]) {
                    return false;
                }
            }
        }
        return true;
    }

    function _getResultColumnType(typeArray) {
        var returnType = array.indexOf(allHadoopTypes,typeArray[0].toLowerCase());
        for (var i=1;i<typeArray.length;i++) {
            returnType =  hadoopCompatibilityType[returnType][array.indexOf(allHadoopTypes, typeArray[i].toLowerCase())];
        }
        return allHadoopTypes[returnType];
    }

    function _guessColumnType(columItems){
        var HADOOPDATATYPE = hadoopDatatype;
        var  hasCharArray = false;
        var  hasNumric = false;
        for (var i=0;i<columItems.length;i++) {
            if (columItems[i].toLowerCase() == HADOOPDATATYPE.CHARARRAY) {
                hasCharArray = true;
            } else if (columItems[i].toLowerCase() == HADOOPDATATYPE.INT
                || columItems[i].toLowerCase() == HADOOPDATATYPE.FLOAT
                || columItems[i].toLowerCase() == HADOOPDATATYPE.LONG
                || columItems[i].toLowerCase() == HADOOPDATATYPE.DOUBLE) {
                hasNumric = true;
            }
        }

        if (hasCharArray == true && hasNumric == true) {
            return "MatchTypeError"
        }

        if (hasCharArray == true && hasNumric == false) {
            return HADOOPDATATYPE.CHARARRAY;
        } else {
            var baseType = HADOOPDATATYPE.BYTEARRAY;
            for (var j=0;j<columItems.length;j++) {
                if (columItems[j]== HADOOPDATATYPE.INT
                    && baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY) {
                    baseType = HADOOPDATATYPE.INT;
                } else if( columItems[j]==HADOOPDATATYPE.LONG &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.LONG;
                } else if( columItems[j]==HADOOPDATATYPE.FLOAT &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY
                        || baseType.toLowerCase()==HADOOPDATATYPE.LONG
                        || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.FLOAT;
                } else if( columItems[j]==HADOOPDATATYPE.DOUBLE &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY
                        || baseType.toLowerCase()==HADOOPDATATYPE.LONG
                        || baseType.toLowerCase()==HADOOPDATATYPE.FLOAT
                        || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.DOUBLE;
                }

            }
            return baseType;
        }

    }

    function isSimilarType(sourcdeType,targetType){
        if(isNumberType(sourcdeType) ==true
            &&isNumberType(targetType) ==true)	{
            return true;
        }else if(sourcdeType!=null){
            return sourcdeType.toUpperCase()==targetType.toUpperCase() ;
        }
        return false;
    };

    function isNumberType(dataType) {
        if(dataType==null){
            return false;
        }
        dataType=dataType.toUpperCase();
        for(var s=0;s<numberTypes.length;s++){
            if (dataType== numberTypes[s].toUpperCase()){
                return true;
            }
        }
        dataType=dataType.toUpperCase();

        if(dataType.indexOf("DECIMAL(")!=-1 && dataType.indexOf(")")!=-1){
            return true;
        }
        //
        if(dataType.indexOf("NUMBER(")!=-1 && dataType.indexOf(")")!=-1){
            return true;
        }
        return false;
    };

    function isNumberType4DB(dataType){
        if(dataType==null){
            return false;
        }
        dataType=dataType.toUpperCase();
        for(var s=0;s<numberTypes_db.length;s++){
            if (dataType== numberTypes_db[s].toUpperCase()){
                return true;
            }
        }
        dataType=dataType.toUpperCase();

        if(dataType.indexOf("DECIMAL(")!=-1 && dataType.indexOf(")")!=-1){
            return true;
        }

        if(dataType.indexOf("NUMBER(")!=-1 && dataType.indexOf(")")!=-1){
            return true;
        }
        if(dataType.indexOf("NUMERIC(")!=-1 && dataType.indexOf(")")!=-1){
            return true;
        }
        return false;
    }

    function isHadoopKeyWord(column){
        if(column!=null){
            for(var i=0;i<HADOOP_KEY_WORD.length;i++){
                if(column.toUpperCase() == HADOOP_KEY_WORD[i].toUpperCase()){
                    return true;
                }
            }
        }

        return false;
    }

    // double > float > long > int > bytearray
    // tuple|bag|map|chararray > bytearray


    return {
        isSimilarType:isSimilarType,
        isNumberType:isNumberType,
        isNumberType4DB:isNumberType4DB,
        hadoopDatatype:hadoopDatatype,
        areHDCompatible: _getHadoopCompatibility,
        getAllHadoopTypes: _getAllHadoopTypes,
        getResultColumnType: _getResultColumnType,
        guessColumnType: _guessColumnType,
        isHadoopKeyWord:isHadoopKeyWord
    }
});