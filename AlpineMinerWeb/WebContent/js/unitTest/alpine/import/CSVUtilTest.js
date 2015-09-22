dojo.provide("unitTest.alpine.import.CSVUtilTest");
dojo.require("alpine.import.CSVUtil");
dojo.registerModulePath("alpine.import.CSVUtil","../../js/alpine/import/CSVUtil");

doh.register("CSVUtilTest" ,[
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
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
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
        }
        var retArray = [];
        retArray = alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowData,csvParams);
        doh.assertEqual(expected, retArray);
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
    }
]);