/**
 * User: robbie
 * Date: 12/4/12
 * 2012
 */

dojo.provide("unitTest.alpine.util.DataTypeUtilsTest");
dojo.require("alpine.import.CSVUtil");
//dojo.registerModulePath("alpine.import.CSVUtil","../../js/alpine/import/CSVUtil");
dojo.require("alpine.util.DataTypeUtils");
//dojo.registerModulePath("alpine.util.DataTypeUtils","../../js/alpine/util/DataTypeUtils");
dojo.require("alpine.util.DBDataTypeUtil");
//dojo.registerModulePath("alpine.util.DBDataTypeUtil","../../js/alpine/util/DBDataTypeUtil");
dojo.require("alpine.props.HadoopDataTypeUtil");
//dojo.registerModulePath("alpine.util.DataTypeUtils","../../js/alpine/util/DataTypeUtils");


doh.register("DataTypeUtilsTest" ,[
    function csvOne(){
        var rowData = [
            "a,b,c,d,e",
            "1,2,3,4,5"
        ];
        var expected = [
            ["a","b","c","d","e"],
            ["1","2","3","4","5"]
        ];
        var csvParams = {
            separator: ",",
            quote: "\"",
            escaped: "\\"
        };
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
        var type;
        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;
        type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,1,true,"db");
        doh.assertEqual(_dbTypes.INTEGER, type);
        type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,1,true,"hd");
        doh.assertEqual(_hdTypes.LONG, type);
    },
    function csvTwo(){
        var rowData = [
            "pclass,survived,name,sex,age,sibsp,parch,ticket,fare,cabin,embarked,boat,body,home",
            "1,1,\"Aubart, Mme. Leontine Pauline\",female,24,0,0,PC 17477,69.3,B35,C,9,,\"Paris, France\"",
            "1,1,\"Barber, Miss. Ellen ~\"Nellie~\"\",female,26,0,0,19877,78.85,,S,6,,"
        ];
        var expected = [
            ["pclass","survived","name","sex","age","sibsp","parch","ticket","fare","cabin","embarked","boat","body","home"],
            ["1","1","Aubart, Mme. Leontine Pauline","female","24","0","0","PC 17477","69.3","B35","C","9","","Paris, France"],
            ["1","1","Barber, Miss. Ellen \"Nellie\"","female","26","0","0","19877","78.85","","S","6","",""]
        ];
        var csvParams = {
            separator: ",",
            quote: "\"",
            escaped: "~"
        };
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);

        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;
        var expectedDB = [
            _dbTypes.INTEGER,
            _dbTypes.INTEGER,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER,
            _dbTypes.INTEGER,
            _dbTypes.INTEGER,
            _dbTypes.VARCHAR,
            _dbTypes.NUMERIC,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR
        ];
        var expectedHD = [
            _hdTypes.LONG,
            _hdTypes.LONG,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG,
            _hdTypes.LONG,
            _hdTypes.LONG,
            _hdTypes.CHARARRAY,
            _hdTypes.DOUBLE,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY
        ];
        var type;
        for (var i=0; i < expectedDB.length; i++) {
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"db");
            doh.assertEqual(expectedDB[i], type);
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"hd");
            doh.assertEqual(expectedHD[i], type);
        }

    },
    function csvThree(){
        var rowData = [
            "exchange|stock_symbol|trade_date|stock_price_open| EOL",
            "\'NYSE\'|\' APL | APL \'|2/8/10|10.84| EOL"
        ];
        var expected = [
            ["exchange","stock_symbol","trade_date","stock_price_open","EOL"],
            ["NYSE"," APL | APL ","2/8/10","10.84","EOL"]
        ];
        var csvParams = {
            separator: "|",
            quote: "\'",
            escaped: "\\"
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);

        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;
        var expectedDB = [
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.NUMERIC,
            _dbTypes.VARCHAR
        ];
        var expectedHD = [
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.DOUBLE,
            _hdTypes.CHARARRAY
        ];
        var type;
        for (var i=0; i < expectedDB.length; i++) {
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"db");
            doh.assertEqual(expectedDB[i], type);
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"hd");
            doh.assertEqual(expectedHD[i], type);
        }
    },
    function csvFour(){
        var rowData = [
            "exchange	stock_symbol	trade_date	stock_price_open	 EOL",
            "NYSE	\" APL~\"	A~\"PL \"	2/8/10	10.84	  E OL"
        ];
        var expected = [
            ["exchange","stock_symbol","trade_date","stock_price_open","EOL"],
            ["NYSE"," APL\"\tA\"PL ","2/8/10","10.84","E OL"]
        ];
        var csvParams = {
            separator: "\t",
            quote: "\"",
            escaped: "~"
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);

        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;
        var expectedDB = [
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.NUMERIC,
            _dbTypes.VARCHAR
        ];
        var expectedHD = [
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.DOUBLE,
            _hdTypes.CHARARRAY
        ];
        var type;
        for (var i=0; i < expectedDB.length; i++) {
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"db");
            doh.assertEqual(expectedDB[i], type);
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"hd");
            doh.assertEqual(expectedHD[i], type);
        }
    },
    function csvFive(){
        var rowData = [
            "a,b,c,d,e",
            "1\\,2+!~,3,4,5"
        ];
        var expected = [
            ["a","b","c","d","e"],
            ["1\\","2+!~","3","4","5"]
        ];
        var csvParams = {
            separator: ",",
            quote: "",
            escaped: ""
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);

        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;
        var expectedDB = [
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER,
            _dbTypes.INTEGER,
            _dbTypes.INTEGER
        ];
        var expectedHD = [
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG,
            _hdTypes.LONG,
            _hdTypes.LONG
        ];
        var type;
        for (var i=0; i < expectedDB.length; i++) {
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"db");
            doh.assertEqual(expectedDB[i], type);
            type = alpine.util.DataTypeUtils.getTypeOfArray(retArray,i,true,"hd");
            doh.assertEqual(expectedHD[i], type);
        }
    },
    function csvSix(){
        var rowData = [
            "a|b|c|d|e",
            "1|2|3|4|5"
        ];
        var expected = [
            ["a","b","c","d","e"],
            ["1","2","3","4","5"]
        ];
        var csvParams = {
            separator: "|",
            quote: "",
            escaped: ""
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
    },
    function csvSeven(){//titanic.csv
        var rowData = [
            "1,1,\"Barber, Miss. Ellen \"\"Nellie\"\"\",female,26,0,0,19877,78.85,,S,6,,",
            "1,1,\"Barber, Miss. Ellen \"\"Nellie\"\"\",female,26,0,0,19877,78.85,,S,6,,"
        ];
        var expected = [
            ["1","1","Barber, Miss. Ellen \"Nellie\"","female","26","0","0","19877","78.85","","S","6","",""],
            ["1","1","Barber, Miss. Ellen \"Nellie\"","female","26","0","0","19877","78.85","","S","6","",""]
        ];
        var csvParams = {
            separator: ",",
            quote: "\"",
            escaped: "\\"
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
    },
    function csvEight(){//excite.small.txt
        var rowData = [
            "610	970916071014	\"veterinary drugs and \"\"mail order\"\"\"",
            "622	970916050623	\"\"\"tom petty and the heartbreakers\"\" into the great wide open +mp3\"",
            "643	970916161710	\"missions, china\""
        ];
        var expected = [
            ["610","970916071014","veterinary drugs and \"mail order\""],
            ["622","970916050623","\"tom petty and the heartbreakers\" into the great wide open +mp3"],
            ["643","970916161710","missions, china"]
        ];
        var csvParams = {
            separator: "\t",
            quote: "\"",
            escaped: "\\"
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
    },
    function csvNine(){//excite.small.log
        var rowData = [
            "45531846E8E7C127	970916071154	\"population of maldives\"",
            "082A665972806A62	970916123431	pegasus",
            "F6C8FFEAA26F1778	970916070130	\"alicia silverstone\" cutest crush batgirl babysitter clueless",
            "A520A112137D028D	970916005634	travel,clearlake,ca."
        ];
        var expected = [
            ["45531846E8E7C127","970916071154","\"population of maldives\""],
            ["082A665972806A62","970916123431","pegasus"],
            ["F6C8FFEAA26F1778","970916070130","\"alicia silverstone\" cutest crush batgirl babysitter clueless"],
            ["A520A112137D028D","970916005634","travel,clearlake,ca."]
        ];
        var csvParams = {
            separator: "\t",
            quote: "",
            escaped: "\\"
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
    },
    function guessDelimiterTest(){
        var string1 = "NYSE	\" APL~\"	A~\"PL \"	2/8/10	10.84	  E OL";
        var string2 = "1,1,\"Barber, Miss. Ellen ~\"Nellie~\"\",female,26,0,0,19877,78.85,,S,6,,";
        var string3 = "asdkfhasjdkfhalkjshfjsdhfkjhfh13432598%&^@%#*";

        var result1, result2, result3;

        result1 = alpine.import.CSVUtil.guessDelimiter(string1);
        result2 = alpine.import.CSVUtil.guessDelimiter(string2);
        result3 = alpine.import.CSVUtil.guessDelimiter(string3);

        doh.assertEqual(result1,"Tab");
        doh.assertEqual(result2,"Comma");
        doh.assertEqual(result3,"Comma");
    },
    function guessDataType1(){
        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;

        var rows = [
            ["1","1.1","1.0","0.1","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["","1.1","1.0","0.1","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","","1.0","0.1","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",true,"0"],
            ["1","1.1","","0.1","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","false",false,"0"],
            ["1","1.1","1.0","","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","1.1","1.0","0.1","","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","false",true,"0"],
            ["1","1.1","1.0","0.1","1e10","","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","1.1","1.0","0.1","1e10","chars",""," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","1.1","1.0","0.1","1e10","chars","1r2","","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","1.1","1.0","0.1","1e10","chars","1r2"," ","","end","0x01","垚","\u0033","1.7592877E7","true",false,"0"],
            ["1","1.1","1.0","0.1","1e10","chars","1r2"," ","","","","","","","true",false,"0"]
        ];
        var expectedDB = [
            _dbTypes.INTEGER,
            _dbTypes.NUMERIC,
            _dbTypes.NUMERIC,
            _dbTypes.NUMERIC,
            _dbTypes.NUMERIC,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER,
            _dbTypes.NUMERIC,
            _dbTypes.VARCHAR,
            _dbTypes.VARCHAR,
            _dbTypes.INTEGER
        ];
        var expectedHD = [
            _hdTypes.LONG,
            _hdTypes.DOUBLE,
            _hdTypes.DOUBLE,
            _hdTypes.DOUBLE,
            _hdTypes.DOUBLE,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG,
            _hdTypes.DOUBLE,
            _hdTypes.CHARARRAY,
            _hdTypes.CHARARRAY,
            _hdTypes.LONG
        ];
        var type;
        for (var i=0; i < expectedDB.length; i++) {
            type = alpine.util.DataTypeUtils.getTypeOfArray(rows,i,false,"db");
            //console.log("exp: " + expectedDB[i] + " ... " + "got: " + type);
            doh.assertEqual(expectedDB[i], type);
            type = alpine.util.DataTypeUtils.getTypeOfArray(rows,i,false,"hd");
            doh.assertEqual(expectedHD[i], type);
        }
        doh.assertEqual("1","1");

    },
    function guessDataType2(){

        var _dbTypes = alpine.util.DBDataTypeUtil.dbTypes;
        var _hdTypes = alpine.props.HadoopDataTypeUtil.hadoopDatatype;

        var type = alpine.util.DataTypeUtils.getDataTypeByValue("true");
        doh.assertEqual(_dbTypes.VARCHAR,type);

        type = alpine.util.DataTypeUtils.getDataTypeByValue(true);
        doh.assertEqual(_dbTypes.VARCHAR,type);

        type = alpine.util.DataTypeUtils.getDataTypeByValue("false");
        doh.assertEqual(_dbTypes.VARCHAR,type);

        type = alpine.util.DataTypeUtils.getDataTypeByValue(false);
        doh.assertEqual(_dbTypes.VARCHAR,type);

    }/*,
    function guessDataType3(){

    },
    function guessDataType4(){

    },
    function guessDataType5(){

    },
    function guessDataType6(){

    }*/
]);